package com.coms4156.mealplanningservice.model;

/** Represents a client in the Meal Planning Service. */
public final class Recipe {

  /** The recipe ID. */
  private String id;

  /** The recipe name. */
  private String name;

  /** The url for the recipe. */
  private String url;

  /** The recipe description. */
  private String description;

  /** The meal course (breakfast, lunch, dinner, etc). */
  private String course;

  /** The meal diet (vegetarian, high-protein, etc). */
  private String diet;

  /** The total time to make recipe. */
  private int totalTime;

  /** The recipe ingredients. */
  private String ingredients;

  /** The recipe instructions. */
  private String instructions;

  /** Default constructor. */
  public Recipe() {
    // This constructor is intentionally empty.
  }

  /**
   * Constructs a recipe.
   *
   * @param id           the recipe ID
   * @param name         the recipe name
   * @param url          the recipe url
   * @param description  the recipe description
   * @param course       the recipe meal course
   * @param diet         the recipe's diet type
   * @param totalTime    the total recipee time
   * @param ingredients  the recipe ingredients
   * @param instructions the recipe instructions
   */
  public Recipe(
      final String id,
      final String name,
      final String url,
      final String description,
      final String course,
      final String diet,
      final int totalTime,
      final String ingredients,
      final String instructions) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.description = description;
    this.course = course;
    this.diet = diet;
    this.totalTime = totalTime;
    this.ingredients = ingredients;
    this.instructions = instructions;
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
   * Gets the url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
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
   * Gets the course.
   *
   * @return the course
   */
  public String getCourse() {
    return course;
  }

  /**
   * Gets the diet.
   *
   * @return the diet
   */
  public String getDiet() {
    return diet;
  }

  /**
   * Gets the total recipe time.
   *
   * @return the total time
   */
  public int getTotalTime() {
    return totalTime;
  }

  /**
   * Gets the ingredients.
   *
   * @return the ingredients
   */
  public String getIngredients() {
    return ingredients;
  }

  /**
   * Gets the instructions.
   *
   * @return the instructions
   */
  public String getInstructions() {
    return instructions;
  }
}
