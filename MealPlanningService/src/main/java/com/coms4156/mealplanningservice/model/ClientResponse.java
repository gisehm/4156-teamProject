package com.coms4156.mealplanningservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response body returned after registering a client.
 *
 * Kept separate from {@link Client} so the API can use snake_case field
 * names (client_id, api_key) without changing the stored clients.json format.
 */
public final class ClientResponse {

  /** The generated client ID. */
  @JsonProperty("client_id")
  private final String clientId;

  /** The client name. */
  @JsonProperty("name")
  private final String name;

  /** The generated API key. */
  @JsonProperty("api_key")
  private final String apiKey;

  /**
   * Constructs a response.
   *
   * @param clientId the client ID
   * @param name     the client name
   * @param apiKey   the API key
   */
  public ClientResponse(
      final String clientId, final String name, final String apiKey) {
    this.clientId = clientId;
    this.name = name;
    this.apiKey = apiKey;
  }

  /**
   * Builds a response from a stored client.
   *
   * @param client the stored client
   * @return the response
   */
  public static ClientResponse from(final Client client) {
    return new ClientResponse(
        client.getId(), client.getName(), client.getApiKey());
  }

  /**
   * Gets the client ID.
   *
   * @return the client ID
   */
  public String getClientId() {
    return clientId;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the API key.
   *
   * @return the API key
   */
  public String getApiKey() {
    return apiKey;
  }
}
