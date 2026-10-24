package com.coms4156.mealplanningservice;

import com.coms4156.mealplanningservice.service.MealPlanningService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class MealPlanningServiceUnitTests {

  @Autowired
  private MealPlanningService service;

  @Autowired
  private LocalStorageService localStorageService;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() throws Exception {
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
