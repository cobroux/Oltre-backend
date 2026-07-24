package io.oltre_backend.food;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class USDANutrient {
    private int nutrientId;
    private double value;
    // getters

    public USDANutrient(int nutrientId, double value) {
        this.nutrientId = nutrientId;
        this.value = value;
    }

    public int getNutrientId() {
        return nutrientId;
    }

    public void setNutrientId(int nutrientId) {
        this.nutrientId = nutrientId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}