package io.oltre_backend.openfoodfacts;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/food")
public class FoodSearchController {

    private final OpenFoodFactsService openFoodFactsService;

    public FoodSearchController(OpenFoodFactsService openFoodFactsService) {
        this.openFoodFactsService = openFoodFactsService;
    }

    @GetMapping("/search")
    public List<OpenFoodFactsProductDTO> search(@RequestParam String q) {
        return openFoodFactsService.search(q);
    }
}
