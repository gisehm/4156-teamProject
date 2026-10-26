package com.coms4156.mealplanningservice;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coms4156.mealplanningservice.controller.ApiController;
import com.coms4156.mealplanningservice.model.Client;
import com.coms4156.mealplanningservice.service.DuplicateClientException;
import com.coms4156.mealplanningservice.service.MealPlanningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Controller tests for POST /v1/clients, with MealPlanningService mocked. */
@ExtendWith(MockitoExtension.class)
class ClientRegistrationControllerTests {

  @Mock private MealPlanningService mealPlanningService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new ApiController(mealPlanningService))
        .build();
  }

  @Test
  void registerClientReturns201WithSnakeCaseFields() throws Exception {
    when(mealPlanningService.registerClient("Mary"))
        .thenReturn(new Client("id-1", "Mary", "key-1"));

    mockMvc.perform(post("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Mary\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.client_id").value("id-1"))
        .andExpect(jsonPath("$.name").value("Mary"))
        .andExpect(jsonPath("$.api_key").value("key-1"))
        .andExpect(jsonPath("$.id").doesNotExist())
        .andExpect(jsonPath("$.apiKey").doesNotExist());
  }

  @Test
  void registerClientDoesNotRequireApiKey() throws Exception {
    when(mealPlanningService.registerClient("Mary"))
        .thenReturn(new Client("id-1", "Mary", "key-1"));

    mockMvc.perform(post("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Mary\"}"))
        .andExpect(status().isCreated());

    verify(mealPlanningService).registerClient("Mary");
  }

  @Test
  void registerClientReturns409WhenNameTaken() throws Exception {
    when(mealPlanningService.registerClient("John"))
        .thenThrow(new DuplicateClientException("John"));

    mockMvc.perform(post("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"John\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error")
            .value("Client name already registered: John"));
  }

  @Test
  void registerClientReturns400WhenNameMissing() throws Exception {
    mockMvc.perform(post("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());

    verifyNoInteractions(mealPlanningService);
  }

  @Test
  void registerClientReturns400WhenNameBlank() throws Exception {
    mockMvc.perform(post("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"   \"}"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mealPlanningService);
  }

  @Test
  void registerClientReturns400WhenJsonMalformed() throws Exception {
    mockMvc.perform(post("/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":"))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(mealPlanningService);
  }
}
