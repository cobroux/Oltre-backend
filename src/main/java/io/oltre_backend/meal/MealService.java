package io.oltre_backend.meal;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.oltre_backend.user.UserRepository;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final UserRepository userRepository;

    public MealService(MealRepository mealRepository, UserRepository userRepository) {
        this.mealRepository = mealRepository;
        this.userRepository = userRepository;
    }

    private MealDTO toDto(Meal m) {
        return new MealDTO(
                m.getId(), m.getMealName(), m.getMealDescript(),
                m.getMealType(), m.getMealDate()
        );
    }

    // Repas d'un jour
    public List<MealDTO> getByDate(LocalDate date, Long userId) {
        return mealRepository.findByMealDateAndUser_Id(date, userId)
                .stream().map(this::toDto).toList();
    }

    // Repas de toute une semaine (lundi → dimanche)
    public List<MealDTO> getWeek(LocalDate monday, Long userId) {
        LocalDate sunday = monday.plusDays(6);
        return mealRepository.findByMealDateBetweenAndUser_Id(monday, sunday, userId)
                .stream().map(this::toDto).toList();
    }

    // Sauvegarder
    public MealDTO save(MealDTO dto, Long userId) {
        Meal meal = new Meal();
        meal.setMealName(dto.getMealName());
        meal.setMealDescript(dto.getMealDescript());
        meal.setMealType(dto.getMealType());
        meal.setMealDate(dto.getMealDate() != null ? dto.getMealDate() : LocalDate.now());
        meal.setUser(userRepository.getReferenceById(userId));
        return toDto(mealRepository.save(meal));
    }

    // Supprimer
    public void delete(Long id, Long userId) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (meal.getUser() == null || !meal.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        mealRepository.deleteById(id);
    }
}