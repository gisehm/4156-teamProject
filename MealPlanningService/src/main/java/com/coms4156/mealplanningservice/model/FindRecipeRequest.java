package com.coms4156.mealplanningservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request body for finding a recipe
 * (POST /v1/clients/{client_id}/recipes).
 */
public final class FindRecipeRequest {

  /** The meal type to match, such as breakfast, lunch or dinner. */
  @JsonProperty("meal_type")
  private String mealType;

  /** The calorie limit; accepted but not enforced until nutrition exists. */
  @JsonProperty("max_calories")
  private Integer maxCalories;

  /** The diet tags that the recipe must satisfy. */
  @JsonProperty("diet_type")
  private List<String> dietTypes;

  /** The allergen tags that the recipe must not contain. */
  @JsonProperty("allergens")
  private List<String> allergens;

  /** The maximum total preparation time in minutes. */
  @JsonProperty("max_prep_time_mins")
  private Integer maxPrepTimeMins;

  /** Default constructor. */
  public FindRecipeRequest() {
    // This constructor is intentionally empty.
  }

  /**
   * Constructs a request.
   *
   * @param mealType        the meal type to match
   * @param maxCalories     the calorie limit, may be null
   * @param dietTypes       the diet tags, may be null
   * @param allergens       the allergen tags, may be null
   * @param maxPrepTimeMins the preparation time limit, may be null
   */
  public FindRecipeRequest(
      final String mealType,
      final Integer maxCalories,
      final List<String> dietTypes,
      final List<String> allergens,
      final Integer maxPrepTimeMins) {
    this.mealType = mealType;
    this.maxCalories = maxCalories;
    this.dietTypes = dietTypes;
    this.allergens = allergens;
    this.maxPrepTimeMins = maxPrepTimeMins;
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
   * Gets the calorie limit.
   *
   * @return the calorie limit, or null if not given
   */
  public Integer getMaxCalories() {
    return maxCalories;
  }

  /**
   * Sets the calorie limit.
   *
   * @param maxCalories the calorie limit
   */
  public void setMaxCalories(final Integer maxCalories) {
    this.maxCalories = maxCalories;
  }

  /**
   * Gets the diet tags.
   *
   * @return the diet tags, or null if not given
   */
  public List<String> getDietTypes() {
    return dietTypes;
  }

  /**
   * Sets the diet tags.
   *
   * @param dietTypes the diet tags
   */
  public void setDietTypes(final List<String> dietTypes) {
    this.dietTypes = dietTypes;
  }

  /**
   * Gets the allergen tags.
   *
   * @return the allergen tags, or null if not given
   */
  public List<String> getAllergens() {
    return allergens;
  }

  /**
   * Sets the allergen tags.
   *
   * @param allergens the allergen tags
   */
  public void setAllergens(final List<String> allergens) {
    this.allergens = allergens;
  }

  /**
   * Gets the preparation time limit.
   *
   * @return the preparation time limit in minutes, or null if not given
   */
  public Integer getMaxPrepTimeMins() {
    return maxPrepTimeMins;
  }

  /**
   * Sets the preparation time limit.
   *
   * @param maxPrepTimeMins the preparation time limit in minutes
   */
  public void setMaxPrepTimeMins(final Integer maxPrepTimeMins) {
    this.maxPrepTimeMins = maxPrepTimeMins;
  }
}
