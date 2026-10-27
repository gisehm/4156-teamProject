package com.coms4156.mealplanningservice;

import com.coms4156.mealplanningservice.service.MealPlanningService;
import com.coms4156.mealplanningservice.service.StorageService;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class MealPlanningServiceUnitTests {

  @Autowired
  private MealPlanningService service;

  @Mock
  private StorageService storageService;

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
    // delete mealplan with valid id
    boolean deleteItem1 = service.deleteMealplan("client-1", "plan-1");

    assertEquals(true, deleteItem1);

    // delete mealplan with invalid client id
    boolean deleteItem2 = service.deleteMealplan("client-2", "plan-1");

    assertEquals(false, deleteItem2);

    // delete mealplan with invalid mealplan id
    boolean deleteItem3 = service.deleteMealplan("client-1", "plan-2");

    assertEquals(false, deleteItem3);
  }
}
