package com.coms4156.mealplanningservice.mealplan;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

/**
 * One complete day selected by the scheduler.
 *
 * @param day the schedule's day label
 * @param breakfast the selected breakfast
 * @param lunch the selected lunch
 * @param dinner the selected dinner
 */
public record PlannedDay(
    DayOfWeek day, PlanningRecipe breakfast, PlanningRecipe lunch,
    PlanningRecipe dinner) {

  /**
   * Requires a day label and exactly one recipe of the correct type for each
   * slot.
   *
   * @throws IllegalArgumentException if the label or any meal slot is invalid
   */
  public PlannedDay {
    if (day == null) {
      throw new IllegalArgumentException("A schedule day is required.");
    }
    requireMealType(breakfast, MealType.BREAKFAST);
    requireMealType(lunch, MealType.LUNCH);
    requireMealType(dinner, MealType.DINNER);
  }

  /**
   * Returns the meals in the response's required breakfast, lunch, dinner
   * order.
   *
   * @return an immutable, ordered list of the three selected meals
   */
  public List<PlanningRecipe> meals() {
    return List.of(breakfast, lunch, dinner);
  }

  /**
   * Calculates the day's exact calorie sum without rounding.
   *
   * @return the sum of all three meals' calories
   */
  public BigDecimal totalCalories() {
    return breakfast.calories().add(lunch.calories()).add(dinner.calories());
  }

  private static void requireMealType(
      final PlanningRecipe recipe, final MealType expectedType) {
    if (recipe == null || recipe.mealType() != expectedType) {
      throw new IllegalArgumentException(
          "A " + expectedType + " recipe is required for this slot.");
    }
  }
}
