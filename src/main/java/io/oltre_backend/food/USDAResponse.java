package io.oltre_backend.food;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class USDAResponse {
    private List<USDAFood> foods;
    // getters

    public USDAResponse(List<USDAFood> foods) {
        this.foods = foods;
    }

    public List<USDAFood> getFoods() {
        return foods;
    }

    public void setFoods(List<USDAFood> foods) {
        this.foods = foods;
    }
}
