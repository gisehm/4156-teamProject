package com.coms4156.mealplanningservice;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
class ApiControllerTest {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private LocalStorageService localStorageService;

  private MockMvc mockMvc;

  @TempDir
  Path tempDir;

  protected static final String VALID_KEY = "valid-key";

  @BeforeEach
  void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

    Files.writeString(
        tempDir.resolve("clients.json"),
        "[{\"id\":\"client-1\",\"name\":\"John\",\"apiKey\":\"valid-key\"}]");
    Files.writeString(
        tempDir.resolve("food_recipes.json"),
        "[{\"name\":\"recipe-1\",\"url\":\"food.com\",\"description\":\"info\",\"course\":\"lunch\",\"diet\":\"Vegetarian\",\"prep_time\":\"10"
            + " M\",\"cook_time\":\"20"
            + " M\",\"ingredients\":\"Onions|Ginger|Garlic\",\"instructions\":\"steps\"}]");
    Files.writeString(
        tempDir.resolve("mealplans.json"),
        "[{\"id\":\"plan-1\",\"clientId\":\"client-1\",\"timeFrame\":\"week\","
            + "\"diet\":[\"Vegetarian\"],\"allergens\":[\"peanuts\"],"
            + "\"maxCalories\":1800,\"maxPrepTimeMins\":40,\"schedule\":["
            + "{\"day\":\"MONDAY\","
            + "\"breakfast\":{\"sourceRecipeId\":\"r1\","
            + "\"mealType\":\"BREAKFAST\",\"calories\":400,"
            + "\"totalTimeMinutes\":10},"
            + "\"lunch\":{\"sourceRecipeId\":\"r2\",\"mealType\":\"LUNCH\","
            + "\"calories\":600,\"totalTimeMinutes\":20},"
            + "\"dinner\":{\"sourceRecipeId\":\"r3\",\"mealType\":\"DINNER\","
            + "\"calories\":700,\"totalTimeMinutes\":30}}]},"
            + "{\"id\":\"plan-2\",\"clientId\":\"client-2\","
            + "\"timeFrame\":\"day\",\"schedule\":[]}]");

    localStorageService.setDirectory(tempDir);
  }

  @Test
  void testDeleteItem() throws Exception {
    // valid id, successful delete
    mockMvc.perform(delete("/v1/clients/{client_id}/mealplan/{mealplan_id}", "client-1", "plan-1")
        .header("X-API-Key", "valid-key"))
        .andExpect(status().isNoContent());

    // invalid api key, fails
    mockMvc.perform(delete("/v1/clients/{client_id}/mealplan/{mealplan_id}", "client-1", "plan-1")
        .header("X-API-Key", "invalid-key"))
        .andExpect(status().isNotFound());

    // invalid item id, fails
    mockMvc.perform(delete("/v1/clients/{client_id}/mealplan/{mealplan_id}", "client-1", "plan-1")
        .header("X-API-Key", "valid-key"))
        .andExpect(status().isNotFound());
  }
}
