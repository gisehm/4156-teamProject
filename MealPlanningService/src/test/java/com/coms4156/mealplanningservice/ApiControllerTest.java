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

    localStorageService.setDirectory(tempDir);
  }

  @Test
  void contextLoads() {
    // Placeholder so the test class is non-empty. Replace with real tests.
  }
}
