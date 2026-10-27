package com.coms4156.mealplanningservice.mealplan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests scheduling rules; HTTP, candidate filtering, and persistence need
 * separate tests.
 */
class MealPlanGeneratorTest {

  private final MealPlanGenerator generator = new MealPlanGenerator();

  @ParameterizedTest
  @CsvSource({ "1439, false", "1440, true", "1800, true", "1801, false" })
  void acceptsOnlyTheInclusiveDailyCalorieBand(String total, boolean feasible) {
    BigDecimal dinnerCalories = new BigDecimal(total).subtract(new BigDecimal("1000"));
    List<PlanningRecipe> candidates = List.of(
        recipe("breakfast", MealType.BREAKFAST, "500", 10),
        recipe("lunch", MealType.LUNCH, "500", 10),
        recipe("dinner", MealType.DINNER, dinnerCalories.toPlainString(), 10));

    if (feasible) {
      List<PlannedDay> schedule = generator.generate(candidates, 1, new BigDecimal("1800"));

      assertEquals(1, schedule.size());
      assertEquals(DayOfWeek.MONDAY, schedule.get(0).day());
      assertEquals(0, new BigDecimal(total).compareTo(schedule.get(0).totalCalories()));
    } else {
      UnfillableMealPlanException failure = assertThrows(
          UnfillableMealPlanException.class,
          () -> generator.generate(candidates, 1, new BigDecimal("1800")));

      assertEquals(DayOfWeek.MONDAY, failure.failedDay());
    }
  }

  @ParameterizedTest
  @CsvSource({ "0.0799999999, false", "0.08, true", "0.14, true", "0.1400000001, false" })
  void comparesFractionalCaloriesWithoutRounding(String dinnerCalories, boolean feasible) {
    List<PlanningRecipe> candidates = List.of(
        recipe("breakfast", MealType.BREAKFAST, "0.08", 10),
        recipe("lunch", MealType.LUNCH, "0.08", 10),
        recipe("dinner", MealType.DINNER, dinnerCalories, 10));

    if (feasible) {
      assertEquals(1, generator.generate(candidates, 1, new BigDecimal("0.3")).size());
    } else {
      assertThrows(UnfillableMealPlanException.class,
          () -> generator.generate(candidates, 1, new BigDecimal("0.3")));
    }
  }

  @Test
  void selectsTheFirstFeasibleCombinationInBreakfastLunchDinnerOrder() {
    List<PlanningRecipe> candidates = List.of(
        recipe("D2", MealType.DINNER, "900", 6),
        recipe("L2", MealType.LUNCH, "500", 6),
        recipe("B2", MealType.BREAKFAST, "500", 6),
        recipe("D1", MealType.DINNER, "300", 5),
        recipe("L1", MealType.LUNCH, "200", 5),
        recipe("B1", MealType.BREAKFAST, "100", 5));

    PlannedDay day = generator.generate(candidates, 1, new BigDecimal("1800")).get(0);

    assertEquals(List.of("B1", "L2", "D2"), sourceIds(day));
    assertEquals(0, new BigDecimal("1500").compareTo(day.totalCalories()));
  }

  @Test
  void breaksEqualPreparationTimeTiesBySourceIdRegardlessOfInputOrder() {
    List<PlanningRecipe> candidates = new ArrayList<>(List.of(
        recipe("breakfast-z", MealType.BREAKFAST, "500", 10),
        recipe("breakfast-a", MealType.BREAKFAST, "500", 10),
        recipe("lunch", MealType.LUNCH, "500", 10),
        recipe("dinner", MealType.DINNER, "500", 10)));

    List<PlannedDay> first = generator.generate(candidates, 1, new BigDecimal("1800"));
    Collections.reverse(candidates);
    List<PlannedDay> reversed = generator.generate(candidates, 1, new BigDecimal("1800"));

    assertEquals("breakfast-a", first.get(0).breakfast().sourceRecipeId());
    assertEquals(first, reversed);
  }

  @Test
  void excludesBothPreviousDaysAndAllowsReuseOnTheFourthDay() {
    List<PlannedDay> schedule = generator.generate(
        candidatesWithThreeChoicesPerMeal(), 7, new BigDecimal("1800"));

    assertEquals(7, schedule.size());
    for (int index = 0; index < schedule.size(); index++) {
      PlannedDay day = schedule.get(index);
      int choice = index % 3;
      assertEquals(DayOfWeek.MONDAY.plus(index), day.day());
      assertEquals(List.of("breakfast-" + choice, "lunch-" + choice, "dinner-" + choice),
          sourceIds(day));
    }
  }

  @Test
  void reportsWednesdayWhenOnlyTwoDistinctBreakfastRecipesExist() {
    List<PlanningRecipe> candidates = candidatesWithThreeChoicesPerMeal();
    candidates.removeIf(recipe -> "breakfast-2".equals(recipe.sourceRecipeId()));

    UnfillableMealPlanException failure = assertThrows(UnfillableMealPlanException.class,
        () -> generator.generate(candidates, 7, new BigDecimal("1800")));

    assertEquals(DayOfWeek.WEDNESDAY, failure.failedDay());
  }

  @Test
  void doesNotBacktrackEarlierDaysWhenTheFirstChoicesLeadToFailure() {
    List<PlanningRecipe> candidates = List.of(
        recipe("B0", MealType.BREAKFAST, "130", 10),
        recipe("B1", MealType.BREAKFAST, "60", 11),
        recipe("B2", MealType.BREAKFAST, "140", 12),
        recipe("L0", MealType.LUNCH, "110", 10),
        recipe("L1", MealType.LUNCH, "50", 11),
        recipe("L2", MealType.LUNCH, "50", 12),
        recipe("D0", MealType.DINNER, "60", 10),
        recipe("D1", MealType.DINNER, "80", 11),
        recipe("D2", MealType.DINNER, "80", 12));

    // B0/L1/D0, B1/L0/D1, B2/L2/D2 could form a repeating valid sequence.
    // The required first choices are B0/L0/D0 and B2/L1/D1, leaving Wednesday at
    // 190 kcal.
    UnfillableMealPlanException failure = assertThrows(UnfillableMealPlanException.class,
        () -> generator.generate(candidates, 7, new BigDecimal("300")));

    assertEquals(DayOfWeek.WEDNESDAY, failure.failedDay());
  }

