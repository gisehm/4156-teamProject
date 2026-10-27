package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coms4156.mealplanningservice.controller.ApiController;
import com.coms4156.mealplanningservice.model.CatalogRecipe;
import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.model.FindRecipeRequest;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Controller tests for POST /v1/clients/{client_id}/recipes, with
 * MealPlanningService mocked.
 */
@ExtendWith(MockitoExtension.class)
class FindRecipeControllerTests {

  private static final String BODY = "{\"meal_type\":\"lunch\","
      + "\"max_calories\":400,\"diet_type\":[\"vegetarian\"],"
      + "\"allergens\":[\"peanuts\"],\"max_prep_time_mins\":40}";

  @Mock private MealPlanningService mealPlanningService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new ApiController(mealPlanningService))
        .build();
  }

  private void authenticateAs(final String clientId) throws Exception {
    when(mealPlanningService.authenticate("key-1"))
        .thenReturn(Optional.of(new Client(clientId, "Mary", "key-1")));
  }

  private static CatalogRecipe sampleRecipe() {
    CatalogRecipe recipe = new CatalogRecipe();
    recipe.setId("R-1");
    recipe.setName("Paneer rice");
    recipe.setUrl("https://example.com/r-1");
    recipe.setDescription("desc");
    recipe.setCourse("Lunch");
    recipe.setTotalTimeMins(20);
    recipe.setIngredients("Rice|Paneer");
    return recipe;
  }

  @Test
  void findRecipeReturns200WithSnakeCaseFields() throws Exception {
    authenticateAs("client-1");
    when(mealPlanningService.findRecipe(any()))
        .thenReturn(Optional.of(sampleRecipe()));

    mockMvc.perform(post("/v1/clients/client-1/recipes")
            .header("X-API-Key", "key-1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.recipe_id").value("R-1"))
        .andExpect(jsonPath("$.meal_type").value("lunch"))
        .andExpect(jsonPath("$.ingredients[1]").value("Paneer"))
        .andExpect(jsonPath("$.total_time_mins").value(20));

    ArgumentCaptor<FindRecipeRequest> request =
        ArgumentCaptor.forClass(FindRecipeRequest.class);
    verify(mealPlanningService).findRecipe(request.capture());
    assertEquals(40, request.getValue().getMaxPrepTimeMins());
  }

  @Test
  void findRecipeReturns401WithoutValidKey() throws Exception {
    when(mealPlanningService.authenticate(null)).thenReturn(Optional.empty());

    mockMvc.perform(post("/v1/clients/client-1/recipes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void findRecipeReturns403ForAnotherClient() throws Exception {
    authenticateAs("client-2");

    mockMvc.perform(post("/v1/clients/client-1/recipes")
            .header("X-API-Key", "key-1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isForbidden());
  }

  @Test
  void findRecipeReturns404WhenNothingMatches() throws Exception {
    authenticateAs("client-1");
    when(mealPlanningService.findRecipe(any())).thenReturn(Optional.empty());

    mockMvc.perform(post("/v1/clients/client-1/recipes")
            .header("X-API-Key", "key-1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(BODY))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  void findRecipeReturns400ForInvalidRequest() throws Exception {
    authenticateAs("client-1");
    when(mealPlanningService.findRecipe(any()))
        .thenThrow(new IllegalArgumentException("meal_type is required"));

    mockMvc.perform(post("/v1/clients/client-1/recipes")
            .header("X-API-Key", "key-1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("meal_type is required"));
  }
}
