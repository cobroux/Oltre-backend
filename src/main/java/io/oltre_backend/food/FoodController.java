package io.oltre_backend.food;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/food")
public class FoodController {

    private final FoodService foodService;


     public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping("/search")
    public List<FoodDTO> search(@RequestParam String query) {
        return foodService.search(query);
    }
}