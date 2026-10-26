package com.coms4156.mealplanningservice.service;

import com.coms4156.mealplanningservice.mealplan.MealType;
import com.coms4156.mealplanningservice.mealplan.PlanningRecipe;
import com.coms4156.mealplanningservice.model.CreateMealPlanRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Explicitly synthetic local candidates, not enriched real catalog recipes.
 *
 * <p>Every fixture has 500 calories. Diet and allergen facts are preset test
 * metadata, not nutrition calculations or verified food safety information.
 */
@Service
@Profile("local & mealplan-demo")
public final class LocalMealPlanCandidates {

  /** Diets assigned to the synthetic plant-based fixtures. */
  private static final Set<String> PLANT_DIETS = Set.of(
      "vegetarian", "vegan", "low_fat", "high_protein");

  /**
   * Filters fixtures before passing their minimal facts to the generator.
   *
   * @param request validated restrictions
   * @return eligible recipes; no client library or catalog is loaded
   */
  public List<PlanningRecipe> findEligible(
      final CreateMealPlanRequest request) {
    List<PlanningRecipe> eligible = new ArrayList<>();
    for (MealType type : MealType.values()) {
      String name = type.name().toLowerCase(Locale.ROOT);
      for (int index = 0; index < 3; index++) {
        addIfEligible(eligible, request,
            new PlanningRecipe("demo-" + name + "-" + index, type,
                new BigDecimal("500"), 10 + index),
            PLANT_DIETS, Set.of());
      }
      addIfEligible(eligible, request,
          new PlanningRecipe("demo-" + name + "-dairy", type,
              new BigDecimal("500"), 1),
          Set.of("vegetarian"), Set.of("dairy"));
    }
    return List.copyOf(eligible);
  }

  private static void addIfEligible(
      final List<PlanningRecipe> result,
      final CreateMealPlanRequest request, final PlanningRecipe recipe,
      final Set<String> diets, final Set<String> allergens) {
    if (recipe.totalTimeMinutes() <= request.maxPrepTimeMins()
        && diets.containsAll(request.dietTypes())
        && Collections.disjoint(allergens, request.allergens())) {
      result.add(recipe);
    }
  }
}
