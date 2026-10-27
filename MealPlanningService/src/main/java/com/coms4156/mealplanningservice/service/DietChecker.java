package com.coms4156.mealplanningservice.service;

import com.coms4156.mealplanningservice.model.CatalogRecipe;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks catalog recipes against our own diet definitions.
 *
 * vegetarian: no meat, fish or shellfish, and not labelled non-vegetarian.
 * vegan: vegetarian, and also no dairy, eggs or honey.
 * high_protein: the dataset labels the recipe as high protein.
 * low_fat and low_carb are accepted but not enforced yet, because they need
 * nutrition data that the catalog does not have.
 */
public final class DietChecker {

  /** The diet tags that clients may request. */
  private static final Set<String> SUPPORTED_DIETS = Set.of(
      "vegetarian", "vegan", "high_protein", "low_fat", "low_carb");

  /** Matches meat ingredients as whole words. */
  private static final Pattern MEAT = AllergenChecker.wholeWordPattern(
      List.of("chicken", "mutton", "lamb", "beef", "pork", "bacon", "ham",
          "meat", "keema", "kheema", "turkey", "duck", "goat", "sausage",
          "sausages", "salami", "pepperoni"));

  /** Matches honey as a whole word. */
  private static final Pattern HONEY =
      AllergenChecker.wholeWordPattern(List.of("honey"));

  /** Private constructor to prevent instantiation. */
  private DietChecker() {
  }

  /**
   * Returns the diet tags that clients may request.
   *
   * @return the supported diet tags
   */
  public static Set<String> supportedDiets() {
    return SUPPORTED_DIETS;
  }

  /**
   * Checks that the recipe satisfies every given diet.
   *
   * @param recipe the catalog recipe
   * @param diets  supported diet tags
   * @return true if the recipe satisfies all of the diets
   * @throws IllegalArgumentException if a diet tag is not supported
   */
  public static boolean satisfiesAll(
      final CatalogRecipe recipe, final List<String> diets) {
    return diets.stream().allMatch(diet -> satisfies(recipe, diet));
  }

  /**
   * Checks that the recipe satisfies one diet.
   *
   * @param recipe the catalog recipe
   * @param diet   a supported diet tag
   * @return true if the recipe satisfies the diet
   * @throws IllegalArgumentException if the diet tag is not supported
   */
  public static boolean satisfies(
      final CatalogRecipe recipe, final String diet) {
    List<String> ingredients = recipe.ingredientList();
    switch (diet) {
      case "vegetarian":
        return isVegetarian(recipe, ingredients);
      case "vegan":
        return isVegetarian(recipe, ingredients)
            && !AllergenChecker.containsAllergen(ingredients, "dairy")
            && !AllergenChecker.containsAllergen(ingredients, "eggs")
            && ingredients.stream()
                .noneMatch(item -> HONEY.matcher(item).find());
      case "high_protein":
        return label(recipe).contains("high protein");
      case "low_fat":
      case "low_carb":
        return true;
      default:
        throw new IllegalArgumentException("unsupported diet: " + diet);
    }
  }

  /**
   * Checks that the recipe has no meat, fish or shellfish.
   *
   * @param recipe      the catalog recipe
   * @param ingredients the recipe's ingredient names
   * @return true if the recipe is vegetarian
   */
  private static boolean isVegetarian(
      final CatalogRecipe recipe, final List<String> ingredients) {
    // "non veg" covers the dataset's misspelled "Non Vegeterian" label too.
    return !label(recipe).contains("non veg")
        && ingredients.stream()
            .noneMatch(item -> MEAT.matcher(item).find())
        && !AllergenChecker.containsAllergen(ingredients, "fish")
        && !AllergenChecker.containsAllergen(ingredients, "shellfish");
  }

  /**
   * Returns the recipe's diet label in lowercase.
   *
   * @param recipe the catalog recipe
   * @return the lowercase diet label, or an empty string if there is none
   */
  private static String label(final CatalogRecipe recipe) {
    String diet = recipe.getDiet();
    return diet == null ? "" : diet.toLowerCase(Locale.ROOT);
  }
}
