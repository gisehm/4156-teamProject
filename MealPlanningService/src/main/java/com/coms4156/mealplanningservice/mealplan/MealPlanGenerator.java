package com.coms4156.mealplanningservice.mealplan;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements the proposal's deterministic, sequential meal-selection algorithm.
 *
 * This class only schedules candidates that the caller has already checked
 * for client access, diet, allergens, and preparation-time limits. It performs
 * no database writes or external API calls. The caller must generate the entire
 * schedule before saving recipes and the plan in one transaction.
 */
public final class MealPlanGenerator {
  /** Minimum fraction of the daily calorie cap that a day must provide. */
  private static final BigDecimal MIN_CALORIE_FRACTION = new BigDecimal("0.8");

  /** Orders each meal slot by preparation time and source ID. */
  private static final Comparator<PlanningRecipe> RECIPE_ORDER = Comparator
      .comparingInt(PlanningRecipe::totalTimeMinutes)
      .thenComparing(PlanningRecipe::sourceRecipeId);

  /**
   * Generates a day or week, choosing each day's first valid
   * breakfast/lunch/dinner combination.
   *
   * Schedules start on Monday. The one-day label and the source-ID tie-break
   * for equal preparation times are explicit defaults where the proposal does
   * not specify a rule. Previous days are never reconsidered: this is the
   * proposed greedy algorithm, not a global weekly optimization.
   *
   * @param eligibleRecipes  the client's eligible library/catalog union, using
   *                         stable source IDs
   * @param numberOfDays     1 for a day or 7 for a week
   * @param maxDailyCalories the positive daily calorie cap
   * @return an immutable list of complete days in chronological order
   * @throws IllegalArgumentException    if inputs are invalid or duplicate
   *                                     source data conflicts
   * @throws UnfillableMealPlanException if a day cannot satisfy the calorie and
   *                                     repetition rules
   */
  public List<PlannedDay> generate(
      final List<PlanningRecipe> eligibleRecipes, final int numberOfDays,
      final BigDecimal maxDailyCalories) {
    if (eligibleRecipes == null) {
      throw new IllegalArgumentException("Eligible recipes are required.");
    }
    if (numberOfDays != 1 && numberOfDays != 7) {
      throw new IllegalArgumentException(
          "A meal plan must contain 1 or 7 days.");
    }
    if (maxDailyCalories == null || maxDailyCalories.signum() <= 0) {
      throw new IllegalArgumentException(
          "The daily calorie cap must be positive.");
    }

    Map<MealType, List<PlanningRecipe>> recipesByType = groupRecipes(
        eligibleRecipes);
    BigDecimal minimumCalories = maxDailyCalories
        .multiply(MIN_CALORIE_FRACTION);
    List<PlannedDay> schedule = new ArrayList<>();

    for (int dayIndex = 0; dayIndex < numberOfDays; dayIndex++) {
      DayOfWeek day = DayOfWeek.MONDAY.plus(dayIndex);
      Set<String> blockedSourceIds = recentlyUsedSourceIds(schedule);
      schedule.add(
          chooseDay(day, recipesByType, blockedSourceIds, minimumCalories,
              maxDailyCalories));
    }
    return List.copyOf(schedule);
  }

  private Map<MealType, List<PlanningRecipe>> groupRecipes(
      final List<PlanningRecipe> candidates) {
    Map<String, PlanningRecipe> recipesBySource = new LinkedHashMap<>();
    for (PlanningRecipe recipe : candidates) {
      if (recipe == null) {
        throw new IllegalArgumentException(
            "An eligible recipe cannot be null.");
      }
      PlanningRecipe previous = recipesBySource
          .putIfAbsent(recipe.sourceRecipeId(), recipe);
      if (previous != null && !hasSamePlanningData(previous, recipe)) {
        throw new IllegalArgumentException(
            "Conflicting data for source recipe "
                + recipe.sourceRecipeId() + ".");
      }
    }

    Map<MealType, List<PlanningRecipe>> groups = new EnumMap<>(MealType.class);
    for (MealType type : MealType.values()) {
      groups.put(type, new ArrayList<>());
    }
    for (PlanningRecipe recipe : recipesBySource.values()) {
      groups.get(recipe.mealType()).add(recipe);
    }
    for (List<PlanningRecipe> group : groups.values()) {
      group.sort(RECIPE_ORDER);
    }
    return groups;
  }

  private boolean hasSamePlanningData(
      final PlanningRecipe first, final PlanningRecipe second) {
    return first.mealType() == second.mealType()
        && first.totalTimeMinutes() == second.totalTimeMinutes()
        && first.calories().compareTo(second.calories()) == 0;
  }

  private Set<String> recentlyUsedSourceIds(final List<PlannedDay> schedule) {
    Set<String> blockedSourceIds = new HashSet<>();
    for (int index = Math
        .max(0, schedule.size() - 2); index < schedule.size(); index++) {
      for (PlanningRecipe recipe : schedule.get(index).meals()) {
        blockedSourceIds.add(recipe.sourceRecipeId());
      }
    }
    return blockedSourceIds;
  }

  private PlannedDay chooseDay(
      final DayOfWeek day,
      final Map<MealType, List<PlanningRecipe>> recipesByType,
      final Set<String> blockedSourceIds,
      final BigDecimal minimumCalories,
      final BigDecimal maximumCalories) {
    for (PlanningRecipe breakfast : recipesByType.get(MealType.BREAKFAST)) {
      if (blockedSourceIds.contains(breakfast.sourceRecipeId())) {
        continue;
      }
      for (PlanningRecipe lunch : recipesByType.get(MealType.LUNCH)) {
        if (blockedSourceIds.contains(lunch.sourceRecipeId())) {
          continue;
        }
        for (PlanningRecipe dinner : recipesByType.get(MealType.DINNER)) {
          if (blockedSourceIds.contains(dinner.sourceRecipeId())) {
            continue;
          }
          BigDecimal total = breakfast.calories()
              .add(lunch.calories()).add(dinner.calories());
          if (total.compareTo(minimumCalories) >= 0
              && total.compareTo(maximumCalories) <= 0) {
            return new PlannedDay(day, breakfast, lunch, dinner);
          }
        }
      }
    }
    throw new UnfillableMealPlanException(day);
  }
}
