package io.oltre_backend.food;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class USDAFood {
    private String description;
    private List<USDANutrient> foodNutrients;
    // getters

    public USDAFood(String description, List<USDANutrient> foodNutrients) {
        this.description = description;
        this.foodNutrients = foodNutrients;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<USDANutrient> getFoodNutrients() {
        return foodNutrients;
    }

    public void setFoodNutrients(List<USDANutrient> foodNutrients) {
        this.foodNutrients = foodNutrients;
    }
}
