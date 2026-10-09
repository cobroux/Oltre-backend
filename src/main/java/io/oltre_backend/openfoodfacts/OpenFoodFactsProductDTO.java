package io.oltre_backend.openfoodfacts;

public class OpenFoodFactsProductDTO {

    private String barcode;
    private String productName;
    private String brand;
    private String imageUrl;
    private Double caloriesPer100g;
    private Double proteinPer100g;
    private Double carbsPer100g;
    private Double fatPer100g;

    public OpenFoodFactsProductDTO(String barcode, String productName, String brand, String imageUrl,
                                    Double caloriesPer100g, Double proteinPer100g, Double carbsPer100g, Double fatPer100g) {
        this.barcode = barcode;
        this.productName = productName;
        this.brand = brand;
        this.imageUrl = imageUrl;
        this.caloriesPer100g = caloriesPer100g;
        this.proteinPer100g = proteinPer100g;
        this.carbsPer100g = carbsPer100g;
        this.fatPer100g = fatPer100g;
    }

    public String getBarcode() { return barcode; }
    public String getProductName() { return productName; }
    public String getBrand() { return brand; }
    public String getImageUrl() { return imageUrl; }
    public Double getCaloriesPer100g() { return caloriesPer100g; }
    public Double getProteinPer100g() { return proteinPer100g; }
    public Double getCarbsPer100g() { return carbsPer100g; }
    public Double getFatPer100g() { return fatPer100g; }
}
