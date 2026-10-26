package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coms4156.mealplanningservice.model.MealPlan;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import com.coms4156.mealplanningservice.service.StorageService;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for finding a saved meal plan, with the storage mocked. */
@ExtendWith(MockitoExtension.class)
class FindMealPlanServiceTests {

  private static final String MEAL_PLANS_JSON =
      "[{\"id\":\"plan-1\",\"clientId\":\"client-1\",\"timeFrame\":\"week\","
          + "\"diet\":[\"Vegetarian\"],\"allergens\":[\"peanuts\"],"
          + "\"maxCalories\":1800,\"maxPrepTimeMins\":40,\"schedule\":["
          + "{\"day\":\"MONDAY\","
          + "\"breakfast\":{\"sourceRecipeId\":\"r1\","
          + "\"mealType\":\"BREAKFAST\",\"calories\":400,"
          + "\"totalTimeMinutes\":10},"
          + "\"lunch\":{\"sourceRecipeId\":\"r2\",\"mealType\":\"LUNCH\","
          + "\"calories\":600,\"totalTimeMinutes\":20},"
          + "\"dinner\":{\"sourceRecipeId\":\"r3\",\"mealType\":\"DINNER\","
          + "\"calories\":700,\"totalTimeMinutes\":30}}]},"
          + "{\"id\":\"plan-2\",\"clientId\":\"client-2\","
          + "\"timeFrame\":\"day\",\"schedule\":[]}]";

  @Mock private StorageService storageService;

  private MealPlanningService service;

  @BeforeEach
  void setUp() {
    service = new MealPlanningService(storageService);
  }

  @Test
  void findMealPlanReturnsThePlanOwnedByTheClient() throws Exception {
    when(storageService.readFile("mealplans.json")).thenReturn(MEAL_PLANS_JSON);

    Optional<MealPlan> plan = service.findMealPlan("client-1", "plan-1");

    assertTrue(plan.isPresent());
    assertEquals("plan-1", plan.get().getId());
    assertEquals("client-1", plan.get().getClientId());
    assertEquals("week", plan.get().getTimeFrame());
    assertEquals(1800, plan.get().getMaxCalories());
  }

  @Test
  void findMealPlanReadsTheScheduleIncludingMeals() throws Exception {
    when(storageService.readFile("mealplans.json")).thenReturn(MEAL_PLANS_JSON);

    MealPlan plan = service.findMealPlan("client-1", "plan-1").orElseThrow();

    assertEquals(1, plan.getSchedule().size());
    assertEquals("r2", plan.getSchedule().get(0).lunch().sourceRecipeId());
  }

  @Test
  void findMealPlanReturnsEmptyWhenTheIdDoesNotExist() throws Exception {
    when(storageService.readFile("mealplans.json")).thenReturn(MEAL_PLANS_JSON);

    assertTrue(service.findMealPlan("client-1", "no-such-plan").isEmpty());
  }

  @Test
  void findMealPlanTreatsAnotherClientsPlanAsNotFound() throws Exception {
    when(storageService.readFile("mealplans.json")).thenReturn(MEAL_PLANS_JSON);

    assertTrue(service.findMealPlan("client-1", "plan-2").isEmpty());
  }

  @Test
  void findMealPlanReturnsEmptyWhenNoPlansAreSaved() throws Exception {
    when(storageService.readFile("mealplans.json")).thenReturn("[]");

    assertTrue(service.findMealPlan("client-1", "plan-1").isEmpty());
  }

  @Test
  void findMealPlanWithNullIdsReturnsEmptyWithoutReadingStorage()
      throws Exception {
    assertTrue(service.findMealPlan(null, "plan-1").isEmpty());
    assertTrue(service.findMealPlan("client-1", null).isEmpty());

    verifyNoInteractions(storageService);
  }

  @Test
  void findMealPlanPropagatesStorageFailure() throws Exception {
    when(storageService.readFile("mealplans.json"))
        .thenThrow(new IOException("disk error"));

    assertThrows(
        IOException.class, () -> service.findMealPlan("client-1", "plan-1"));
  }
}
