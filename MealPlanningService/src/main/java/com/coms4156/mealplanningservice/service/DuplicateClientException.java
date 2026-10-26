package com.coms4156.mealplanningservice.service;

/** Thrown when registering a client whose name is already taken. */
public final class DuplicateClientException extends RuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs the exception.
   *
   * @param name the name that is already registered
   */
  public DuplicateClientException(final String name) {
    super("Client name already registered: " + name);
  }
}
