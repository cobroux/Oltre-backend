package io.oltre_backend.meal;

import java.time.LocalDate;
import jakarta.persistence.*;

import io.oltre_backend.user.User;

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

    // Nutrition optionnelle : renseignée quand le repas est rattaché à un
    // produit OpenFoodFacts (saisie libre sinon, tous ces champs restent
    // null).
    @Column(name = "calories")
    private Integer calories;

    @Column(name = "protein_g")
    private Double proteinG;

    @Column(name = "carbs_g")
    private Double carbsG;

    @Column(name = "fat_g")
    private Double fatG;

    @Column(name = "quantity_g")
    private Double quantityG;

    @Column(name = "off_barcode")
    private String offBarcode;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

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
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}