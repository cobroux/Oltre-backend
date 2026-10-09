package io.oltre_backend.meal;

import java.time.LocalDate;

public class MealDTO {

    private Long id;
    private String mealName;
    private String mealDescript;
    private MealType mealType;
    private LocalDate mealDate;
    private Integer calories;
    private Double proteinG;
    private Double carbsG;
    private Double fatG;
    private Double quantityG;
    private String offBarcode;

    public MealDTO() {}

    public MealDTO(Long id, String mealName, String mealDescript,
                   MealType mealType, LocalDate mealDate,
                   Integer calories, Double proteinG, Double carbsG, Double fatG,
                   Double quantityG, String offBarcode) {
        this.id = id;
        this.mealName = mealName;
        this.mealDescript = mealDescript;
        this.mealType = mealType;
        this.mealDate = mealDate;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatG = fatG;
        this.quantityG = quantityG;
        this.offBarcode = offBarcode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }
    public String getMealDescript() { return mealDescript; }
    public void setMealDescript(String mealDescript) { this.mealDescript = mealDescript; }
    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }
    public LocalDate getMealDate() { return mealDate; }
    public void setMealDate(LocalDate mealDate) { this.mealDate = mealDate; }
    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }
    public Double getProteinG() { return proteinG; }
    public void setProteinG(Double proteinG) { this.proteinG = proteinG; }
    public Double getCarbsG() { return carbsG; }
    public void setCarbsG(Double carbsG) { this.carbsG = carbsG; }
    public Double getFatG() { return fatG; }
    public void setFatG(Double fatG) { this.fatG = fatG; }
    public Double getQuantityG() { return quantityG; }
    public void setQuantityG(Double quantityG) { this.quantityG = quantityG; }
    public String getOffBarcode() { return offBarcode; }
    public void setOffBarcode(String offBarcode) { this.offBarcode = offBarcode; }
}
