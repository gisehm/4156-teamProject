package com.coms4156.mealplanningservice.controller;

import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.model.ClientResponse;
import com.coms4156.mealplanningservice.model.CreateClientRequest;
import com.coms4156.mealplanningservice.service.DuplicateClientException;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
}
