package io.oltre_backend.food;

import java.net.http.HttpClient;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class FoodService {

    @Value("${app.usda.api-key}")
    private String usdaApiKey;

    private final RestClient usdaClient;
    private final RestClient translateClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FoodService() throws Exception {
        // SSL désactivé pour les environnements d'entreprise
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                @Override
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                @Override
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAll, new java.security.SecureRandom());
        HttpClient httpClient = HttpClient.newBuilder().sslContext(sslContext).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);

        this.usdaClient = RestClient.builder()
                .baseUrl("https://api.nal.usda.gov")
                .defaultHeader("Accept", "application/json")
                .requestFactory(factory)
                .build();

        this.translateClient = RestClient.builder()
        .baseUrl("https://api.mymemory.translated.net")
        .requestFactory(factory)
        .build();
    }

    public List<FoodDTO> search(String query) {
    try {
        String englishQuery = translate(query, "fr|en");

        System.out.println("Query traduite : " + englishQuery);

        USDAResponse response = usdaClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/fdc/v1/foods/search")
                        .queryParam("query", englishQuery)
                        .queryParam("api_key", usdaApiKey)
                        .queryParam("pageSize", "20")
                        .queryParam("dataType", "Foundation,SR Legacy")
                        .build()
                )
                .retrieve()
                .body(USDAResponse.class);

        if (response == null || response.getFoods() == null) return List.of();

        return response.getFoods().stream()
        .map(f -> new FoodDTO(
                translate(f.getDescription(), "en|fr"),
                getNutrient(f, 1008),
                getNutrient(f, 1003),
                getNutrient(f, 1005),
                getNutrient(f, 1004)
        ))
        .filter(f -> f.getName().toLowerCase().contains(query.toLowerCase())
                  || f.getName().toLowerCase().contains(englishQuery.toLowerCase()))
        .toList();

    } catch (Exception e) {
        System.err.println("Erreur : " + e.getMessage());
        return List.of();
    }
}

private String translate(String text, String langPair) {
    try {
        String response = translateClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/get")
                        .queryParam("q", text)
                        .queryParam("langpair", langPair)
                        .build()
                )
                .retrieve()
                .body(String.class);

        JsonNode node = objectMapper.readTree(response);
        return node.get("responseData").get("translatedText").asText();

    } catch (JsonProcessingException e) {
        return text; // retourne l'original si erreur
    }
}


    private Integer getNutrient(USDAFood food, int nutrientId) {
        if (food.getFoodNutrients() == null) return 0;
        return food.getFoodNutrients().stream()
                .filter(n -> n.getNutrientId() == nutrientId)
                .findFirst()
                .map(n -> (int) Math.round(n.getValue()))
                .orElse(0);
    }
}