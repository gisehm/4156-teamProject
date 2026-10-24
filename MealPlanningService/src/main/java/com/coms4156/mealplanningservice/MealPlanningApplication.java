package com.coms4156.mealplanningservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Main application class for the Meal Planning Service. */
@SpringBootApplication
public final class MealPlanningApplication {

  /** Private constructor to prevent instantiation. */
  private MealPlanningApplication() {
  }

  /**
   * Application entry point.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    SpringApplication.run(MealPlanningApplication.class, args);
  }
}
