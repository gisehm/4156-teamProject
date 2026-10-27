package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coms4156.mealplanningservice.model.CatalogRecipe;
import com.coms4156.mealplanningservice.model.FindRecipeRequest;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import com.coms4156.mealplanningservice.service.StorageService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for MealPlanningService.findRecipe, with storage mocked. */
@ExtendWith(MockitoExtension.class)
class FindRecipeServiceTests {

  private static final String CATALOG_JSON = "["
      + recipeJson("L-slow", "Lunch", "Vegetarian", 45, "Rice|Dal")
      + "," + recipeJson("L-peanut", "Lunch", "Vegetarian", 10,
          "Poha|Peanuts")
      + "," + recipeJson("L-chicken", "Lunch", "Non Vegeterian", 15,
          "Rice|Chicken")
      + "," + recipeJson("L-veg", "Lunch", "Vegetarian", 20, "Rice|Paneer")
      + "," + recipeJson("L-zero", "Lunch", "Vegetarian", 0, "Bread")
      + "," + recipeJson("D-fast", "Dinner", "Vegetarian", 5, "Soup")
      + "]";

  @Mock private StorageService storageService;

  private MealPlanningService service;

  private static String recipeJson(
      final String id, final String course, final String diet,
      final int totalTime, final String ingredients) {
    return "{\"id\":\"" + id + "\",\"name\":\"" + id + " name\","
        + "\"url\":\"https://example.com/" + id + "\","
        + "\"description\":\"desc\",\"course\":\"" + course + "\","
        + "\"diet\":\"" + diet + "\",\"total_time\":" + totalTime + ","
        + "\"ingredients\":\"" + ingredients + "\","
        + "\"instructions\":\"steps\"}";
  }

  private static FindRecipeRequest lunch(
      final List<String> diets, final List<String> allergens) {
    return new FindRecipeRequest("lunch", 600, diets, allergens, 60);
  }

  @BeforeEach
  void setUp() {
    service = new MealPlanningService(storageService);
  }

  @Test
  void findRecipePicksShortestMatchingMealIgnoringCase() throws Exception {
    when(storageService.readFile("food_recipes.json"))
        .thenReturn(CATALOG_JSON);

    CatalogRecipe recipe =
        service.findRecipe(lunch(null, null)).orElseThrow();

    assertEquals("L-peanut", recipe.getId());
    assertEquals(10, recipe.getTotalTimeMins());
    assertEquals(List.of("Poha", "Peanuts"), recipe.ingredientList());
  }

  @Test
  void findRecipeSkipsAllergensDietsAndZeroTimes() throws Exception {
    when(storageService.readFile("food_recipes.json"))
        .thenReturn(CATALOG_JSON);

    CatalogRecipe recipe = service.findRecipe(
        lunch(List.of("vegetarian"), List.of("peanuts"))).orElseThrow();

    assertEquals("L-veg", recipe.getId());
  }

  @Test
  void findRecipeRespectsMaxPrepTime() throws Exception {
    when(storageService.readFile("food_recipes.json"))
        .thenReturn(CATALOG_JSON);

    // L-slow is the only vegetarian, peanut- and dairy-free lunch, and it
    // takes 45 minutes.
    FindRecipeRequest request = new FindRecipeRequest(
        "lunch", null, List.of("vegetarian"), List.of("peanuts", "dairy"), 30);

    assertTrue(service.findRecipe(request).isEmpty());
  }

  @Test
  void findRecipeReturnsEmptyForUnknownMealType() throws Exception {
    when(storageService.readFile("food_recipes.json"))
        .thenReturn(CATALOG_JSON);

    FindRecipeRequest request =
        new FindRecipeRequest("brunch", null, null, null, null);

    assertTrue(service.findRecipe(request).isEmpty());
  }

  @Test
  void findRecipeRejectsMissingMealType() {
    FindRecipeRequest request =
        new FindRecipeRequest("  ", null, null, null, null);

    assertThrows(
        IllegalArgumentException.class, () -> service.findRecipe(request));
    verifyNoInteractions(storageService);
  }

  @Test
  void findRecipeRejectsNonPositiveLimits() {
    FindRecipeRequest request =
        new FindRecipeRequest("lunch", 0, null, null, null);

    assertThrows(
        IllegalArgumentException.class, () -> service.findRecipe(request));
  }

  @Test
  void findRecipeRejectsUnsupportedTags() {
    assertThrows(IllegalArgumentException.class,
        () -> service.findRecipe(lunch(List.of("keto"), null)));
    assertThrows(IllegalArgumentException.class,
        () -> service.findRecipe(lunch(null, List.of("gluten"))));
    verifyNoInteractions(storageService);
  }

  @Test
  void findRecipeRejectsNullTags() {
    List<String> nullTag = Arrays.asList((String) null);

    assertThrows(IllegalArgumentException.class,
        () -> service.findRecipe(lunch(nullTag, null)));
    assertThrows(IllegalArgumentException.class,
        () -> service.findRecipe(lunch(null, nullTag)));
    verifyNoInteractions(storageService);
  }

  @Test
  void findRecipePropagatesStorageFailure() throws Exception {
    when(storageService.readFile("food_recipes.json"))
        .thenThrow(new IOException("disk error"));

    assertThrows(IOException.class, () -> service.findRecipe(lunch(null, null)));
  }
}
