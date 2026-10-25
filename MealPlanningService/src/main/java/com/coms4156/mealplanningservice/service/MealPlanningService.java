package com.coms4156.mealplanningservice.service;

import com.coms4156.mealplanningservice.model.Client;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Service for Meal Planning business logic. */
@Service
public class MealPlanningService {

  /** Name of the file that stores the registered clients. */
  private static final String CLIENTS_FILE = "clients.json";

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

  /**
   * Finds the client that owns the given API key.
   *
   * @param apiKey the key from the X-API-Key header, may be null
   * @return the matching client, or empty if the key is missing or unknown
   * @throws IOException if the clients file cannot be read
   */
  public Optional<Client> authenticate(final String apiKey)
      throws IOException {
    if (apiKey == null || apiKey.isBlank()) {
      return Optional.empty();
    }
    List<Client> clients =
        objectMapper.readValue(
            storageService.readFile(CLIENTS_FILE), new TypeReference<>() { });
    return clients.stream()
        .filter(client -> apiKey.equals(client.getApiKey()))
        .findFirst();
  }
}
