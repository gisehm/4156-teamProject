package com.coms4156.mealplanningservice.mealplan;

import java.math.BigDecimal;

/**
 * The information the scheduler needs about an already eligible recipe.
 *
 * <p>The source ID must remain the same when a catalog recipe is saved in a
 * client's library. It is not the ID of that client-specific copy. The caller
 * supplies per-serving calories and performs client ownership, diet, allergen,
 * and preparation-time filtering before scheduling.
 *
 * @param sourceRecipeId stable identity of the underlying recipe
 * @param mealType the recipe's meal slot
 * @param calories computed calories per serving, without rounding by this
 *     scheduler
 * @param totalTimeMinutes total preparation and cooking time in minutes
 */
public record PlanningRecipe(
    String sourceRecipeId, MealType mealType, BigDecimal calories,
    int totalTimeMinutes) {

  /**
   * Validates the values required to compare and schedule a recipe.
   *
   * @throws IllegalArgumentException if an identity or meal type is missing, or
   *     a number is invalid
   */
  public PlanningRecipe {
    if (sourceRecipeId == null || sourceRecipeId.isBlank()) {
      throw new IllegalArgumentException("A source recipe ID is required.");
    }
    if (mealType == null) {
      throw new IllegalArgumentException("A meal type is required.");
    }
    if (calories == null || calories.signum() < 0) {
      throw new IllegalArgumentException(
          "Recipe calories must be non-negative.");
    }
    if (totalTimeMinutes < 0) {
      throw new IllegalArgumentException(
          "Recipe preparation time must be non-negative.");
    }
  }
}
