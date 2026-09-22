package io.oltre_backend.sport;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class GarminService {

    private final RestClient restClient;

    public GarminService(@Value("${garmin.service-url}") String garminServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(garminServiceUrl)
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

    record ConnectPayload(String email, String password) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class StatusResponse {
        public boolean connected;
    }
}
