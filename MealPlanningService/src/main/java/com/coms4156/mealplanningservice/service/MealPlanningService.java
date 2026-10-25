package com.coms4156.mealplanningservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/** Service for Meal Planning business logic. */
@Service
public class MealPlanningService {

  /** The object mapper. */
  private final ObjectMapper objectMapper = new ObjectMapper();

  /** The storage service. */
  private final StorageService storageService;

  /**
   * Constructs the service.
   *
   * @param storageService the storage service
   */
  public MealPlanningService(final StorageService storageService) {
    this.storageService = storageService;
  }
}
