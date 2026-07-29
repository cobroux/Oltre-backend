package io.oltre_backend.meal;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meal_name", nullable = false)
    private String mealName;

    @Column(name = "meal_descript")
    private String mealDescript;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type")
    private MealType mealType;

    @Column(name = "meal_date")
    private LocalDate mealDate;

    public Meal() {}

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