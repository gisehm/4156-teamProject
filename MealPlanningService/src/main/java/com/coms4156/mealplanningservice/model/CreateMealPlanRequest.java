package com.coms4156.mealplanningservice.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Validated POST constraints, separate from the saved MealPlan model.
 *
 * @param timeFrame day or week
 * @param maxCalories positive integer daily calorie limit
 * @param dietTypes diets that every selected recipe must satisfy
 * @param allergens allergens to exclude
 * @param maxPrepTimeMins positive preparation limit for each recipe
 */
public record CreateMealPlanRequest(
    String timeFrame, int maxCalories, List<String> dietTypes,
    List<String> allergens, int maxPrepTimeMins) {

  /** The accepted JSON request fields. */
  private static final Set<String> FIELDS = Set.of(
      "time_frame", "max_calories", "diet_type", "allergens",
      "max_prep_time_mins");

  /** Diet tags supported by this request contract. */
  private static final Set<String> DIETS = Set.of(
      "low_fat", "high_protein", "low_carb", "vegetarian", "vegan");

  /** Allergen tags supported by this request contract. */
  private static final Set<String> ALLERGENS = Set.of(
      "peanuts", "dairy", "eggs", "soy", "wheat", "tree_nuts", "fish",
      "shellfish", "sesame");

  /** Strict parser; decimal limits are not silently truncated to integers. */
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
      .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

  /**
   * Validates constraints and makes independent immutable restriction lists.
   *
   * @throws IllegalArgumentException if a constraint is invalid
   */
  public CreateMealPlanRequest {
    if (!"day".equals(timeFrame) && !"week".equals(timeFrame)) {
      throw new IllegalArgumentException("time_frame must be day or week");
    }
    if (maxCalories <= 0 || maxPrepTimeMins <= 0) {
      throw new IllegalArgumentException(
          "max_calories and max_prep_time_mins must be positive integers");
    }
    dietTypes = restrictions(dietTypes, DIETS, "diet_type");
    allergens = restrictions(allergens, ALLERGENS, "allergens");
  }

  /**
   * Parses the documented snake_case request without numeric coercion.
   *
   * @param body JSON request body
   * @return validated constraints
   * @throws IllegalArgumentException if the JSON or constraints are invalid
   */
  public static CreateMealPlanRequest parse(final String body) {
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("a JSON request body is required");
    }
    JsonNode root;
    try {
      root = MAPPER.readTree(body);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("a valid JSON object is required");
    }
    if (root == null || !root.isObject()) {
      throw new IllegalArgumentException("a JSON object is required");
    }
    root.fieldNames().forEachRemaining(field -> {
      if (!FIELDS.contains(field)) {
        throw new IllegalArgumentException("unknown field: " + field);
      }
    });
    JsonNode frame = root.get("time_frame");
    if (frame == null || !frame.isTextual()) {
      throw new IllegalArgumentException("time_frame must be day or week");
    }
    return new CreateMealPlanRequest(
        frame.textValue(), integer(root, "max_calories"),
        stringList(root, "diet_type"), stringList(root, "allergens"),
        integer(root, "max_prep_time_mins"));
  }

  private static int integer(final JsonNode root, final String field) {
    JsonNode value = root.get(field);
    if (value == null || !value.isIntegralNumber()
        || !value.canConvertToInt()) {
      throw new IllegalArgumentException(
          field + " must be a positive 32-bit integer");
    }
    return value.intValue();
  }

  private static List<String> stringList(
      final JsonNode root, final String field) {
    JsonNode value = root.get(field);
    if (value == null || value.isNull()) {
      return List.of();
    }
    if (!value.isArray()) {
      throw new IllegalArgumentException(field + " must be a string array");
    }
    List<String> result = new ArrayList<>();
    for (JsonNode item : value) {
      if (!item.isTextual()) {
        throw new IllegalArgumentException(field + " must be a string array");
      }
      result.add(item.textValue());
    }
    return result;
  }

  private static List<String> restrictions(
      final List<String> values, final Set<String> allowed,
      final String field) {
    if (values == null) {
      return List.of();
    }
    for (String value : values) {
      if (value == null || !allowed.contains(value)) {
        throw new IllegalArgumentException("unsupported " + field + " tag");
      }
    }
    return List.copyOf(new LinkedHashSet<>(values));
  }
}
