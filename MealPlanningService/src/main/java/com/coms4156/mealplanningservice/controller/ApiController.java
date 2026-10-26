package com.coms4156.mealplanningservice.controller;

import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.model.ClientResponse;
import com.coms4156.mealplanningservice.model.CreateClientRequest;
import com.coms4156.mealplanningservice.service.DuplicateClientException;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for the Meal Planning Service. */
@RestController
@RequestMapping("/v1")
public final class ApiController {

  /** The meal planning service. */
  private final MealPlanningService mealPlanningService;

  /**
   * Creates a new ApiController with the meal planning service.
   *
   * @param mealPlanningService the meal planning service
   */
  public ApiController(final MealPlanningService mealPlanningService) {
    this.mealPlanningService = mealPlanningService;
  }

  /**
   * Registers a new client. This is the only endpoint that does not require
   * an X-API-Key header.
   *
   * @param request the request body containing the client name
   * @return 201 with client_id, name and api_key; 400 if the name is missing
   *     or blank; 409 if the name is already registered
   * @throws IOException if the clients file cannot be read or written,
   *     which Spring maps to 500
   */
  @PostMapping("/clients")
  public ResponseEntity<?> registerClient(
      @RequestBody(required = false) final CreateClientRequest request)
      throws IOException {
    if (request == null
        || request.getName() == null
        || request.getName().isBlank()) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", "name is required and must not be blank"));
    }
    try {
      Client client = mealPlanningService.registerClient(request.getName());
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(ClientResponse.from(client));
    } catch (DuplicateClientException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("error", e.getMessage()));
    }
  }

  /**
   * Retrieves a saved meal plan. The API key must belong to the client in
   * the path, and the plan must belong to that client.
   *
   * @param apiKey the X-API-Key header value
   * @param clientId the ID of the client that owns the meal plan
   * @param mealPlanId the ID of the meal plan to retrieve
   * @return 200 with the meal plan; 401 if the API key is missing or
   *     unknown; 403 if the key belongs to a different client; 404 if this
   *     client has no meal plan with that ID
   * @throws IOException if a storage file cannot be read, which Spring maps
   *     to 500
   */
  @GetMapping("/clients/{client_id}/mealplan/{mealplan_id}")
  public ResponseEntity<?> getMealPlan(
      @RequestHeader(value = "X-API-Key", required = false)
          final String apiKey,
      @PathVariable("client_id") final String clientId,
      @PathVariable("mealplan_id") final String mealPlanId)
      throws IOException {
    Optional<Client> caller = mealPlanningService.authenticate(apiKey);
    if (caller.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "a valid X-API-Key header is required"));
    }
    if (!clientId.equals(caller.get().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "this API key cannot access that client"));
    }
    return mealPlanningService.findMealPlan(clientId, mealPlanId)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "meal plan not found")));
  }
}
