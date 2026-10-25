package com.coms4156.mealplanningservice.model;

import java.util.ArrayList;
import java.util.List;

/** Represents a saved meal plan that belongs to one client. */
public final class MealPlan {

  /** The meal plan ID. */
  private String id;

  /** The ID of the client that owns this meal plan. */
  private String clientId;

  /** The time frame the plan covers (day or week). */
  private String timeFrame;

  /** The diets every recipe in the plan satisfies. */
  private List<String> diet = new ArrayList<>();

  /** The allergens no recipe in the plan contains. */
  private List<String> allergens = new ArrayList<>();

  /** The meals planned for each day, in order. */
  private List<DayPlan> schedule = new ArrayList<>();

  /** Default constructor. */
  public MealPlan() {
    // This constructor is intentionally empty.
  }

  /**
   * Constructs a meal plan.
   *
   * @param id        the meal plan ID
   * @param clientId  the ID of the owning client
   * @param timeFrame the time frame the plan covers
   * @param diet      the diets the plan satisfies
   * @param allergens the allergens the plan avoids
   * @param schedule  the meals planned for each day
   */
  public MealPlan(
      final String id,
      final String clientId,
      final String timeFrame,
      final List<String> diet,
      final List<String> allergens,
      final List<DayPlan> schedule) {
    this.id = id;
    this.clientId = clientId;
    this.timeFrame = timeFrame;
    this.diet = diet;
    this.allergens = allergens;
    this.schedule = schedule;
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
   * Gets the client ID.
   *
   * @return the ID of the owning client
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Sets the client ID.
   *
   * @param clientId the ID of the owning client
   */
  public void setClientId(final String clientId) {
    this.clientId = clientId;
  }

  /**
   * Gets the time frame.
   *
   * @return the time frame the plan covers
   */
  public String getTimeFrame() {
    return timeFrame;
  }

  /**
   * Sets the time frame.
   *
   * @param timeFrame the time frame the plan covers
   */
  public void setTimeFrame(final String timeFrame) {
    this.timeFrame = timeFrame;
  }

  /**
   * Gets the diets.
   *
   * @return the diets the plan satisfies
   */
  public List<String> getDiet() {
    return diet;
  }

  /**
   * Sets the diets.
   *
   * @param diet the diets the plan satisfies
   */
  public void setDiet(final List<String> diet) {
    this.diet = diet;
  }

  /**
   * Gets the allergens.
   *
   * @return the allergens the plan avoids
   */
  public List<String> getAllergens() {
    return allergens;
  }

  /**
   * Sets the allergens.
   *
   * @param allergens the allergens the plan avoids
   */
  public void setAllergens(final List<String> allergens) {
    this.allergens = allergens;
  }

  /**
   * Gets the schedule.
   *
   * @return the meals planned for each day
   */
  public List<DayPlan> getSchedule() {
    return schedule;
  }

  /**
   * Sets the schedule.
   *
   * @param schedule the meals planned for each day
   */
  public void setSchedule(final List<DayPlan> schedule) {
    this.schedule = schedule;
  }
}
