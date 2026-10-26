package com.coms4156.mealplanningservice.controller;

import com.coms4156.mealplanningservice.mealplan.UnfillableMealPlanException;
import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.model.CreateMealPlanRequest;
import com.coms4156.mealplanningservice.service.MealPlanPostService;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Local POST integration using the shared authentication and saved model. */
@RestController
@Profile("local & mealplan-demo")
public final class MealPlanPostController {

  /** The team's existing authentication service. */
  private final MealPlanningService clients;

  /** Generation and shared-plan persistence. */
  private final MealPlanPostService plans;

  /**
   * Constructs the POST entry point without modifying the GET controller.
   *
   * @param clients shared client authentication
   * @param plans shared-model plan creation
   */
  public MealPlanPostController(
      final MealPlanningService clients, final MealPlanPostService plans) {
    this.clients = clients;
    this.plans = plans;
  }

  /**
   * Creates a saved plan for the authenticated client.
   *
   * @param apiKey caller's API key
   * @param clientId path client, which must own the key
   * @param body documented snake_case request JSON
   * @return 201 with shared MealPlan; 400, 401, 403 or 422 on rejection
   * @throws IOException if storage fails, handled as 500 below
   */
  @PostMapping(value = "/v1/clients/{client_id}/mealplan",
      consumes = "application/json")
  public ResponseEntity<?> create(
      @RequestHeader(value = "X-API-Key", required = false)
          final String apiKey,
      @PathVariable("client_id") final String clientId,
      @RequestBody(required = false) final String body) throws IOException {
    Optional<Client> caller = clients.authenticate(apiKey);
    if (caller.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "a valid X-API-Key header is required"));
    }
    if (!clientId.equals(caller.get().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "this API key cannot access that client"));
    }
    CreateMealPlanRequest request;
    try {
      request = CreateMealPlanRequest.parse(body);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
    try {
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(plans.create(clientId, request));
    } catch (UnfillableMealPlanException e) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(Map.of("error", e.getMessage(), "failed_day",
              e.failedDay().name().toLowerCase(Locale.ROOT)));
    }
  }

  /**
   * Reports storage failure without exposing local paths or reporting success.
   *
   * @return an internal-server-error response
   */
  @ExceptionHandler(IOException.class)
  public ResponseEntity<?> storageFailure() {
    return ResponseEntity.internalServerError()
        .body(Map.of("error", "meal plan storage unavailable"));
  }
}