  @Test
  void repeatedCopiesOfTheSameSourceCannotBypassTheTwoDayExclusion() {
    List<PlanningRecipe> candidates = candidatesWithThreeChoicesPerMeal();
    candidates.removeIf(recipe -> "breakfast-2".equals(recipe.sourceRecipeId()));
    candidates.add(recipe("breakfast-0", MealType.BREAKFAST, "500.00", 10));
    candidates.add(recipe("breakfast-1", MealType.BREAKFAST, "500.0", 11));

    UnfillableMealPlanException failure = assertThrows(UnfillableMealPlanException.class,
        () -> generator.generate(candidates, 7, new BigDecimal("1800")));

    assertEquals(DayOfWeek.WEDNESDAY, failure.failedDay());
  }

  @Test
  void acceptsEquivalentSourceCopiesWithDifferentDecimalScales() {
    List<PlanningRecipe> candidates = candidatesWithThreeChoicesPerMeal();
    List<PlannedDay> expected = generator.generate(candidates, 7, new BigDecimal("1800"));
    candidates.add(recipe("breakfast-0", MealType.BREAKFAST, "500.00", 10));

    List<PlannedDay> actual = generator.generate(candidates, 7, new BigDecimal("1800"));

    assertEquals(expected.size(), actual.size());
    for (int index = 0; index < expected.size(); index++) {
      assertEquals(sourceIds(expected.get(index)), sourceIds(actual.get(index)));
      assertEquals(0,
          expected.get(index).totalCalories().compareTo(actual.get(index).totalCalories()));
    }
  }

  @ParameterizedTest
  @EnumSource(MealType.class)
  void reportsMondayWhenOneMealTypeHasNoCandidates(MealType missingMeal) {
    List<PlanningRecipe> candidates = candidatesWithThreeChoicesPerMeal();
    candidates.removeIf(recipe -> recipe.mealType() == missingMeal);

    UnfillableMealPlanException failure = assertThrows(UnfillableMealPlanException.class,
        () -> generator.generate(candidates, 7, new BigDecimal("1800")));

    assertEquals(DayOfWeek.MONDAY, failure.failedDay());
  }

  @ParameterizedTest
  @MethodSource("conflictingSourceCopies")
  void rejectsContradictoryDataForTheSameSource(PlanningRecipe conflictingCopy) {
    List<PlanningRecipe> candidates = candidatesWithThreeChoicesPerMeal();
    candidates.add(conflictingCopy);

    assertThrows(IllegalArgumentException.class,
        () -> generator.generate(candidates, 1, new BigDecimal("1800")));
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 2, 6, 8 })
  void rejectsUnsupportedPlanLengths(int numberOfDays) {
    assertThrows(IllegalArgumentException.class,
        () -> generator.generate(
            candidatesWithThreeChoicesPerMeal(), numberOfDays, new BigDecimal("1800")));
  }

  @ParameterizedTest
  @ValueSource(strings = { "0", "-1" })
  void rejectsNonpositiveDailyCalorieLimits(String calorieLimit) {
    assertThrows(IllegalArgumentException.class,
        () -> generator.generate(
            candidatesWithThreeChoicesPerMeal(), 1, new BigDecimal(calorieLimit)));
  }

  @Test
  void preservesTheCandidateListAndReturnsAnImmutableSchedule() {
    List<PlanningRecipe> candidates = candidatesWithThreeChoicesPerMeal();
    Collections.reverse(candidates);
    List<PlanningRecipe> originalOrder = List.copyOf(candidates);

    List<PlannedDay> schedule = generator.generate(candidates, 7, new BigDecimal("1800"));

    assertEquals(originalOrder, candidates);
    assertThrows(UnsupportedOperationException.class, () -> schedule.add(schedule.get(0)));
    assertThrows(UnsupportedOperationException.class,
        () -> schedule.get(0).meals().add(schedule.get(0).breakfast()));
  }

  private static Stream<PlanningRecipe> conflictingSourceCopies() {
    return Stream.of(
        recipe("breakfast-0", MealType.LUNCH, "500", 10),
        recipe("breakfast-0", MealType.BREAKFAST, "501", 10),
        recipe("breakfast-0", MealType.BREAKFAST, "500", 11));
  }

  private static List<PlanningRecipe> candidatesWithThreeChoicesPerMeal() {
    List<PlanningRecipe> candidates = new ArrayList<>();
    for (int choice = 0; choice < 3; choice++) {
      candidates.add(recipe("breakfast-" + choice, MealType.BREAKFAST, "500", 10 + choice));
      candidates.add(recipe("lunch-" + choice, MealType.LUNCH, "500", 10 + choice));
      candidates.add(recipe("dinner-" + choice, MealType.DINNER, "500", 10 + choice));
    }
    return candidates;
  }

  private static List<String> sourceIds(PlannedDay day) {
    return day.meals().stream().map(PlanningRecipe::sourceRecipeId).toList();
  }

  private static PlanningRecipe recipe(
      String sourceId, MealType mealType, String calories, int preparationMinutes) {
    return new PlanningRecipe(sourceId, mealType, new BigDecimal(calories), preparationMinutes);
  }
}
