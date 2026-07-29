package io.oltre_backend.meal;

import java.time.LocalDate;

public class MealDTO {

    private Long id;
    private String mealName;
    private String mealDescript;
    private MealType mealType;
    private LocalDate mealDate;

    public MealDTO() {}

    public MealDTO(Long id, String mealName, String mealDescript,
                   MealType mealType, LocalDate mealDate) {
        this.id = id;
        this.mealName = mealName;
        this.mealDescript = mealDescript;
        this.mealType = mealType;
        this.mealDate = mealDate;
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
}