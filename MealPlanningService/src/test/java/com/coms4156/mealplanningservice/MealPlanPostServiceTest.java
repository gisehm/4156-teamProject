package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.coms4156.mealplanningservice.mealplan.UnfillableMealPlanException;
import com.coms4156.mealplanningservice.model.CreateMealPlanRequest;
import com.coms4156.mealplanningservice.model.MealPlan;
import com.coms4156.mealplanningservice.service.LocalMealPlanCandidates;
import com.coms4156.mealplanningservice.service.MealPlanPostService;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import com.coms4156.mealplanningservice.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Checks shared storage compatibility using the actual local candidates. */
@ExtendWith(MockitoExtension.class)
class MealPlanPostServiceTest {

  /** The shared file read by both POST and GET services. */
  private static final String PLANS_FILE = "mealplans.json";

  /** Storage is the only replaced dependency. */
  @Mock private StorageService storage;

  /** Mapper for inspecting the public shared storage document. */
  private final ObjectMapper mapper = new ObjectMapper();

  /** POST service with the real synthetic candidate source and generator. */
  private MealPlanPostService service;

  @BeforeEach
  void setUp() {
    service = new MealPlanPostService(storage, new LocalMealPlanCandidates());
  }

  @Test
  void appendsWithoutReplacingExistingPlansAndCanBeReadByGet()
      throws Exception {
    MealPlan existing = new MealPlan(
        "existing-plan", "other-client", "day", List.of(), List.of(),
        1800, 30, List.of());
    String initial = mapper.writeValueAsString(List.of(existing));
    when(storage.readFile(PLANS_FILE)).thenReturn(initial);
    CreateMealPlanRequest request = request("week", 30);

    MealPlan created = service.create("client-1", request);

    ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
    verify(storage).readFile(PLANS_FILE);
    verify(storage).writeFile(eq(PLANS_FILE), content.capture());
    verifyNoMoreInteractions(storage);
    JsonNode saved = mapper.readTree(content.getValue());
    assertTrue(saved.isArray());
    assertEquals(2, saved.size());
    assertEquals(mapper.readTree(initial).get(0), saved.get(0));
    assertNotEquals(existing.getId(), created.getId());
    assertNotNull(UUID.fromString(created.getId()));
    assertEquals(created.getId(), saved.get(1).get("id").asText());

    when(storage.readFile(PLANS_FILE)).thenReturn(content.getValue());
    MealPlanningService reader = new MealPlanningService(storage);
    MealPlan loaded = reader.findMealPlan("client-1", created.getId())
        .orElseThrow();
    assertEquals("client-1", loaded.getClientId());
    assertEquals("week", loaded.getTimeFrame());
    assertEquals(request.dietTypes(), loaded.getDiet());
    assertEquals(request.allergens(), loaded.getAllergens());
    assertEquals(1800, loaded.getMaxCalories());
    assertEquals(30, loaded.getMaxPrepTimeMins());
    assertEquals(created.getSchedule(), loaded.getSchedule());
    assertEquals(7, loaded.getSchedule().size());
    assertEquals(DayOfWeek.SUNDAY, loaded.getSchedule().get(6).day());
    loaded.getSchedule().forEach(day -> day.meals().forEach(meal ->
        assertFalse(meal.sourceRecipeId().endsWith("-dairy"))));
    assertTrue(reader.findMealPlan("other-client", created.getId())
        .isEmpty());
  }

  @Test
  void createsTheFirstDayPlanInAnEmptySharedArray() throws Exception {
    when(storage.readFile(PLANS_FILE)).thenReturn("[]");
    CreateMealPlanRequest request = new CreateMealPlanRequest(
        "day", 1800, List.of("vegetarian"), List.of(), 1);

    MealPlan created = service.create("client-1", request);

    assertEquals(1, created.getSchedule().size());
    assertEquals(DayOfWeek.MONDAY, created.getSchedule().get(0).day());
    created.getSchedule().get(0).meals().forEach(meal ->
        assertTrue(meal.sourceRecipeId().endsWith("-dairy")));
    ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
    verify(storage).writeFile(eq(PLANS_FILE), content.capture());
    assertEquals(1, mapper.readTree(content.getValue()).size());
    assertEquals(created.getId(),
        mapper.readTree(content.getValue()).get(0).get("id").asText());
  }

  @Test
  void lateWednesdayFailureDoesNotReadOrWriteStorage() {
    UnfillableMealPlanException failure = assertThrows(
        UnfillableMealPlanException.class,
        () -> service.create("client-1", request("week", 11)));

    assertEquals(DayOfWeek.WEDNESDAY, failure.failedDay());
    verifyNoInteractions(storage);
  }

  @Test
  void anUnsatisfiedDietDoesNotReadOrWriteStorage() {
    CreateMealPlanRequest request = new CreateMealPlanRequest(
        "day", 1800, List.of("low_carb"), List.of(), 30);

    assertThrows(UnfillableMealPlanException.class,
        () -> service.create("client-1", request));

    verifyNoInteractions(storage);
  }

  @ParameterizedTest
  @ValueSource(strings = {"{", "null", "[null]", "{}", "[] []"})
  void rejectsInvalidStoredDocumentsWithoutWriting(final String content)
      throws Exception {
    when(storage.readFile(PLANS_FILE)).thenReturn(content);

    assertThrows(IOException.class,
        () -> service.create("client-1", request("day", 30)));

    verify(storage).readFile(PLANS_FILE);
    verify(storage, never()).writeFile(anyString(), anyString());
  }

  @Test
  void propagatesReadFailuresWithoutWriting() throws Exception {
    IOException failure = new IOException("simulated read failure");
    when(storage.readFile(PLANS_FILE)).thenThrow(failure);

    assertSame(failure, assertThrows(IOException.class,
        () -> service.create("client-1", request("day", 30))));

    verify(storage).readFile(PLANS_FILE);
    verifyNoMoreInteractions(storage);
  }

  @Test
  void propagatesWriteFailuresWithoutRetrying() throws Exception {
    when(storage.readFile(PLANS_FILE)).thenReturn("[]");
    IOException failure = new IOException("simulated write failure");
    doThrow(failure).when(storage).writeFile(eq(PLANS_FILE), anyString());

    assertSame(failure, assertThrows(IOException.class,
        () -> service.create("client-1", request("day", 30))));

    verify(storage).readFile(PLANS_FILE);
    verify(storage).writeFile(eq(PLANS_FILE), anyString());
    verifyNoMoreInteractions(storage);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" "})
  void rejectsMissingClientWithoutTouchingStorage(final String clientId) {
    assertThrows(IllegalArgumentException.class,
        () -> service.create(clientId, request("day", 30)));

    verifyNoInteractions(storage);
  }

  private static CreateMealPlanRequest request(
      final String frame, final int prepMinutes) {
    return new CreateMealPlanRequest(
        frame, 1800, List.of("vegan"), List.of("dairy"), prepMinutes);
  }
}
