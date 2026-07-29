package io.oltre_backend.meal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.oltre_backend.AbstractIntegrationTest;
import jakarta.transaction.Transactional;

@Transactional
class MealRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private MealRepository mealRepository;

    @Test
    void testSaveMeal() {
        Meal meal = new Meal();
        meal.setMealName("Pizza maison");
        meal.setMealDescript("Pizza avec tomate et mozzarella");
        meal.setMealType(MealType.DINER);
        meal.setMealDate(LocalDate.of(2026, 7, 29));

        Meal saved = mealRepository.save(meal);

        assertNotNull(saved.getId());
        assertEquals("Pizza maison", saved.getMealName());
        assertEquals(MealType.DINER, saved.getMealType());
        assertEquals(LocalDate.of(2026, 7, 29), saved.getMealDate());
    }

    @Test
    void testFindAllMeals() {
        Meal meal = new Meal();
        meal.setMealName("Salade César");
        meal.setMealDescript("Poulet salade parmesan");
        meal.setMealType(MealType.DEJEUNER);
        meal.setMealDate(LocalDate.now());

        mealRepository.save(meal);

        List<Meal> meals = mealRepository.findAll();

        assertFalse(meals.isEmpty());
        assertEquals("Salade César", meals.get(0).getMealName());
    }

    @Test
    void testFindMealById() {
        Meal meal = new Meal();
        meal.setMealName("Pâtes carbonara");
        meal.setMealType(MealType.DINER);
        meal.setMealDate(LocalDate.now());

        Meal saved = mealRepository.save(meal);

        Meal found = mealRepository.findById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals("Pâtes carbonara", found.getMealName());
    }

    @Test
    void testDeleteMeal() {
        Meal meal = new Meal();
        meal.setMealName("Burger");
        meal.setMealType(MealType.DINER);
        meal.setMealDate(LocalDate.now());

        Meal saved = mealRepository.save(meal);

        mealRepository.delete(saved);

        assertFalse(mealRepository.findById(saved.getId()).isPresent());
    }
}