package com.coms4156.mealplanningservice.model;

/** Request body for registering a new client (POST /v1/clients). */
public final class CreateClientRequest {

  /** The requested client name. */
  private String name;

  /** Default constructor. */
  public CreateClientRequest() {
    // This constructor is intentionally empty.
  }

  /**
   * Constructs a request.
   *
   * @param name the requested client name
   */
  public CreateClientRequest(final String name) {
    this.name = name;
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
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(final String name) {
    this.name = name;
  }
}
