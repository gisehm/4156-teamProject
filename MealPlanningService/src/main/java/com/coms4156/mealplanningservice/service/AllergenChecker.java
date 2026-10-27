package com.coms4156.mealplanningservice.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks ingredient names against our own allergen dictionary.
 *
 * Words are matched case-insensitively as whole words, so "egg" matches
 * "Egg yolk" but not "Eggplant". The check is deliberately conservative:
 * "peanut butter" also counts as dairy because it contains "butter".
 */
public final class AllergenChecker {

  /** Words that identify each supported allergen tag. */
  private static final Map<String, List<String>> ALLERGEN_WORDS = Map.of(
      "peanuts", List.of("peanut", "peanuts", "groundnut", "groundnuts"),
      "dairy", List.of("milk", "butter", "buttermilk", "cheese", "cream",
          "yogurt", "yoghurt", "curd", "paneer", "ghee", "khoa", "khoya",
          "malai"),
      "eggs", List.of("egg", "eggs"),
      "soy", List.of("soy", "soya", "tofu", "edamame"),
      "wheat", List.of("wheat", "all purpose flour", "maida", "semolina",
          "sooji", "suji", "rava", "atta", "bread", "pasta", "spaghetti",
          "noodles", "vermicelli"),
      "tree_nuts", List.of("almond", "almonds", "cashew", "cashews",
          "walnut", "walnuts", "pistachio", "pistachios", "hazelnut",
          "hazelnuts", "pecan", "pecans", "macadamia", "badam", "kaju",
          "pine nut", "pine nuts", "mixed nuts", "nutella"),
      "fish", List.of("fish", "salmon", "tuna", "cod", "sardine",
          "sardines", "mackerel", "anchovy", "anchovies", "tilapia",
          "pomfret", "basa"),
      "shellfish", List.of("prawn", "prawns", "shrimp", "shrimps", "crab",
          "crabs", "lobster", "mussels", "clams", "oysters", "scallops"),
      "sesame", List.of("sesame", "til", "gingelly", "tahini"));

  /** One whole-word, case-insensitive pattern per allergen tag. */
  private static final Map<String, Pattern> ALLERGEN_PATTERNS =
      ALLERGEN_WORDS.entrySet().stream()
          .collect(Collectors.toUnmodifiableMap(
              Map.Entry::getKey,
              entry -> wholeWordPattern(entry.getValue())));

  /** Private constructor to prevent instantiation. */
  private AllergenChecker() {
  }

  /**
   * Returns the allergen tags that clients may request.
   *
   * @return the supported allergen tags
   */
  public static Set<String> supportedAllergens() {
    return ALLERGEN_PATTERNS.keySet();
  }

  /**
   * Checks whether any ingredient contains the given allergen.
   *
   * @param ingredients the ingredient names
   * @param allergen    a supported allergen tag
   * @return true if at least one ingredient matches the allergen
   * @throws IllegalArgumentException if the allergen tag is not supported
   */
  public static boolean containsAllergen(
      final List<String> ingredients, final String allergen) {
    Pattern pattern = ALLERGEN_PATTERNS.get(allergen);
    if (pattern == null) {
      throw new IllegalArgumentException("unsupported allergen: " + allergen);
    }
    return ingredients.stream()
        .anyMatch(ingredient -> pattern.matcher(ingredient).find());
  }

  /**
   * Checks that the ingredients contain none of the given allergens.
   *
   * @param ingredients the ingredient names
   * @param allergens   supported allergen tags
   * @return true if no ingredient matches any of the allergens
   * @throws IllegalArgumentException if an allergen tag is not supported
   */
  public static boolean isFreeOf(
      final List<String> ingredients, final List<String> allergens) {
    return allergens.stream()
        .noneMatch(allergen -> containsAllergen(ingredients, allergen));
  }

  /**
   * Builds a pattern that matches any of the words as a whole word. The
   * words of a multi-word entry may be separated by spaces or hyphens, so
   * "all purpose flour" also matches "All-purpose Flour".
   *
   * @param words the words to match
   * @return a case-insensitive whole-word pattern
   */
  static Pattern wholeWordPattern(final List<String> words) {
    String alternatives = words.stream()
        .map(word -> Arrays.stream(word.split(" "))
            .map(Pattern::quote)
            .collect(Collectors.joining("[\\s-]+")))
        .collect(Collectors.joining("|"));
    return Pattern.compile(
        "\\b(?:" + alternatives + ")\\b", Pattern.CASE_INSENSITIVE);
  }
}
