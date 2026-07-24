package io.oltre_backend.food;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenFoodFactsResponse {

    private List<Product> products;

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Product {

        @JsonProperty("product_name")
        private String productName;

        private Map<String, Object> nutriments;

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Map<String, Object> getNutriments() { return nutriments; }
        public void setNutriments(Map<String, Object> nutriments) { this.nutriments = nutriments; }
    }
}