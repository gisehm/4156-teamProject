package com.coms4156.mealplanningservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;

/**
 * A recipe from the catalog file (food_recipes.json), with the same fields
 * as the dataset.
 */
public final class CatalogRecipe {

  /** The catalog recipe ID. */
  private String id;

  /** The recipe name. */
  private String name;

  /** The URL of the original recipe. */
  private String url;

  /** The recipe description. */
  private String description;

  /** The meal course, such as Breakfast, Lunch or Dinner. */
  private String course;

  /** The dataset's diet label, such as Vegetarian or Non Vegeterian. */
  private String diet;

  /** The total time to make the recipe, in minutes. */
  @JsonProperty("total_time")
  private int totalTimeMins;

  /** The ingredient names, separated by "|". */
  private String ingredients;

  /** The recipe instructions. */
  private String instructions;

  /** Default constructor. */
  public CatalogRecipe() {
    // This constructor is intentionally empty.
  }

  /**
   * Splits the ingredients field into individual ingredient names.
   *
   * @return the trimmed ingredient names, or an empty list if there are none
   */
  public List<String> ingredientList() {
    if (ingredients == null || ingredients.isBlank()) {
      return List.of();
    }
    return Arrays.stream(ingredients.split("\\|"))
        .map(String::trim)
        .filter(item -> !item.isEmpty())
        .toList();
  }

  /**
   * Gets the ID.
   *
   * @return the ID
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the ID.
   *
   * @param id the ID
   */
  public void setId(final String id) {
    this.id = id;
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
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(final String name) {
    this.name = name;
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
   * Sets the URL.
   *
   * @param url the URL
   */
  public void setUrl(final String url) {
    this.url = url;
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
   * Sets the description.
   *
   * @param description the description
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * Gets the meal course.
   *
   * @return the meal course
   */
  public String getCourse() {
    return course;
  }

  /**
   * Sets the meal course.
   *
   * @param course the meal course
   */
  public void setCourse(final String course) {
    this.course = course;
  }

  /**
   * Gets the diet label.
   *
   * @return the diet label
   */
  public String getDiet() {
    return diet;
  }

  /**
   * Sets the diet label.
   *
   * @param diet the diet label
   */
  public void setDiet(final String diet) {
    this.diet = diet;
  }

  /**
   * Gets the total time in minutes.
   *
   * @return the total time in minutes
   */
  public int getTotalTimeMins() {
    return totalTimeMins;
  }

  /**
   * Sets the total time in minutes.
   *
   * @param totalTimeMins the total time in minutes
   */
  public void setTotalTimeMins(final int totalTimeMins) {
    this.totalTimeMins = totalTimeMins;
  }

  /**
   * Gets the ingredients field.
   *
   * @return the ingredient names separated by "|"
   */
  public String getIngredients() {
    return ingredients;
  }

  /**
   * Sets the ingredients field.
   *
   * @param ingredients the ingredient names separated by "|"
   */
  public void setIngredients(final String ingredients) {
    this.ingredients = ingredients;
  }

  /**
   * Gets the instructions.
   *
   * @return the instructions
   */
  public String getInstructions() {
    return instructions;
  }

  /**
   * Sets the instructions.
   *
   * @param instructions the instructions
   */
  public void setInstructions(final String instructions) {
    this.instructions = instructions;
  }
}
