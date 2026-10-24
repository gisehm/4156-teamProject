package com.coms4156.mealplanningservice.controller;

import com.coms4156.mealplanningservice.service.MealPlanningService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the Meal Planning Service. */
@RestController
@RequestMapping("/v1")
public final class ApiController {

  /** The meal planning service. */
  private final MealPlanningService mealPlanningService;

  /**
   * Creates a new ApiController with the meal planning service.
   *
   * @param mealPlanningService the meal planning service
   */
  public ApiController(final MealPlanningService mealPlanningService) {
    this.mealPlanningService = mealPlanningService;
  }
}
