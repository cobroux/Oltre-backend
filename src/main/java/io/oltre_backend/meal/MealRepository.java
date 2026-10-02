package io.oltre_backend.meal;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {

    // Repas d'un jour précis
    List<Meal> findByMealDateAndUser_Id(LocalDate mealDate, Long userId);

    // Repas entre deux dates (pour la vue semaine)
    List<Meal> findByMealDateBetweenAndUser_Id(LocalDate start, LocalDate end, Long userId);
}