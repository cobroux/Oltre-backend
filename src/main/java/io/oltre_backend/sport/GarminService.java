package io.oltre_backend.sport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class GarminService {

    private static final Logger log = LoggerFactory.getLogger(GarminService.class);

    private final RestClient restClient;

    public GarminService(
            @Value("${garmin.service-url}") String garminServiceUrl,
            @Value("${garmin.service-token}") String garminServiceToken) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(15000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(garminServiceUrl)
                .defaultHeader("X-Internal-Token", garminServiceToken)
                .build();
    }

    public void connect(Long userId, String email, String password) {
        try {
            restClient.post()
                    .uri("/users/{userId}/connect", userId)
                    .body(new ConnectPayload(email, password))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Unauthorized e) {
            // Le sidecar garmin-mcp renvoie le détail réel de l'échec dans le
            // corps ({"detail": "..."}) : identifiants invalides, Garmin
            // injoignable, ou un défi anti-bot/MFA qui bloque le login même
            // avec les bons identifiants. On le relaie plutôt que d'afficher
            // un "identifiants invalides" trompeur dans tous les cas.
            throw new GarminAuthException(extractDetail(e).orElse("Identifiants Garmin invalides."));
        } catch (RestClientException e) {
            throw unavailable("connect", e);
        }
    }

    private Optional<String> extractDetail(HttpClientErrorException e) {
        try {
            Map<String, Object> body = e.getResponseBodyAs(Map.class);
            Object detail = body != null ? body.get("detail") : null;
            return detail != null ? Optional.of(detail.toString()) : Optional.empty();
        } catch (Exception parseError) {
            return Optional.empty();
        }
    }

    public boolean isConnected(Long userId) {
        try {
            StatusResponse status = restClient.get()
                    .uri("/users/{userId}/status", userId)
                    .retrieve()
                    .body(StatusResponse.class);
            return status != null && status.connected;
        } catch (RestClientException e) {
            throw unavailable("status", e);
        }
    }

    // Sidecar éteint, timeout ou 5xx : 502 propre plutôt qu'un 500.
    private GarminServiceUnavailableException unavailable(String operation, RestClientException e) {
        log.warn("Garmin sidecar injoignable pour {}", operation, e);
        return new GarminServiceUnavailableException("Service Garmin injoignable. Réessaie plus tard.");
    }

    public void disconnect(Long userId) {
        try {
            restClient.delete()
                    .uri("/users/{userId}/connect", userId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw unavailable("disconnect", e);
        }
    }

    public List<GarminActivityDTO> getActivities(Long userId, String monday, int limit) {
        return getActivities(userId, monday, limit, 0);
    }

    private List<GarminActivityDTO> getActivities(Long userId, String monday, int limit, int start) {
        try {
            GarminActivity[] activities = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{userId}/activities")
                            .queryParam("limit", limit)
                            .queryParam("start", start)
                            .queryParamIfPresent("since", Optional.ofNullable(monday))
                            .build(userId))
                    .retrieve()
                    .body(GarminActivity[].class);

            if (activities == null) return List.of();
            return Arrays.stream(activities).map(GarminActivityDTO::new).toList();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GarminAuthException("Compte Garmin non connecté ou session expirée.");
        } catch (RestClientException e) {
            throw unavailable("activities", e);
        }
    }

    public GarminActivityDetailDTO getActivityDetails(Long userId, String activityId) {
        try {
            return restClient.get()
                    .uri("/users/{userId}/activities/{activityId}/details", userId, activityId)
                    .retrieve()
                    .body(GarminActivityDetailDTO.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GarminAuthException("Compte Garmin non connecté ou session expirée.");
        } catch (HttpClientErrorException.NotFound e) {
            // Le sidecar renvoie une 404 "Not Found" générique de FastAPI
            // quand la route elle-même n'existe pas (ex: sidecar pas encore
            // redéployé avec la dernière version) - pas une vraie 404
            // "activité introuvable" côté Garmin. On le journalise (sinon ce
            // cas devenait invisible dans les logs depuis qu'il est rattrapé
            // proprement) et on le dit clairement au lieu de laisser remonter
            // une stack trace Spring en 500.
            log.warn("Garmin sidecar a renvoyé 404 sur /activities/{}/details - route manquante, sidecar pas à jour ?", activityId);
            throw new GarminServiceUnavailableException(
                    "Service Garmin indisponible ou pas à jour. Réessaie plus tard.");
        } catch (RestClientException e) {
            log.warn("Garmin sidecar injoignable pour /activities/{}/details", activityId, e);
            throw new GarminServiceUnavailableException("Service Garmin injoignable. Réessaie plus tard.");
        }
    }

    private static final double[] RUN_DISTANCE_BRACKET_KM = {5, 10, 15, 20, 21.1, 42.2};
    private static final String[] RUN_DISTANCE_BRACKET_LABEL = {"5 km", "10 km", "15 km", "20 km", "Semi-marathon", "Marathon"};

    private static final int SIDECAR_PAGE_SIZE = 200;
    private static final int MAX_RECORD_ACTIVITIES = 5000;

    private String sportFamily(String sportType) {
        String type = sportType != null ? sportType : "";
        if (type.contains("running")) return "running";
        if (type.contains("cycling") || type.contains("biking")) return "cycling";
        if (type.contains("strength")) return "strength";
        if (type.contains("swim")) return "swim";
        if (type.contains("walking") || type.contains("hiking")) return "walking";
        return "other";
    }

    public GarminRecordsResponseDTO getRecords(Long userId) {
        // Le sidecar plafonne à 200 activités par appel : sans pagination,
        // "depuis toujours" ne voyait que les 200 dernières (≈ cette année).
        List<GarminActivityDTO> activities = new ArrayList<>();
        for (int start = 0; start < MAX_RECORD_ACTIVITIES; start += SIDECAR_PAGE_SIZE) {
            List<GarminActivityDTO> page = getActivities(userId, null, SIDECAR_PAGE_SIZE, start);
            activities.addAll(page);
            if (page.size() < SIDECAR_PAGE_SIZE) break;
        }
        List<GarminActivityDTO> thisYearActivities = activities.stream()
                .filter(a -> isCurrentYear(a.getStartLocal()))
                .toList();

        return new GarminRecordsResponseDTO(
                computeRecordsForActivities(activities),
                computeRecordsForActivities(thisYearActivities)
        );
    }

    private boolean isCurrentYear(String startLocal) {
        if (startLocal == null || startLocal.length() < 4) return false;
        return startLocal.substring(0, 4).equals(String.valueOf(java.time.Year.now().getValue()));
    }

    private GarminRecordsDTO computeRecordsForActivities(List<GarminActivityDTO> activities) {
        List<GarminActivityDTO> runs = activities.stream()
                .filter(a -> "running".equals(sportFamily(a.getSportType())))
                .toList();

        Map<String, SportRecordDTO> otherRecords = new LinkedHashMap<>();
        for (GarminActivityDTO a : activities) {
            String family = sportFamily(a.getSportType());
            // Running has its own distance-bracket records above. Strength
            // training has no distance/elevation/speed to track, so it
            // never has anything meaningful to show here.
            if ("running".equals(family) || "strength".equals(family)) continue;

            SportRecordDTO record = otherRecords.computeIfAbsent(family, SportRecordDTO::new);

            if (a.getDistance() != null) {
                double distanceKm = a.getDistance() / 1000.0;
                if (record.getBestDistanceKm() == null || distanceKm > record.getBestDistanceKm()) {
                    record.setBestDistanceKm(distanceKm);
                    record.setBestDistanceDate(a.getStartLocal());
                }
            }

            if (a.getElevationGain() != null
                    && (record.getBestElevationGainM() == null || a.getElevationGain() > record.getBestElevationGainM())) {
                record.setBestElevationGainM(a.getElevationGain());
                record.setBestElevationDate(a.getStartLocal());
            }

            if (a.getAvgSpeed() != null) {
                double speedKmh = a.getAvgSpeed() * 3.6;
                if (record.getBestAvgSpeedKmh() == null || speedKmh > record.getBestAvgSpeedKmh()) {
                    record.setBestAvgSpeedKmh(speedKmh);
                    record.setBestAvgSpeedDate(a.getStartLocal());
                }
            }
        }

        List<SportRecordDTO> populatedOtherRecords = otherRecords.values().stream()
                .filter(r -> r.getBestDistanceKm() != null
                        || r.getBestElevationGainM() != null
                        || r.getBestAvgSpeedKmh() != null)
                .toList();

        return new GarminRecordsDTO(computeRunningRecords(runs), populatedOtherRecords);
    }

    private List<RunningRecordDTO> computeRunningRecords(List<GarminActivityDTO> runs) {
        List<RunningRecordDTO> result = new ArrayList<>();

        for (int i = 0; i < RUN_DISTANCE_BRACKET_KM.length; i++) {
            double targetKm = RUN_DISTANCE_BRACKET_KM[i];
            GarminActivityDTO best = null;
            for (GarminActivityDTO a : runs) {
                if (a.getDistance() == null || a.getMovingTime() == null) continue;
                double km = a.getDistance() / 1000.0;
                // shortcut: temps de la sortie entière (pas de meilleur segment
                // dans une sortie plus longue) ; fenêtre étroite pour qu'une
                // sortie de 14 km ne compte pas comme un 15 km, ni un semi comme un 20 km.
                if (km < targetKm * 0.99 || km > targetKm * 1.03) continue;
                if (best == null || a.getMovingTime() < best.getMovingTime()) {
                    best = a;
                }
            }
            if (best != null) {
                result.add(new RunningRecordDTO(RUN_DISTANCE_BRACKET_LABEL[i], targetKm,
                        best.getDistance() / 1000.0, best.getMovingTime(), best.getStartLocal()));
            }
        }

        runs.stream()
                .filter(a -> a.getDistance() != null && a.getMovingTime() != null)
                .max(Comparator.comparingDouble(GarminActivityDTO::getDistance))
                .ifPresent(longest -> {
                    double km = longest.getDistance() / 1000.0;
                    boolean alreadyCovered = result.stream()
                            .anyMatch(r -> Math.abs(r.getDistanceKm() - km) < 0.1);
                    if (!alreadyCovered) {
                        result.add(new RunningRecordDTO("Plus longue sortie", km, km,
                                longest.getMovingTime(), longest.getStartLocal()));
                    }
                });

        return result;
    }

    record ConnectPayload(String email, String password) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class StatusResponse {
        public boolean connected;
    }
}
