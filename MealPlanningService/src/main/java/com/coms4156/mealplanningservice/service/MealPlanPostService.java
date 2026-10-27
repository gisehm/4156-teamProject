package com.coms4156.mealplanningservice.service;

import com.coms4156.mealplanningservice.mealplan.MealPlanGenerator;
import com.coms4156.mealplanningservice.mealplan.PlannedDay;
import com.coms4156.mealplanningservice.model.CreateMealPlanRequest;
import com.coms4156.mealplanningservice.model.MealPlan;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Creates shared saved plans from the explicitly local demo candidates. */
@Service
@Profile("local & mealplan-demo")
public final class MealPlanPostService {

  /** The same storage filename used by the team's GET implementation. */
  private static final String PLANS_FILE = "mealplans.json";

  /** Shared storage abstraction; local and test storage use the same format. */
  private final StorageService storage;

  /** Synthetic candidates and their preset eligibility metadata. */
  private final LocalMealPlanCandidates candidates;

  /** The existing scheduling algorithm. */
  private final MealPlanGenerator generator = new MealPlanGenerator();

  /** Stored data must be one complete JSON value, not concatenated JSON. */
  private final ObjectMapper mapper = new ObjectMapper()
      .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

  /**
   * Connects the local demo inputs to the team's storage abstraction.
   *
   * @param storage    shared storage
   * @param candidates explicit local fixtures
   */
  public MealPlanPostService(
      final StorageService storage,
      final LocalMealPlanCandidates candidates) {
    this.storage = storage;
    this.candidates = candidates;
  }

  /**
   * Generates a complete schedule, then appends one shared MealPlan.
   *
   * Synchronization prevents lost appends within this service instance.
   * This is not a database transaction or a multi-instance storage lock.
   *
   * @param clientId authenticated owning client
   * @param request  validated constraints
   * @return the stored plan, with the UUID used by GET and DELETE
   * @throws IOException if existing data is invalid or cannot be written
   */
  public synchronized MealPlan create(
      final String clientId, final CreateMealPlanRequest request)
      throws IOException {
    if (clientId == null || clientId.isBlank()) {
      throw new IllegalArgumentException("an owning client is required");
    }
    int days = "week".equals(request.timeFrame()) ? 7 : 1;
    List<PlannedDay> schedule = generator.generate(
        candidates.findEligible(request), days,
        BigDecimal.valueOf(request.maxCalories()));
    MealPlan plan = new MealPlan(
        UUID.randomUUID().toString(), clientId, request.timeFrame(),
        request.dietTypes(), request.allergens(), request.maxCalories(),
        request.maxPrepTimeMins(), schedule);

    List<MealPlan> previous = mapper.readValue(
        storage.readFile(PLANS_FILE), new TypeReference<>() {
        });
    if (previous == null || previous.stream().anyMatch(p -> p == null)) {
      throw new IOException("mealplans.json must contain a plan array");
    }
    List<MealPlan> plans = new ArrayList<>(previous);
    plans.add(plan);
    storage.writeFile(PLANS_FILE,
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(plans));
    return plan;
  }
}
