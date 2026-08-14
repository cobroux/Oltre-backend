package io.oltre_backend.sport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
public class StravaService {

    @Value("${strava.client-id}")
    private String clientId;

    @Value("${strava.client-secret}")
    private String clientSecret;

    @Value("${strava.access-token}")
    private String accessToken;

    @Value("${strava.refresh-token}")
    private String refreshToken;

    // Token expire dans 6h — on stocke l'expiration en mémoire
    private long tokenExpiresAt = 0;

    private final RestClient restClient;
    private final RestClient tokenClient;

    public StravaService() throws Exception {
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAll, new java.security.SecureRandom());
        HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);

        this.restClient = RestClient.builder()
                .baseUrl("https://www.strava.com/api/v3")
                .requestFactory(factory)
                .build();

        this.tokenClient = RestClient.builder()
                .baseUrl("https://www.strava.com")
                .requestFactory(factory)
                .build();
    }

    // Rafraîchit le token si expiré avant chaque appel
    private String getValidAccessToken() {
        long now = Instant.now().getEpochSecond();
        if (tokenExpiresAt == 0 || now >= tokenExpiresAt - 300) { // refresh 5min avant expiration
            refreshAccessToken();
        }
        return accessToken;
    }

    private void refreshAccessToken() {
        try {
            TokenResponse response = tokenClient.post()
                    .uri("/oauth/token")
                    .body(new TokenRequest(clientId, clientSecret, refreshToken))
                    .retrieve()
                    .body(TokenResponse.class);

            if (response != null) {
                this.accessToken   = response.accessToken;
                this.refreshToken  = response.refreshToken;
                this.tokenExpiresAt = response.expiresAt;
                System.out.println("Strava token rafraîchi — expire à : " + Instant.ofEpochSecond(response.expiresAt));
            }
        } catch (Exception e) {
            System.err.println("Erreur refresh token Strava : " + e.getMessage());
        }
    }

    public List<StravaActivityDTO> getActivities(String monday, int limit) {
    // Convertit la date du lundi en timestamp Unix
    long after = 0;
    if (monday != null) {
        after = java.time.LocalDate.parse(monday)
                .atStartOfDay(java.time.ZoneOffset.UTC)
                .toEpochSecond();
    }

    final long afterFinal = after;

    StravaActivity[] activities = restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/athlete/activities")
                    .queryParam("after", afterFinal)  // ← filtre natif Strava
                    .queryParam("per_page", limit)
                    .build()
            )
            .header("Authorization", "Bearer " + getValidAccessToken())
            .retrieve()
            .body(StravaActivity[].class);

    if (activities == null) return List.of();

    return Arrays.stream(activities)
            .map(StravaActivityDTO::new)
            .toList();
}
    // ── Classes internes pour le refresh ────────────────
    record TokenRequest(
            @JsonProperty("client_id")     String clientId,
            @JsonProperty("client_secret") String clientSecret,
            @JsonProperty("refresh_token") String refreshToken,
            @JsonProperty("grant_type")    String grantType
    ) {
        TokenRequest(String clientId, String clientSecret, String refreshToken) {
            this(clientId, clientSecret, refreshToken, "refresh_token");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TokenResponse {
        @JsonProperty("access_token")  String accessToken;
        @JsonProperty("refresh_token") String refreshToken;
        @JsonProperty("expires_at")    long   expiresAt;
    }
}