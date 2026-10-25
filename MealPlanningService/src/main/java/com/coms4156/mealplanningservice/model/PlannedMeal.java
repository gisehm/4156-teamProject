package com.coms4156.mealplanningservice.model;

/** Represents one meal in a day of a meal plan. */
public final class PlannedMeal {

  /** The meal type (breakfast, lunch, dinner, etc). */
  private String mealType;

  /** The recipe planned for this meal. */
  private Recipe recipe;

  /** Default constructor. */
  public PlannedMeal() {
    // This constructor is intentionally empty.
  }

  /**
   * Constructs a planned meal.
   *
   * @param mealType the meal type
   * @param recipe   the recipe planned for this meal
   */
  public PlannedMeal(final String mealType, final Recipe recipe) {
    this.mealType = mealType;
    this.recipe = recipe;
  }

  /**
   * Gets the meal type.
   *
   * @return the meal type
   */
  public String getMealType() {
    return mealType;
  }

  /**
   * Sets the meal type.
   *
   * @param mealType the meal type
   */
  public void setMealType(final String mealType) {
    this.mealType = mealType;
  }

  /**
   * Gets the recipe.
   *
   * @return the recipe
   */
  public Recipe getRecipe() {
    return recipe;
  }

  /**
   * Sets the recipe.
   *
   * @param recipe the recipe
   */
  public void setRecipe(final Recipe recipe) {
    this.recipe = recipe;
  }
}
