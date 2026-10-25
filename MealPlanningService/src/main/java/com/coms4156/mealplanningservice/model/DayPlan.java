package com.coms4156.mealplanningservice.model;

import java.util.ArrayList;
import java.util.List;

/** Represents the meals planned for one day of a meal plan. */
public final class DayPlan {

  /** The name of the day (monday, tuesday, etc). */
  private String day;

  /** The meals planned for this day. */
  private List<PlannedMeal> meals = new ArrayList<>();

  /** Default constructor. */
  public DayPlan() {
    // This constructor is intentionally empty.
  }

  /**
   * Constructs a day plan.
   *
   * @param day   the name of the day
   * @param meals the meals planned for this day
   */
  public DayPlan(final String day, final List<PlannedMeal> meals) {
    this.day = day;
    this.meals = meals;
  }

  /**
   * Gets the day.
   *
   * @return the name of the day
   */
  public String getDay() {
    return day;
  }

  /**
   * Sets the day.
   *
   * @param day the name of the day
   */
  public void setDay(final String day) {
    this.day = day;
  }

  /**
   * Gets the meals.
   *
   * @return the meals planned for this day
   */
  public List<PlannedMeal> getMeals() {
    return meals;
  }

  /**
   * Sets the meals.
   *
   * @param meals the meals planned for this day
   */
  public void setMeals(final List<PlannedMeal> meals) {
    this.meals = meals;
  }
}
