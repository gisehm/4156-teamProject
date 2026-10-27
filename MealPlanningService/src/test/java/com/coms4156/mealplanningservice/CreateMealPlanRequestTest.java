package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.coms4156.mealplanningservice.model.CreateMealPlanRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests the public POST request contract without numeric coercion. */
class CreateMealPlanRequestTest {

  /** Minimal valid request used to vary one invalid boundary at a time. */
  private static final String VALID_BODY =
      "{\"time_frame\":\"week\",\"max_calories\":1800,"
          + "\"max_prep_time_mins\":30}";

  @ParameterizedTest
  @ValueSource(strings = {"day", "week"})
  void acceptsBothTimeFramesWithAbsentRestrictions(final String frame) {
    CreateMealPlanRequest request = CreateMealPlanRequest.parse(
        VALID_BODY.replace("week", frame));

    assertEquals(frame, request.timeFrame());
    assertEquals(1800, request.maxCalories());
    assertEquals(30, request.maxPrepTimeMins());
    assertTrue(request.dietTypes().isEmpty());
    assertTrue(request.allergens().isEmpty());
  }

  @Test
  void treatsNullRestrictionsAsEmpty() {
    CreateMealPlanRequest request = CreateMealPlanRequest.parse(
        withRestrictions("null", "null"));

    assertTrue(request.dietTypes().isEmpty());
    assertTrue(request.allergens().isEmpty());
  }

  @Test
  void acceptsThePositiveIntegerUpperBoundaryWithoutTruncation() {
    CreateMealPlanRequest request = CreateMealPlanRequest.parse(
        VALID_BODY.replace("1800", "2147483647")
            .replace(":30", ":2147483647"));

    assertEquals(Integer.MAX_VALUE, request.maxCalories());
    assertEquals(Integer.MAX_VALUE, request.maxPrepTimeMins());
  }

  @Test
  void deduplicatesRestrictionsAndMakesParsedListsImmutable() {
    CreateMealPlanRequest request = CreateMealPlanRequest.parse(
        withRestrictions("[\"vegan\",\"low_fat\",\"vegan\"]",
            "[\"dairy\",\"peanuts\",\"dairy\"]"));

    assertEquals(List.of("vegan", "low_fat"), request.dietTypes());
    assertEquals(List.of("dairy", "peanuts"), request.allergens());
    assertThrows(UnsupportedOperationException.class,
        () -> request.dietTypes().add("vegetarian"));
    assertThrows(UnsupportedOperationException.class,
        () -> request.allergens().clear());
  }

  @Test
  void constructorTakesIndependentSnapshotsOfCallerLists() {
    List<String> diets = new ArrayList<>(List.of("vegan"));
    List<String> allergens = new ArrayList<>(List.of("dairy"));
    CreateMealPlanRequest request = new CreateMealPlanRequest(
        "day", 1800, diets, allergens, 30);

    diets.clear();
    allergens.add("peanuts");

    assertEquals(List.of("vegan"), request.dietTypes());
    assertEquals(List.of("dairy"), request.allergens());
  }

  @ParameterizedTest
  @MethodSource("invalidBodies")
  void rejectsInvalidBodiesAndUnsupportedValues(final String body) {
    assertThrows(IllegalArgumentException.class,
        () -> CreateMealPlanRequest.parse(body));
  }

  private static Stream<String> invalidBodies() {
    return Stream.of(
        null,
        " ",
        "{",
        "null",
        "[]",
        VALID_BODY.replace("\"time_frame\":\"week\",", ""),
        VALID_BODY.replace("\"max_calories\":1800,", ""),
        VALID_BODY.replace(",\"max_prep_time_mins\":30", ""),
        VALID_BODY.replace("week", "month"),
        VALID_BODY.replace("\"week\"", "true"),
        VALID_BODY.replace("1800", "null"),
        VALID_BODY.replace("1800", "\"1800\""),
        VALID_BODY.replace(":30", ":30.5"),
        VALID_BODY.replace("1800", "2147483648"),
        VALID_BODY.replace("1800", "0"),
        VALID_BODY.replace(":30", ":-1"),
        VALID_BODY.replace("}", ",\"unexpected\":true}"),
        VALID_BODY.replace("}", ",\"max_calories\":1700}"),
        VALID_BODY + " {}",
        withRestrictions("\"vegan\"", "[]"),
        withRestrictions("[null]", "[]"),
        withRestrictions("[\"unsupported\"]", "[]"),
        withRestrictions("[]", "[42]"),
        withRestrictions("[]", "[\"unsupported\"]"));
  }

  private static String withRestrictions(
      final String diets, final String allergens) {
    return VALID_BODY.substring(0, VALID_BODY.length() - 1)
        + ",\"diet_type\":" + diets + ",\"allergens\":" + allergens + "}";
  }
}
