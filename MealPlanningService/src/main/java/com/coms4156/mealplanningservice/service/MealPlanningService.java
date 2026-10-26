package com.coms4156.mealplanningservice.service;

import com.coms4156.mealplanningservice.model.Client;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  /**
   * Registers a new client with a generated client ID and API key.
   *
   * <p>Synchronized so that two concurrent requests cannot both pass the
   * duplicate-name check before either one writes the clients file.
   *
   * @param name the client name; surrounding whitespace is trimmed
   * @return the stored client, including its generated ID and API key
   * @throws IllegalArgumentException if the name is null or blank
   * @throws DuplicateClientException if the name is already registered,
   *     compared case-insensitively
   * @throws IOException if the clients file cannot be read or written
   */
  public synchronized Client registerClient(final String name)
      throws IOException {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    String trimmedName = name.trim();

    List<Client> clients = readClients();
    boolean nameTaken = clients.stream()
        .map(Client::getName)
        .anyMatch(existing -> existing != null
            && existing.trim().equalsIgnoreCase(trimmedName));
    if (nameTaken) {
      throw new DuplicateClientException(trimmedName);
    }

    Client client = new Client(
        UUID.randomUUID().toString(),
        trimmedName,
        UUID.randomUUID().toString());
    clients.add(client);
    storageService.writeFile(
        CLIENTS_FILE,
        objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(clients));
    return client;
  }

  /**
   * Reads all registered clients from storage.
   *
   * @return a mutable list of clients
   * @throws IOException if the clients file cannot be read
   */
  private List<Client> readClients() throws IOException {
    List<Client> clients = objectMapper.readValue(
        storageService.readFile(CLIENTS_FILE), new TypeReference<>() { });
    return new ArrayList<>(clients);
  }
}
