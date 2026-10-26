package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coms4156.mealplanningservice.mealplan.MealType;
import com.coms4156.mealplanningservice.mealplan.PlannedDay;
import com.coms4156.mealplanningservice.mealplan.PlanningRecipe;
import com.coms4156.mealplanningservice.model.MealPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests that a meal plan survives being written to and read from JSON. */
class MealPlanModelTests {

  private ObjectMapper objectMapper;

  private PlannedDay monday;

  private MealPlan plan;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    monday =
        new PlannedDay(
            DayOfWeek.MONDAY,
            new PlanningRecipe(
                "recipe-1", MealType.BREAKFAST, new BigDecimal("400"), 10),
            new PlanningRecipe(
                "recipe-2", MealType.LUNCH, new BigDecimal("600"), 20),
            new PlanningRecipe(
                "recipe-3", MealType.DINNER, new BigDecimal("700"), 30));
    plan =
        new MealPlan(
            "plan-1",
            "client-1",
            "week",
            List.of("Vegetarian"),
            List.of("peanuts"),
            1800,
            40,
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
  void roundTripKeepsEveryMealOfTheDay() throws Exception {
    String json = objectMapper.writeValueAsString(plan);

    MealPlan loaded = objectMapper.readValue(json, MealPlan.class);

    PlannedDay day = loaded.getSchedule().get(0);
    assertEquals(monday, day);
    assertEquals(DayOfWeek.MONDAY, day.day());
    assertEquals("recipe-2", day.lunch().sourceRecipeId());
    assertEquals(MealType.DINNER, day.dinner().mealType());
    assertEquals(30, day.dinner().totalTimeMinutes());
  }

  @Test
  void roundTripKeepsTheCaloriesNeededToRecheckADay() throws Exception {
    String json = objectMapper.writeValueAsString(plan);

    MealPlan loaded = objectMapper.readValue(json, MealPlan.class);

    PlannedDay day = loaded.getSchedule().get(0);
    assertEquals(0, new BigDecimal("1700").compareTo(day.totalCalories()));
  }

  @Test
  void roundTripKeepsTheConstraintsUsedToBuildThePlan() throws Exception {
    String json = objectMapper.writeValueAsString(plan);

    MealPlan loaded = objectMapper.readValue(json, MealPlan.class);

    assertEquals(1800, loaded.getMaxCalories());
    assertEquals(40, loaded.getMaxPrepTimeMins());
  }

  @Test
  void planWithoutLimitsKeepsThemNullAfterRoundTrip() throws Exception {
    MealPlan unlimited = new MealPlan();
    unlimited.setId("plan-2");

    MealPlan loaded =
        objectMapper.readValue(
            objectMapper.writeValueAsString(unlimited), MealPlan.class);

    assertNull(loaded.getMaxCalories());
    assertNull(loaded.getMaxPrepTimeMins());
  }

  @Test
  void newMealPlanStartsWithEmptyLists() {
    MealPlan empty = new MealPlan();

    assertTrue(empty.getDiet().isEmpty());
    assertTrue(empty.getAllergens().isEmpty());
    assertTrue(empty.getSchedule().isEmpty());
  }
}
