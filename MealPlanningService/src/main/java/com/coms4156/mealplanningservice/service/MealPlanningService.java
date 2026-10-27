package com.coms4156.mealplanningservice.service;

import com.coms4156.mealplanningservice.model.CatalogRecipe;
import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.model.FindRecipeRequest;
import com.coms4156.mealplanningservice.model.MealPlan;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Service for Meal Planning business logic. */
@Service
public class MealPlanningService {

  /** Name of the file that stores the registered clients. */
  private static final String CLIENTS_FILE = "clients.json";

  /** Name of the file that stores the saved meal plans. */
  private static final String MEAL_PLANS_FILE = "mealplans.json";

  /** Name of the file that stores the recipe catalog. */
  private static final String CATALOG_FILE = "food_recipes.json";

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
   * Finds a saved meal plan that belongs to the given client.
   *
   * <p>A plan owned by a different client is treated as not found, so one
   * client cannot learn whether another client's plan ID exists.
   *
   * @param clientId the ID of the client that must own the plan
   * @param mealPlanId the ID of the meal plan to find
   * @return the meal plan, or empty if the client has no plan with that ID
   * @throws IOException if the meal plans file cannot be read
   */
  public Optional<MealPlan> findMealPlan(
      final String clientId, final String mealPlanId) throws IOException {
    if (clientId == null || mealPlanId == null) {
      return Optional.empty();
    }
    List<MealPlan> mealPlans =
        objectMapper.readValue(
            storageService.readFile(MEAL_PLANS_FILE),
            new TypeReference<>() { });
    return mealPlans.stream()
        .filter(plan -> clientId.equals(plan.getClientId()))
        .filter(plan -> mealPlanId.equals(plan.getId()))
        .findFirst();
  }

  /**
   * Registers a new client with a generated client ID and API key.
   *
   * Synchronized so that two concurrent requests cannot both pass the
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

  /**
   * Finds the catalog recipe that best matches the given constraints.
   *
   * A candidate must match the meal type (ignoring case), take at most
   * max_prep_time_mins, contain none of the allergens and satisfy every
   * diet. Recipes with a missing (zero) total time are skipped. Among the
   * candidates, the one with the shortest total time wins, with the recipe
   * ID as a tie-break. max_calories is validated but not enforced until
   * nutrition data is available.
   *
   * @param request the recipe constraints
   * @return the best matching recipe, or empty if no recipe matches
   * @throws IllegalArgumentException if the request is invalid
   * @throws IOException if the catalog file cannot be read
   */
  public Optional<CatalogRecipe> findRecipe(final FindRecipeRequest request)
      throws IOException {
    validateFindRecipeRequest(request);
    String mealType = request.getMealType().trim();
    Integer maxPrepTimeMins = request.getMaxPrepTimeMins();
    List<String> diets = valueOrEmpty(request.getDietTypes());
    List<String> allergens = valueOrEmpty(request.getAllergens());

    return readCatalog().stream()
        .filter(recipe -> mealType.equalsIgnoreCase(recipe.getCourse()))
        .filter(recipe -> recipe.getTotalTimeMins() > 0)
        .filter(recipe -> maxPrepTimeMins == null
            || recipe.getTotalTimeMins() <= maxPrepTimeMins)
        .filter(recipe ->
            AllergenChecker.isFreeOf(recipe.ingredientList(), allergens))
        .filter(recipe -> DietChecker.satisfiesAll(recipe, diets))
        .min(Comparator.comparingInt(CatalogRecipe::getTotalTimeMins)
            .thenComparing(CatalogRecipe::getId));
  }

  /**
   * Checks that a find-recipe request is complete and uses supported tags.
   *
   * @param request the recipe constraints
   * @throws IllegalArgumentException if the request is invalid
   */
  private void validateFindRecipeRequest(final FindRecipeRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("a JSON request body is required");
    }
    if (request.getMealType() == null || request.getMealType().isBlank()) {
      throw new IllegalArgumentException("meal_type is required");
    }
    requirePositive(request.getMaxCalories(), "max_calories");
    requirePositive(request.getMaxPrepTimeMins(), "max_prep_time_mins");
    // Null tags are checked first: Set.of(...).contains(null) throws.
    for (String diet : valueOrEmpty(request.getDietTypes())) {
      if (diet == null || !DietChecker.supportedDiets().contains(diet)) {
        throw new IllegalArgumentException("unsupported diet_type: " + diet);
      }
    }
    for (String allergen : valueOrEmpty(request.getAllergens())) {
      if (allergen == null
          || !AllergenChecker.supportedAllergens().contains(allergen)) {
        throw new IllegalArgumentException(
            "unsupported allergen: " + allergen);
      }
    }
  }

  /**
   * Checks that an optional number is positive when it is given.
   *
   * @param value the number, may be null
   * @param field the request field name, used in the error message
   * @throws IllegalArgumentException if the number is zero or negative
   */
  private static void requirePositive(final Integer value, final String field) {
    if (value != null && value <= 0) {
      throw new IllegalArgumentException(field + " must be positive");
    }
  }

  /**
   * Returns the list, or an empty list if it is null.
   *
   * @param values the list, may be null
   * @return the list, or an empty list
   */
  private static List<String> valueOrEmpty(final List<String> values) {
    return values == null ? List.of() : values;
  }

  /**
   * Reads all recipes from the catalog file.
   *
   * @return the catalog recipes
   * @throws IOException if the catalog file cannot be read
   */
  private List<CatalogRecipe> readCatalog() throws IOException {
    return objectMapper.readValue(
        storageService.readFile(CATALOG_FILE), new TypeReference<>() { });
  }
}
