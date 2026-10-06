package io.oltre_backend.sport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class GarminService {

    private final RestClient restClient;

    public GarminService(
            @Value("${garmin.service-url}") String garminServiceUrl,
            @Value("${garmin.service-token}") String garminServiceToken) {
        this.restClient = RestClient.builder()
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
            throw new GarminAuthException("Identifiants Garmin invalides.");
        }
    }

    public boolean isConnected(Long userId) {
        StatusResponse status = restClient.get()
                .uri("/users/{userId}/status", userId)
                .retrieve()
                .body(StatusResponse.class);
        return status != null && status.connected;
    }

    public void disconnect(Long userId) {
        restClient.delete()
                .uri("/users/{userId}/connect", userId)
                .retrieve()
                .toBodilessEntity();
    }

    public List<GarminActivityDTO> getActivities(Long userId, String monday, int limit) {
        try {
            GarminActivity[] activities = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{userId}/activities")
                            .queryParam("limit", limit)
                            .queryParamIfPresent("since", Optional.ofNullable(monday))
                            .build(userId))
                    .retrieve()
                    .body(GarminActivity[].class);

            if (activities == null) return List.of();
            return Arrays.stream(activities).map(GarminActivityDTO::new).toList();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GarminAuthException("Compte Garmin non connecté ou session expirée.");
        }
    }

    private static final double[] RUN_DISTANCE_BRACKET_KM = {5, 10, 15, 20, 21.1, 42.2};
    private static final String[] RUN_DISTANCE_BRACKET_LABEL = {"5 km", "10 km", "15 km", "20 km", "Semi-marathon", "Marathon"};

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
        List<GarminActivityDTO> activities = getActivities(userId, null, 500);
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
                if (km < targetKm * 0.9 || km > targetKm * 1.1) continue;
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
