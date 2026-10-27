package com.coms4156.mealplanningservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Locale;

/**
 * Response body returned by POST /v1/clients/{client_id}/recipes.
 *
 * Uses snake_case field names, like the client registration response.
 */
public final class RecipeResponse {

  /** The catalog recipe ID. */
  @JsonProperty("recipe_id")
  private final String recipeId;

  /** The meal type, in lowercase. */
  @JsonProperty("meal_type")
  private final String mealType;

  /** The URL of the original recipe. */
  @JsonProperty("url")
  private final String url;

  /** The recipe name. */
  @JsonProperty("name")
  private final String name;

  /** The recipe description. */
  @JsonProperty("description")
  private final String description;

  /** The ingredient names. */
  @JsonProperty("ingredients")
  private final List<String> ingredients;

  /** The total time to make the recipe, in minutes. */
  @JsonProperty("total_time_mins")
  private final int totalTimeMins;

  /**
   * Constructs a response.
   *
   * @param recipeId      the catalog recipe ID
   * @param mealType      the meal type
   * @param url           the recipe URL
   * @param name          the recipe name
   * @param description   the recipe description
   * @param ingredients   the ingredient names
   * @param totalTimeMins the total time in minutes
   */
  public RecipeResponse(
      final String recipeId,
      final String mealType,
      final String url,
      final String name,
      final String description,
      final List<String> ingredients,
      final int totalTimeMins) {
    this.recipeId = recipeId;
    this.mealType = mealType;
    this.url = url;
    this.name = name;
    this.description = description;
    this.ingredients = ingredients;
    this.totalTimeMins = totalTimeMins;
  }

  /**
   * Builds a response from a catalog recipe.
   *
   * @param recipe the catalog recipe
   * @return the response
   */
  public static RecipeResponse from(final CatalogRecipe recipe) {
    return new RecipeResponse(
        recipe.getId(),
        recipe.getCourse().toLowerCase(Locale.ROOT),
        recipe.getUrl(),
        recipe.getName(),
        recipe.getDescription(),
        recipe.ingredientList(),
        recipe.getTotalTimeMins());
  }

  /**
   * Gets the recipe ID.
   *
   * @return the recipe ID
   */
  public String getRecipeId() {
    return recipeId;
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
   * Gets the URL.
   *
   * @return the URL
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the ingredient names.
   *
   * @return the ingredient names
   */
  public List<String> getIngredients() {
    return ingredients;
  }

  /**
   * Gets the total time in minutes.
   *
   * @return the total time in minutes
   */
  public int getTotalTimeMins() {
    return totalTimeMins;
  }
}
