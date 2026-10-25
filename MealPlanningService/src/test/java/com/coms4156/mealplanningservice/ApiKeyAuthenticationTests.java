package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import com.coms4156.mealplanningservice.service.StorageService;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for API key authentication, with the storage mocked. */
@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationTests {

  private static final String CLIENTS_JSON =
      "[{\"id\":\"client-1\",\"name\":\"John\",\"apiKey\":\"key-1\"},"
          + "{\"id\":\"client-2\",\"name\":\"Mary\",\"apiKey\":\"key-2\"}]";

  @Mock private StorageService storageService;

  private MealPlanningService service;

  @BeforeEach
  void setUp() {
    service = new MealPlanningService(storageService);
  }

  @Test
  void authenticateWithValidKeyReturnsMatchingClient() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);

    Optional<Client> client = service.authenticate("key-2");

    assertTrue(client.isPresent());
    assertEquals("client-2", client.get().getId());
    assertEquals("Mary", client.get().getName());
  }

  @Test
  void authenticateWithUnknownKeyReturnsEmpty() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);

    assertTrue(service.authenticate("bogus-key").isEmpty());
  }

  @Test
  void authenticateWithNullKeyReturnsEmptyWithoutReadingStorage()
      throws Exception {
    assertTrue(service.authenticate(null).isEmpty());

    verifyNoInteractions(storageService);
  }

  @Test
  void authenticateWithBlankKeyReturnsEmptyWithoutReadingStorage()
      throws Exception {
    assertTrue(service.authenticate("  ").isEmpty());

    verifyNoInteractions(storageService);
  }

  @Test
  void authenticatePropagatesStorageFailure() throws Exception {
    when(storageService.readFile("clients.json"))
        .thenThrow(new IOException("disk error"));

    assertThrows(IOException.class, () -> service.authenticate("key-1"));
  }
}
