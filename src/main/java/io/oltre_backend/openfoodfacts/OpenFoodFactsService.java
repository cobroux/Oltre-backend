package io.oltre_backend.openfoodfacts;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenFoodFactsService {

    private static final int MAX_RESULTS = 15;
    // cgi/search.pl matches on name, brand, categories AND generic_name, then
    // sorts by popularity (scan count) rather than text relevance - "pate"
    // surfaces Nutella ("pâte à tartiner" category) above actual pasta
    // brands. We fetch a wider pool and re-rank it ourselves by how well the
    // product's own name matches, so the popularity bias doesn't bury the
    // results that are actually about what was typed. Kept modest (rather
    // than e.g. 50) because cgi/search.pl is already slow on its own, and a
    // bigger page_size makes it slower still - not worth it for marginal
    // extra re-ranking quality.
    private static final int FETCH_POOL_SIZE = 24;

    private final RestClient restClient;

    public OpenFoodFactsService(
            @Value("${openfoodfacts.base-url}") String baseUrl,
            @Value("${openfoodfacts.user-agent}") String userAgent) {
        // cgi/search.pl can be genuinely slow (several seconds) - without a
        // timeout a struggling request just hangs, leaving the "recherche en
        // cours" spinner stuck indefinitely on the frontend instead of
        // failing fast into the empty-results fallback.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                // OpenFoodFacts asks every integration to identify itself with a
                // descriptive User-Agent instead of a generic HTTP client string.
                .defaultHeader("User-Agent", userAgent)
                .build();
    }

    public List<OpenFoodFactsProductDTO> search(String query) {
        if (query == null || query.isBlank()) return List.of();

        SearchResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/cgi/search.pl")
                            .queryParam("search_terms", query)
                            // cgi/search.pl is built for the HTML search form's POST,
                            // not a plain GET query - without these two it silently
                            // ignores search_terms and the request returns no
                            // products at all.
                            .queryParam("search_simple", "1")
                            .queryParam("action", "process")
                            .queryParam("json", "1")
                            .queryParam("page_size", FETCH_POOL_SIZE)
                            .build())
                    .retrieve()
                    .body(SearchResponse.class);
        } catch (Exception e) {
            // Timeout ou API injoignable : on renvoie une liste vide plutôt
            // qu'un 500 - le frontend sait déjà traiter "aucun résultat".
            return List.of();
        }

        if (response == null || response.products == null) return List.of();

        String normalizedQuery = normalize(query);
        List<ScoredProduct> scored = new ArrayList<>();
        for (RawProduct p : response.products) {
            if (p.product_name == null || p.product_name.isBlank()) continue;
            if (p.nutriments == null) continue;

            // Most products carry energy-kcal_100g directly, but some only
            // have the legacy energy_100g in kJ - convert rather than drop
            // the product.
            Double kcal = p.nutriments.energyKcal100g;
            if (kcal == null && p.nutriments.energy_100g != null) {
                kcal = p.nutriments.energy_100g / 4.184;
            }
            if (kcal == null) continue;

            OpenFoodFactsProductDTO dto = new OpenFoodFactsProductDTO(
                    p.code,
                    p.product_name,
                    p.brands,
                    p.image_front_small_url,
                    Math.round(kcal * 10) / 10.0,
                    p.nutriments.proteins_100g,
                    p.nutriments.carbohydrates_100g,
                    p.nutriments.fat_100g
            );
            scored.add(new ScoredProduct(dto, nameMatchScore(normalize(p.product_name), normalizedQuery)));
        }

        return scored.stream()
                .sorted(Comparator.comparingInt(ScoredProduct::score))
                .map(ScoredProduct::product)
                .limit(MAX_RESULTS)
                .toList();
    }

    // 0 = le nom du produit commence par la recherche, 1 = la recherche
    // apparaît comme mot entier dans le nom, 2 = simple sous-chaîne, 3 = le
    // nom ne correspond pas du tout (le match venait d'ailleurs - marque,
    // catégorie... - on le garde mais tout en bas).
    int nameMatchScore(String normalizedName, String normalizedQuery) {
        if (normalizedName.startsWith(normalizedQuery)) return 0;
        if (normalizedName.matches(".*\\b" + java.util.regex.Pattern.quote(normalizedQuery) + "\\b.*")) return 1;
        if (normalizedName.contains(normalizedQuery)) return 2;
        return 3;
    }

    String normalize(String s) {
        String withoutAccents = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase();
    }

    private record ScoredProduct(OpenFoodFactsProductDTO product, int score) {}

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
        public Double energy_100g;
        public Double proteins_100g;
        public Double carbohydrates_100g;
        public Double fat_100g;
    }
}
