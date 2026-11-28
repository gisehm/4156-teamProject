package com.coms4156.mealplanningservice;

import com.coms4156.mealplanningservice.service.MealPlanningService;
import com.coms4156.mealplanningservice.mealplan.MealType;
import com.coms4156.mealplanningservice.mealplan.UpdateMealplanRequest;
import com.coms4156.mealplanningservice.model.MealPlan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.DayOfWeek;
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

    /** MealPlanningService instance to use for testing. */
    @Autowired
    private MealPlanningService service;

    /** Local storage implementation supplied by the test configuration. */
    @Autowired
    private LocalStorageService localStorageService;

    /** Isolated storage location recreated for every test. */
    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Files.writeString(
            tempDir.resolve("clients.json"),
            "[{\"id\":\"client-1\",\"name\":\"John\",\"apiKey\":\"valid-key\""
                    + "}]");
        Files.writeString(
            tempDir.resolve("food_recipes.json"),
            "[{\"id\":\"recipe-1\",\"name\":\"name-1\",\"url\":\"food.com\","
                + "\"description\":\"info\",\"course\":\"Breakfast\","
                + "\"diet\":\"Vegetarian\",\"total_time\":30,"
                + "\"ingredients\":\"Onions|Ginger|Garlic\","
                + "\"instructions\":\"steps\"}, {\"id\":\"recipe-2\","
                + "\"name\":\"name-2\",\"url\":\"food.com\","
                + "\"description\":\"info\",\"course\":\"Lunch\","
                + "\"diet\":\"Vegetarian\",\"total_time\":30,"
                + "\"ingredients\":\"Onions|Ginger|Garlic\","
                + "\"instructions\":\"steps\"},{\"id\":\"recipe-3\","
                + "\"name\":\"name-3\",\"url\":\"food.com\","
                + "\"description\":\"info\",\"course\":\"Dinner\","
                + "\"diet\":\"Vegetarian\",\"total_time\":30,"
                + "\"ingredients\":\"Onions|Ginger|Garlic\","
                + "\"instructions\":\"steps\"}]");
        Files.writeString(
            tempDir.resolve("mealplans.json"),
            "[{\"id\":\"plan-1\",\"clientId\":\"client-1\","
                + "\"timeFrame\":\"week\", \"diet\":[\"Vegetarian\"],"
                + "\"allergens\":[\"peanuts\"],\"maxCalories\":1800,"
                + "\"maxPrepTimeMins\":40,\"schedule\":[{\"day\":\"MONDAY\","
                + "\"breakfast\":{\"sourceRecipeId\":\"recipe-4\","
                + "\"mealType\":\"BREAKFAST\",\"calories\":400,"
                + "\"totalTimeMinutes\":10},\"lunch\":{\"sourceRecipeId\":"
                + "\"recipe-5\",\"mealType\":\"LUNCH\", \"calories\":600,"
                + "\"totalTimeMinutes\":20}, \"dinner\":{\"sourceRecipeId\":"
                + "\"recipe-6\",\"mealType\":\"DINNER\", \"calories\":700,"
                + "\"totalTimeMinutes\":30}}]}, {\"id\":\"plan-2\","
                + "\"clientId\":\"client-2\",\"timeFrame\":\"day\","
                + "\"schedule\":[]}]");

        localStorageService.setDirectory(tempDir);
    }

    @Test
    void testDeleteMealplan() throws Exception {
        // delete mealplan with valid id
        boolean deleteMealplan1 = service.deleteMealplan("client-1", "plan-1");

        assertEquals(true, deleteMealplan1);

        // delete mealplan with invalid client id
        boolean deleteMealplan2 = service.deleteMealplan("client-2", "plan-1");

        assertEquals(false, deleteMealplan2);

        // delete mealplan with invalid mealplan id
        boolean deleteMealplan3 = service.deleteMealplan("client-1", "plan-2");

        assertEquals(false, deleteMealplan3);
    }

    @Test
    void testUpdateMealplan() throws Exception {
        // successfully replace breakfast recipe in a mealplan
        UpdateMealplanRequest request1 = new UpdateMealplanRequest(
                DayOfWeek.MONDAY, MealType.BREAKFAST, "recipe-1");

        MealPlan mealplan1 = service.updateMealplan("client-1", "plan-1",
                request1);

        assertEquals("recipe-1", mealplan1.getSchedule()
                .get(request1.getDayIndex()).breakfast().sourceRecipeId());
        assertEquals(MealType.BREAKFAST, mealplan1.getSchedule()
                .get(request1.getDayIndex()).breakfast().mealType());
        assertEquals(30, mealplan1.getSchedule()
                .get(request1.getDayIndex()).breakfast().totalTimeMinutes());

        // successfully replace lunch recipe in a mealplan
        UpdateMealplanRequest request2 = new UpdateMealplanRequest(
                DayOfWeek.MONDAY, MealType.LUNCH, "recipe-2");

        MealPlan mealplan2 = service.updateMealplan("client-1", "plan-1",
                request2);

        assertEquals("recipe-2", mealplan2.getSchedule()
                .get(request2.getDayIndex()).lunch().sourceRecipeId());
        assertEquals(MealType.LUNCH, mealplan2.getSchedule()
                .get(request2.getDayIndex()).lunch().mealType());
        assertEquals(30, mealplan2.getSchedule().get(request2.getDayIndex())
                .lunch().totalTimeMinutes());

        // successfully replace dinner recipe in a mealplan
        UpdateMealplanRequest request3 = new UpdateMealplanRequest(
                DayOfWeek.MONDAY, MealType.DINNER, "recipe-3");

        MealPlan mealplan3 = service.updateMealplan("client-1", "plan-1",
                request3);

        assertEquals("recipe-3", mealplan3.getSchedule()
                .get(request3.getDayIndex()).dinner().sourceRecipeId());
        assertEquals(MealType.DINNER, mealplan3.getSchedule()
                .get(request3.getDayIndex()).dinner().mealType());
        assertEquals(30, mealplan3.getSchedule().get(request3.getDayIndex())
                .dinner().totalTimeMinutes());

        // invalid client id, valid mealplan id, and valid request
        MealPlan mealplan4 = service.updateMealplan("client-2", "plan-1",
                request1);

        assertNull(mealplan4);

        // valid client id, invalid mealplan id, and valid request
        MealPlan mealplan5 = service.updateMealplan("client-1", "plan-2",
                request1);

        assertNull(mealplan5);

        // valid ids and invalid request (invalid recipe ID)
        UpdateMealplanRequest request4 = new UpdateMealplanRequest(
                DayOfWeek.MONDAY, MealType.LUNCH, "invalid");

        MealPlan mealplan6 = service.updateMealplan("client-1", "plan-1",
                request4);

        assertNull(mealplan6);

        // valid client id, valid mealplan id, and null request
        MealPlan mealplan7 = service.updateMealplan("client-1", "plan-1", null);

        assertNull(mealplan7);
    }
}
