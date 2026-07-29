package io.oltre_backend.meal;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    private MealDTO toDto(Meal m) {
        return new MealDTO(
                m.getId(), m.getMealName(), m.getMealDescript(),
                m.getMealType(), m.getMealDate()
        );
    }

    // Repas d'un jour
    public List<MealDTO> getByDate(LocalDate date) {
        return mealRepository.findByMealDate(date)
                .stream().map(this::toDto).toList();
    }

    // Repas de toute une semaine (lundi → dimanche)
    public List<MealDTO> getWeek(LocalDate monday) {
        LocalDate sunday = monday.plusDays(6);
        return mealRepository.findByMealDateBetween(monday, sunday)
                .stream().map(this::toDto).toList();
    }

    // Sauvegarder
    public MealDTO save(MealDTO dto) {
        Meal meal = new Meal();
        meal.setMealName(dto.getMealName());
        meal.setMealDescript(dto.getMealDescript());
        meal.setMealType(dto.getMealType());
        meal.setMealDate(dto.getMealDate() != null ? dto.getMealDate() : LocalDate.now());
        return toDto(mealRepository.save(meal));
    }

    // Supprimer
    public void delete(Long id) {
        mealRepository.deleteById(id);
    }
}