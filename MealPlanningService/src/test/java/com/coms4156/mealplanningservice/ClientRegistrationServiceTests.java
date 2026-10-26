package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.service.DuplicateClientException;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import com.coms4156.mealplanningservice.service.StorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for MealPlanningService.registerClient, with storage mocked. */
@ExtendWith(MockitoExtension.class)
class ClientRegistrationServiceTests {

  private static final String CLIENTS_JSON =
      "[{\"id\":\"client-1\",\"name\":\"John\",\"apiKey\":\"key-1\"}]";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock private StorageService storageService;

  private MealPlanningService service;

  @BeforeEach
  void setUp() {
    service = new MealPlanningService(storageService);
  }

  @Test
  void registerClientStoresNewClientWithGeneratedIdAndKey() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);

    Client client = service.registerClient("Mary");

    assertEquals("Mary", client.getName());
    UUID.fromString(client.getId());
    UUID.fromString(client.getApiKey());
    assertNotEquals(client.getId(), client.getApiKey());

    List<Client> written = captureWrittenClients();
    assertEquals(2, written.size());
    assertEquals("client-1", written.get(0).getId());
    assertEquals(client.getId(), written.get(1).getId());
    assertEquals("Mary", written.get(1).getName());
    assertEquals(client.getApiKey(), written.get(1).getApiKey());
  }

  @Test
  void registerClientTrimsName() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);

    Client client = service.registerClient("  Mary  ");

    assertEquals("Mary", client.getName());
    assertEquals("Mary", captureWrittenClients().get(1).getName());
  }

  @Test
  void registerClientIntoEmptyFile() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn("[]");

    Client client = service.registerClient("Mary");

    List<Client> written = captureWrittenClients();
    assertEquals(1, written.size());
    assertEquals(client.getId(), written.get(0).getId());
  }

  @Test
  void registerClientGeneratesDifferentKeysForEachClient() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn("[]");

    Client first = service.registerClient("Mary");
    Client second = service.registerClient("Alex");

    assertNotEquals(first.getId(), second.getId());
    assertNotEquals(first.getApiKey(), second.getApiKey());
  }

  @Test
  void registerClientRejectsExactDuplicateName() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);

    assertThrows(
        DuplicateClientException.class, () -> service.registerClient("John"));
    verify(storageService, never()).writeFile(anyString(), anyString());
  }

  @Test
  void registerClientRejectsDuplicateIgnoringCaseAndWhitespace()
      throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);

    assertThrows(
        DuplicateClientException.class,
        () -> service.registerClient("  jOHN "));
    verify(storageService, never()).writeFile(anyString(), anyString());
  }

  @Test
  void registerClientPropagatesReadFailure() throws Exception {
    when(storageService.readFile("clients.json"))
        .thenThrow(new IOException("disk error"));

    assertThrows(IOException.class, () -> service.registerClient("Mary"));
  }

  @Test
  void registerClientPropagatesWriteFailure() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);
    doThrow(new IOException("disk full"))
        .when(storageService).writeFile(eq("clients.json"), anyString());

    assertThrows(IOException.class, () -> service.registerClient("Mary"));
  }

  @Test
  void registeredClientCanAuthenticateWithItsKey() throws Exception {
    when(storageService.readFile("clients.json")).thenReturn(CLIENTS_JSON);
    Client client = service.registerClient("Mary");
    String writtenJson = captureWrittenJson();
    when(storageService.readFile("clients.json")).thenReturn(writtenJson);

    assertEquals(
        client.getId(),
        service.authenticate(client.getApiKey()).orElseThrow().getId());
  }

  private String captureWrittenJson() throws IOException {
    ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);
    verify(storageService).writeFile(eq("clients.json"), content.capture());
    return content.getValue();
  }

  private List<Client> captureWrittenClients() throws IOException {
    return objectMapper.readValue(
        captureWrittenJson(), new TypeReference<List<Client>>() { });
  }
}
