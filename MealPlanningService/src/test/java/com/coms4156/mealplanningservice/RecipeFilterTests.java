package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coms4156.mealplanningservice.model.CatalogRecipe;
import com.coms4156.mealplanningservice.service.AllergenChecker;
import com.coms4156.mealplanningservice.service.DietChecker;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for AllergenChecker and DietChecker. */
class RecipeFilterTests {

  private static CatalogRecipe recipe(
      final String diet, final String ingredients) {
    CatalogRecipe recipe = new CatalogRecipe();
    recipe.setDiet(diet);
    recipe.setIngredients(ingredients);
    return recipe;
  }

  @Test
  void allergenMatchesWholeWordsIgnoringCase() {
    assertTrue(AllergenChecker.containsAllergen(
        List.of("Rice", "Roasted Peanuts"), "peanuts"));
    assertTrue(AllergenChecker.containsAllergen(
        List.of("All Purpose Flour"), "wheat"));
    assertFalse(AllergenChecker.containsAllergen(
        List.of("Eggplant", "Rice flour"), "eggs"));
  }

  @Test
  void multiWordAllergensMatchWithHyphens() {
    assertTrue(AllergenChecker.containsAllergen(
        List.of("All-purpose Flour"), "wheat"));
    assertTrue(AllergenChecker.containsAllergen(
        List.of("All - Purpose  flour"), "wheat"));
  }

  @Test
  void treeNutsIncludeDatasetVariants() {
    assertTrue(AllergenChecker.containsAllergen(
        List.of("Pine nuts"), "tree_nuts"));
    assertTrue(AllergenChecker.containsAllergen(
        List.of("Mixed nuts"), "tree_nuts"));
    assertTrue(AllergenChecker.containsAllergen(
        List.of("Nutella Spread"), "tree_nuts"));
    assertFalse(AllergenChecker.containsAllergen(
        List.of("Nutmeg powder", "Fresh coconut"), "tree_nuts"));
  }

  @Test
  void isFreeOfChecksEveryAllergen() {
    List<String> ingredients = List.of("Paneer", "Onion", "Salt");

    assertTrue(AllergenChecker.isFreeOf(ingredients, List.of("peanuts")));
    assertFalse(
        AllergenChecker.isFreeOf(ingredients, List.of("peanuts", "dairy")));
    assertTrue(AllergenChecker.isFreeOf(ingredients, List.of()));
  }

  @Test
  void unsupportedAllergenIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> AllergenChecker.containsAllergen(List.of("Rice"), "gluten"));
  }

  @Test
  void vegetarianRejectsMeatAndNonVegLabel() {
    assertTrue(DietChecker.satisfies(
        recipe("Vegetarian", "Rice|Onion"), "vegetarian"));
    assertFalse(DietChecker.satisfies(
        recipe("Vegetarian", "Rice|Chicken breast"), "vegetarian"));
    assertFalse(DietChecker.satisfies(
        recipe("Non Vegeterian", "Rice|Onion"), "vegetarian"));
  }

  @Test
  void veganAlsoRejectsDairyEggsAndHoney() {
    assertTrue(DietChecker.satisfies(
        recipe("Vegan", "Rice|Tofu|Onion"), "vegan"));
    assertFalse(DietChecker.satisfies(
        recipe("Vegetarian", "Rice|Ghee"), "vegan"));
    assertFalse(DietChecker.satisfies(
        recipe("Vegetarian", "Oats|Honey"), "vegan"));
  }

  @Test
  void highProteinUsesTheDatasetLabel() {
    assertTrue(DietChecker.satisfies(
        recipe("High Protein Vegetarian", "Dal"), "high_protein"));
    assertFalse(DietChecker.satisfies(
        recipe("Vegetarian", "Dal"), "high_protein"));
  }

  @Test
  void satisfiesAllRequiresEveryDiet() {
    CatalogRecipe dal = recipe("High Protein Vegetarian", "Dal|Ghee");

    assertTrue(DietChecker.satisfiesAll(
        dal, List.of("vegetarian", "high_protein", "low_fat")));
    assertFalse(DietChecker.satisfiesAll(
        dal, List.of("vegetarian", "vegan")));
  }

  @Test
  void unsupportedDietIsRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> DietChecker.satisfies(recipe("Vegetarian", "Rice"), "keto"));
  }
}
