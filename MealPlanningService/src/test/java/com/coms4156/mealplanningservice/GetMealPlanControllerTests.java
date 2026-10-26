package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coms4156.mealplanningservice.controller.ApiController;
import com.coms4156.mealplanningservice.mealplan.MealType;
import com.coms4156.mealplanningservice.mealplan.PlannedDay;
import com.coms4156.mealplanningservice.mealplan.PlanningRecipe;
import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.model.MealPlan;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Controller tests for GET /v1/clients/{client_id}/mealplan/{id}. */
@ExtendWith(MockitoExtension.class)
class GetMealPlanControllerTests {

  private static final String URL = "/v1/clients/client-1/mealplan/plan-1";

  @Mock private MealPlanningService mealPlanningService;

  private MockMvc mockMvc;

  private MealPlan plan;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new ApiController(mealPlanningService))
        .build();
    PlannedDay monday = new PlannedDay(
        DayOfWeek.MONDAY,
        new PlanningRecipe(
            "r1", MealType.BREAKFAST, new BigDecimal("400"), 10),
        new PlanningRecipe("r2", MealType.LUNCH, new BigDecimal("600"), 20),
        new PlanningRecipe("r3", MealType.DINNER, new BigDecimal("700"), 30));
    plan = new MealPlan(
        "plan-1", "client-1", "week", List.of("Vegetarian"),
        List.of("peanuts"), 1800, 40, List.of(monday));
  }

  @Test
  void getMealPlanReturns200WithThePlan() throws Exception {
    when(mealPlanningService.authenticate("key-1"))
        .thenReturn(Optional.of(new Client("client-1", "John", "key-1")));
    when(mealPlanningService.findMealPlan("client-1", "plan-1"))
        .thenReturn(Optional.of(plan));

    mockMvc.perform(get(URL).header("X-API-Key", "key-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("plan-1"))
        .andExpect(jsonPath("$.clientId").value("client-1"))
        .andExpect(jsonPath("$.timeFrame").value("week"))
        .andExpect(jsonPath("$.maxCalories").value(1800))
        .andExpect(jsonPath("$.schedule[0].day").value("MONDAY"))
        .andExpect(
            jsonPath("$.schedule[0].lunch.sourceRecipeId").value("r2"));
  }

  @Test
  void getMealPlanReturns401WhenTheApiKeyHeaderIsMissing() throws Exception {
    when(mealPlanningService.authenticate(null)).thenReturn(Optional.empty());

    mockMvc.perform(get(URL))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").exists());

    verify(mealPlanningService).authenticate(null);
    verifyNoMoreInteractions(mealPlanningService);
  }

  @Test
  void getMealPlanReturns401WhenTheApiKeyIsUnknown() throws Exception {
    when(mealPlanningService.authenticate("bogus"))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(URL).header("X-API-Key", "bogus"))
        .andExpect(status().isUnauthorized());

    verifyNoMoreInteractions(mealPlanningService);
  }

  @Test
  void getMealPlanReturns403WhenTheKeyBelongsToAnotherClient()
      throws Exception {
    when(mealPlanningService.authenticate("key-2"))
        .thenReturn(Optional.of(new Client("client-2", "Mary", "key-2")));

    mockMvc.perform(get(URL).header("X-API-Key", "key-2"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").exists());

    verify(mealPlanningService).authenticate("key-2");
    verifyNoMoreInteractions(mealPlanningService);
  }

  @Test
  void getMealPlanReturns404WhenTheClientHasNoSuchPlan() throws Exception {
    when(mealPlanningService.authenticate("key-1"))
        .thenReturn(Optional.of(new Client("client-1", "John", "key-1")));
    when(mealPlanningService.findMealPlan("client-1", "plan-1"))
        .thenReturn(Optional.empty());

    mockMvc.perform(get(URL).header("X-API-Key", "key-1"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("meal plan not found"));
  }

  @Test
  void getMealPlanPropagatesStorageFailureAsAnException() throws Exception {
    when(mealPlanningService.authenticate("key-1"))
        .thenReturn(Optional.of(new Client("client-1", "John", "key-1")));
    when(mealPlanningService.findMealPlan("client-1", "plan-1"))
        .thenThrow(new IOException("disk error"));

    assertThrows(
        Exception.class,
        () -> mockMvc.perform(get(URL).header("X-API-Key", "key-1")));
  }
}
