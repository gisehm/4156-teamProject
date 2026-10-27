package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.coms4156.mealplanningservice.service.StorageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Exercises POST and GET against shared temporary file storage. */
@SpringBootTest
@ActiveProfiles({"test", "local", "mealplan-demo"})
@Import(TestConfig.class)
class MealPlanPostIntegrationTest {

  /** These client identities and keys are synthetic test data. */
  private static final String CLIENTS = """
      [{"id":"client-1","name":"One","apiKey":"key-1"},
       {"id":"client-2","name":"Two","apiKey":"key-2"}]
      """;

  /** Catalog fixture that POST must leave unchanged. */
  private static final String CATALOG =
      "[{\"id\":\"untouched-catalog-recipe\"}]";

  /** Valid request with both dietary and allergen restrictions. */
  private static final String REQUEST = """
      {"time_frame":"day","max_calories":1800,
       "diet_type":["vegan","high_protein"],
       "allergens":["dairy","peanuts"],"max_prep_time_mins":30}
      """;

  /** Expected meal slots in their scheduling order. */
  private static final List<String> SLOTS =
      List.of("breakfast", "lunch", "dinner");

  /** Shared storage implementation supplied by the test configuration. */
  @Autowired
  private StorageService storage;

  /** Real Spring web context containing both POST and GET controllers. */
  @Autowired
  private WebApplicationContext context;

  /** Isolated storage location recreated for every test. */
  @TempDir
  private Path directory;

  /** Independent parser for response and stored-data assertions. */
  private final ObjectMapper mapper = new ObjectMapper();

  /** Request dispatcher backed by the real Spring web context. */
  private MockMvc mvc;

