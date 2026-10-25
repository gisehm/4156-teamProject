package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coms4156.mealplanningservice.model.DayPlan;
import com.coms4156.mealplanningservice.model.MealPlan;
import com.coms4156.mealplanningservice.model.PlannedMeal;
import com.coms4156.mealplanningservice.model.Recipe;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests that a meal plan survives being written to and read from JSON. */
class MealPlanModelTests {

  private ObjectMapper objectMapper;

  private MealPlan plan;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    Recipe recipe =
        new Recipe(
            "recipe-1",
            "Veg Pulao",
            "https://example.com/veg-pulao",
            "A simple rice dish",
            "Lunch",
            "Vegetarian",
            45,
            "Rice|Carrot|Peas",
            "Cook the rice|Add the vegetables");
    DayPlan monday =
        new DayPlan("monday", List.of(new PlannedMeal("lunch", recipe)));
    plan =
        new MealPlan(
            "plan-1",
            "client-1",
            "week",
            List.of("Vegetarian"),
            List.of("peanuts"),
            List.of(monday));
  }

  @Test
  void mealPlanRoundTripsThroughJson() throws Exception {
    String json = objectMapper.writeValueAsString(plan);

    MealPlan loaded = objectMapper.readValue(json, MealPlan.class);

    assertEquals("plan-1", loaded.getId());
    assertEquals("client-1", loaded.getClientId());
    assertEquals("week", loaded.getTimeFrame());
    assertEquals(List.of("Vegetarian"), loaded.getDiet());
    assertEquals(List.of("peanuts"), loaded.getAllergens());
    assertEquals(1, loaded.getSchedule().size());
  }

  @Test
  void roundTripKeepsTheRecipeInsideTheMeal() throws Exception {
    String json = objectMapper.writeValueAsString(plan);

    MealPlan loaded = objectMapper.readValue(json, MealPlan.class);

    DayPlan day = loaded.getSchedule().get(0);
    assertEquals("monday", day.getDay());
    PlannedMeal meal = day.getMeals().get(0);
    assertEquals("lunch", meal.getMealType());
    assertNotNull(meal.getRecipe());
    assertEquals("Veg Pulao", meal.getRecipe().getName());
    assertEquals(45, meal.getRecipe().getTotalTimeMins());
  }

  @Test
  void newMealPlanStartsWithEmptyLists() {
    MealPlan empty = new MealPlan();

    assertTrue(empty.getDiet().isEmpty());
    assertTrue(empty.getAllergens().isEmpty());
    assertTrue(empty.getSchedule().isEmpty());
  }
}
