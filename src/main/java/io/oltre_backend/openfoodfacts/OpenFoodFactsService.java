package io.oltre_backend.openfoodfacts;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenFoodFactsService {

    private static final int MAX_RESULTS = 15;

    private final RestClient restClient;

    public OpenFoodFactsService(
            @Value("${openfoodfacts.base-url}") String baseUrl,
            @Value("${openfoodfacts.user-agent}") String userAgent) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                // OpenFoodFacts asks every integration to identify itself with a
                // descriptive User-Agent instead of a generic HTTP client string.
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    public List<OpenFoodFactsProductDTO> search(String query) {
        if (query == null || query.isBlank()) return List.of();

        SearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cgi/search.pl")
                        .queryParam("search_terms", query)
                        .queryParam("json", "1")
                        .queryParam("page_size", MAX_RESULTS)
                        .build())
                .retrieve()
                .body(SearchResponse.class);

        if (response == null || response.products == null) return List.of();

        List<OpenFoodFactsProductDTO> results = new ArrayList<>();
        for (RawProduct p : response.products) {
            if (p.product_name == null || p.product_name.isBlank()) continue;
            if (p.nutriments == null || p.nutriments.energyKcal100g == null) continue;

            results.add(new OpenFoodFactsProductDTO(
                    p.code,
                    p.product_name,
                    p.brands,
                    p.image_front_small_url,
                    p.nutriments.energyKcal100g,
                    p.nutriments.proteins_100g,
                    p.nutriments.carbohydrates_100g,
                    p.nutriments.fat_100g
            ));
            if (results.size() >= MAX_RESULTS) break;
        }
        return results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SearchResponse {
        public List<RawProduct> products;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class RawProduct {
        public String code;
        public String product_name;
        public String brands;
        public String image_front_small_url;
        public Nutriments nutriments;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Nutriments {
        @JsonProperty("energy-kcal_100g")
        public Double energyKcal100g;
        public Double proteins_100g;
        public Double carbohydrates_100g;
        public Double fat_100g;
    }
}