  @BeforeEach
  void setUp() throws Exception {
    ((LocalStorageService) storage).setDirectory(directory);
    Files.writeString(directory.resolve("clients.json"), CLIENTS);
    Files.writeString(directory.resolve("food_recipes.json"), CATALOG);
    Files.writeString(planFile(), "[]");
    mvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @AfterEach
  void leavesClientCredentialsAndCatalogUnchanged() throws Exception {
    assertEquals(CLIENTS, Files.readString(directory.resolve("clients.json")));
    assertEquals(CATALOG,
        Files.readString(directory.resolve("food_recipes.json")));
  }

  @Test
  void createsSharedDayWithUuidAndAllConstraintsReadableByGet()
      throws Exception {
    JsonNode plan = created(REQUEST);

    assertEquals(plan.path("id").asText(),
        UUID.fromString(plan.path("id").asText()).toString());
    assertEquals("client-1", plan.path("clientId").asText());
    assertEquals("day", plan.path("timeFrame").asText());
    assertEquals(1800, plan.path("maxCalories").asInt());
    assertEquals(30, plan.path("maxPrepTimeMins").asInt());
    assertEquals(mapper.readTree("[\"vegan\",\"high_protein\"]"),
        plan.path("diet"));
    assertEquals(mapper.readTree("[\"dairy\",\"peanuts\"]"),
        plan.path("allergens"));
    assertEquals(1, plan.path("schedule").size());
    assertEquals("MONDAY", plan.path("schedule").get(0).path("day").asText());
    assertDay(plan.path("schedule").get(0), 1800);
    assertEquals(plan, stored().get(0));
    assertEquals(plan,
        retrieved("client-1", "key-1", plan.path("id").asText(), 200));
  }

  @Test
  void createsCompleteWeekWithChronologicalDaysAndNoRecentRepeats()
      throws Exception {
    JsonNode plan = created(REQUEST.replace("\"day\"", "\"week\""));
    List<Set<String>> sources = new ArrayList<>();

    assertEquals(7, plan.path("schedule").size());
    for (int index = 0; index < 7; index++) {
      JsonNode day = plan.path("schedule").get(index);
      assertEquals(DayOfWeek.MONDAY.plus(index).name(),
          day.path("day").asText());
      Set<String> used = assertDay(day, 1800);
      for (int previous = Math.max(0, index - 2);
          previous < index; previous++) {
        assertTrue(Collections.disjoint(used, sources.get(previous)));
      }
      sources.add(used);
    }
    assertEquals(plan,
        retrieved("client-1", "key-1", plan.path("id").asText(), 200));
  }

  @Test
  void appendsAnotherUuidWithoutReplacingTheExistingPlan() throws Exception {
    JsonNode first = created(REQUEST);
    JsonNode second = created(REQUEST.replace("1800", "1500"));

    assertNotEquals(first.path("id"), second.path("id"));
    assertEquals(2, stored().size());
    assertEquals(first, stored().get(0));
    assertEquals(second, stored().get(1));
    assertEquals(1800, stored().get(0).path("maxCalories").asInt());
    assertEquals(1500, stored().get(1).path("maxCalories").asInt());
    assertEquals(first,
        retrieved("client-1", "key-1", first.path("id").asText(), 200));
  }

  @Test
  void omittedRestrictionsPermitTheFastestDairyFixtures() throws Exception {
    JsonNode plan = created("""
        {"time_frame":"day","max_calories":1800,"max_prep_time_mins":30}
        """);

    assertTrue(plan.path("diet").isEmpty());
    assertTrue(plan.path("allergens").isEmpty());
    for (String slot : SLOTS) {
      JsonNode meal = plan.path("schedule").get(0).path(slot);
      assertTrue(meal.path("sourceRecipeId").asText().endsWith("-dairy"));
      assertEquals(1, meal.path("totalTimeMinutes").asInt());
    }
  }

  @Test
  void veganRestrictionExcludesDairyEvenWithoutAnAllergenRestriction()
      throws Exception {
    JsonNode plan = created("""
        {"time_frame":"day","max_calories":1800,"diet_type":["vegan"],
         "max_prep_time_mins":30}
        """);

    assertPlantMeals(plan);
  }

  @Test
  void dairyAllergenExcludesDairyForAnOtherwiseVegetarianPlan()
      throws Exception {
    JsonNode plan = created("""
        {"time_frame":"day","max_calories":1800,"diet_type":["vegetarian"],
         "allergens":["dairy"],"max_prep_time_mins":30}
        """);

    assertPlantMeals(plan);
  }

  @Test
  void missingBlankAndUnknownKeysAreRejectedBeforeBodyParsing()
      throws Exception {
    String before = Files.readString(planFile());
    for (String key : new String[] {null, "", " ", "unknown-key"}) {
      assertError(postPlan("client-1", key, "not-json"), 401);
      assertEquals(before, Files.readString(planFile()));
    }
  }

  @Test
  void anotherClientsPathIsRejectedBeforeBodyParsing() throws Exception {
    String before = Files.readString(planFile());

    assertError(postPlan("client-2", "key-1", "not-json"), 403);
    assertEquals(before, Files.readString(planFile()));
  }

  @Test
  void invalidRequestBodiesReturn400WithoutChangingAnExistingPlan()
      throws Exception {
    created(REQUEST);
    String before = Files.readString(planFile());
    for (String body : new String[] {null, "{", REQUEST.replace("1800", "0")}) {
      assertError(postPlan("client-1", "key-1", body), 400);
      assertEquals(before, Files.readString(planFile()));
    }
  }

  @Test
  void wednesdayFailureDoesNotSaveAnyPartialDays() throws Exception {
    created(REQUEST);
    String before = Files.readString(planFile());
    MvcResult result = postPlan("client-1", "key-1", """
        {"time_frame":"week","max_calories":1800,"max_prep_time_mins":10}
        """);

    assertError(result, 422);
    assertEquals("wednesday", json(result).path("failed_day").asText());
    assertEquals(before, Files.readString(planFile()));
  }

  @Test
  void unsupportedByFixturesLowCarbPlanReturns422OnMonday() throws Exception {
    String before = Files.readString(planFile());
    MvcResult result = postPlan("client-1", "key-1",
        REQUEST.replace("\"vegan\"", "\"low_carb\""));

    assertError(result, 422);
    assertEquals("monday", json(result).path("failed_day").asText());
    assertEquals(before, Files.readString(planFile()));
  }

  @Test
  void corruptStoredDataReturns500WithoutOverwritingIt() throws Exception {
    for (String damaged : List.of("{broken-json", "null", "[] {}")) {
      Files.writeString(planFile(), damaged);
      MvcResult result = postPlan("client-1", "key-1", REQUEST);

      assertError(result, 500);
      assertFalse(json(result).path("error").asText()
          .contains(directory.toString()));
      assertEquals(damaged, Files.readString(planFile()));
    }
  }

  @Test
  void missingPlanStorageReturns500InsteadOfReportingCreation()
      throws Exception {
    Files.delete(planFile());

    assertError(postPlan("client-1", "key-1", REQUEST), 500);
    assertFalse(Files.exists(planFile()));
  }

  @Test
  void getCannotRevealAnotherClientsSavedPlan() throws Exception {
    String id = created(REQUEST).path("id").asText();

    retrieved("client-2", "key-2", id, 404);
    retrieved("client-1", "key-2", id, 403);
    retrieved("client-1", "key-1", UUID.randomUUID().toString(), 404);
  }

  private JsonNode created(final String body) throws Exception {
    MvcResult result = postPlan("client-1", "key-1", body);
    assertEquals(201, result.getResponse().getStatus(),
        result.getResponse().getContentAsString());
    return json(result);
  }

  private MvcResult postPlan(
      final String clientId, final String key, final String body)
      throws Exception {
    MockHttpServletRequestBuilder request = post(
        "/v1/clients/{client_id}/mealplan", clientId)
        .contentType(MediaType.APPLICATION_JSON);
    if (key != null) {
      request.header("X-API-Key", key);
    }
    if (body != null) {
      request.content(body);
    }
    return mvc.perform(request).andReturn();
  }

  private JsonNode retrieved(
      final String clientId, final String key, final String id,
      final int expectedStatus) throws Exception {
    MvcResult result = mvc.perform(get(
        "/v1/clients/{client_id}/mealplan/{mealplan_id}", clientId, id)
        .header("X-API-Key", key)).andReturn();
    assertEquals(expectedStatus, result.getResponse().getStatus(),
        result.getResponse().getContentAsString());
    return json(result);
  }

  private void assertError(final MvcResult result, final int expectedStatus)
      throws Exception {
    assertEquals(expectedStatus, result.getResponse().getStatus(),
        result.getResponse().getContentAsString());
    assertTrue(json(result).hasNonNull("error"));
  }

  private Set<String> assertDay(final JsonNode day, final int calorieCap) {
    BigDecimal calories = BigDecimal.ZERO;
    Set<String> sources = new HashSet<>();
    for (String slot : SLOTS) {
      JsonNode meal = day.path(slot);
      assertEquals(slot.toUpperCase(java.util.Locale.ROOT),
          meal.path("mealType").asText());
      assertTrue(sources.add(meal.path("sourceRecipeId").asText()));
      calories = calories.add(meal.path("calories").decimalValue());
    }
    assertTrue(calories.compareTo(BigDecimal.valueOf(calorieCap)) <= 0);
    assertTrue(calories.compareTo(
        BigDecimal.valueOf(calorieCap).multiply(new BigDecimal("0.8"))) >= 0);
    return sources;
  }

  private void assertPlantMeals(final JsonNode plan) {
    for (String slot : SLOTS) {
      JsonNode meal = plan.path("schedule").get(0).path(slot);
      assertFalse(meal.path("sourceRecipeId").asText().endsWith("-dairy"));
      assertEquals(10, meal.path("totalTimeMinutes").asInt());
    }
  }

  private JsonNode json(final MvcResult result) throws Exception {
    return mapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode stored() throws Exception {
    return mapper.readTree(Files.readString(planFile()));
  }

  private Path planFile() {
    return directory.resolve("mealplans.json");
  }
}
