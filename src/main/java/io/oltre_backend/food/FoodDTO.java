package io.oltre_backend.food;

public class FoodDTO {
    private String name;
    private Integer calories;   // per 100g
    private Integer protein_g;
    private Integer carbohydrates_total_g;
    private Integer fat_total_g;

   public FoodDTO(String name, Integer calories, Integer protein_g, Integer carbohydrates_total_g, Integer fat_total_g) {
    this.name = name;
    this.calories = calories;
    this.protein_g = protein_g;
    this.carbohydrates_total_g = carbohydrates_total_g;
    this.fat_total_g = fat_total_g;
}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Integer getProtein_g() {
        return protein_g;
    }

    public void setProtein_g(Integer protein_g) {
        this.protein_g = protein_g;
    }

    public void setCarbohydrates_total_g(Integer carbohydrates_total_g) {
        this.carbohydrates_total_g = carbohydrates_total_g;
    }

    public Integer getCarbohydrates_total_g() {
        return carbohydrates_total_g;
    }

    public Integer getFat_total_g() {
        return fat_total_g;
    }

    public void setFat_total_g(Integer fat_total_g) {
        this.fat_total_g = fat_total_g;
    }
}