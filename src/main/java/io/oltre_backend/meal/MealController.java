package io.oltre_backend.meal;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.oltre_backend.auth.CurrentUser;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping("/day")
    public List<MealDTO> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return mealService.getByDate(date, CurrentUser.id());
    }

    @GetMapping("/week")
    public List<MealDTO> getWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monday) {
        return mealService.getWeek(monday, CurrentUser.id());
    }

    @PostMapping("/save")
    public MealDTO save(@RequestBody MealDTO dto) {
        return mealService.save(dto, CurrentUser.id());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mealService.delete(id, CurrentUser.id());
    }
}