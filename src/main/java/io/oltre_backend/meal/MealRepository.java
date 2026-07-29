package io.oltre_backend.meal;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {

    // Repas d'un jour précis
    List<Meal> findByMealDate(LocalDate mealDate);

    // Repas entre deux dates (pour la vue semaine)
    List<Meal> findByMealDateBetween(LocalDate start, LocalDate end);
}