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
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestConfig.class)
class ApiControllerTest {

        /**
         * Real Spring web context containing both DELETE and
         * PATCH controllers.
         */
        @Autowired
        private WebApplicationContext context;

        /** Local storage implementation supplied by the test configuration. */
        @Autowired
        private LocalStorageService localStorageService;

        /** Request dispatcher backed by the real Spring web context. */
        private MockMvc mockMvc;

        /** Isolated storage location recreated for every test. */
        @TempDir
        private Path tempDir;

        /** Valid API Key to be used in tests. */
        protected static final String VALID_KEY = "valid-key";

        @BeforeEach
        void setUp() throws Exception {
                mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

                Files.writeString(
                        tempDir.resolve("clients.json"),
                        "[{\"id\":\"client-1\",\"name\":\"John\","
                        + "\"apiKey\":\"valid-key\"}]");
                Files.writeString(
                        tempDir.resolve("food_recipes.json"),
                        "[{\"id\":\"recipe-1\",\"name\":\"name-1\","
                                + "\"url\":\"food.com\",\"description\":\"info"
                                + "\",\"course\":\"Breakfast\","
                                + "\"diet\":\"Vegetarian\",\"total_time\":30,"
                                + "\"ingredients\":\"Onions|Ginger|Garlic\","
                                + "\"instructions\":\"steps\"}, {\"id\":"
                                + "\"recipe-2\",\"name\":\"name-2\",\"url\""
                                + ":\"food.com\",\"description\":\"info\","
                                + "\"course\":\"Lunch\",\"diet\":\"Vegetarian\""
                                + ",\"total_time\":30,\"ingredients\":\"Onions"
                                + "|Ginger|Garlic\",\"instructions\":\"steps\"}"
                                + ", {\"id\":\"recipe-3\",\"name\":\"name-3\","
                                + "\"url\":\"food.com\",\"description\":\"info"
                                + "\",\"course\":\"Dinner\","
                                + "\"diet\":\"Vegetarian\",\"total_time\":30,"
                                + "\"ingredients\":\"Onions|Ginger|Garlic\","
                                + "\"instructions\":\"steps\"}]");
                Files.writeString(
                        tempDir.resolve("mealplans.json"),
                        "[{\"id\":\"plan-1\",\"clientId\":\"client-1\","
                                + "\"timeFrame\":\"week\",\"diet\":["
                                + "\"Vegetarian\"],\"allergens\":[\"peanuts\"],"
                                + "\"maxCalories\":1800,\"maxPrepTimeMins\":40,"
                                + "\"schedule\":[{\"day\":\"MONDAY\","
                                + "\"breakfast\":{\"sourceRecipeId\":\"recipe-4"
                                + "\",\"mealType\":\"BREAKFAST\",\"calories\""
                                + ":400,\"totalTimeMinutes\":10},"
                                + "\"lunch\":{\"sourceRecipeId\":\"recipe-5\","
                                + "\"mealType\":\"LUNCH\",\"calories\":600,"
                                + "\"totalTimeMinutes\":20},\"dinner\":{"
                                + "\"sourceRecipeId\":\"recipe-6\",\"mealType"
                                + "\":\"DINNER\",\"calories\":700,"
                                + "\"totalTimeMinutes\":30}}]},{\"id\":\""
                                + "plan-2\",\"clientId\":\"client-2\","
                                + "\"timeFrame\":\"day\",\"schedule\":[]}]");

                localStorageService.setDirectory(tempDir);
        }

        @Test
        void testDeleteMealplan() throws Exception {
                // valid ids & api key, successful delete
                mockMvc.perform(delete(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isNoContent());

                // valid ids & missing api key, fails
                mockMvc.perform(delete(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .header("X-API-Key", ""))
                                .andExpect(status().isUnauthorized());

                // valid ids & invalid api key, fails
                mockMvc.perform(delete(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .header("X-API-Key", "invalid-key"))
                                .andExpect(status().isUnauthorized());

                // valid api key & invalid mealplan id, fails
                mockMvc.perform(delete(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-2")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isNotFound());

                // valid api key & invalid client id, fails
                mockMvc.perform(delete(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-3", "plan-1")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isForbidden());

                // valid api key & forbidden client id, fails
                mockMvc.perform(delete(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-2", "plan-1")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testUpdateMealplan() throws Exception {

                // valid ids + request & api key, successful breakfast update
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"recipe-1\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isOk());

                // valid ids + request & api key, successful lunch update
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"LUNCH\","
                                + "\"recipeId\":\"recipe-2\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isOk());

                // valid ids + request & api key, successful dinner update
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"DINNER\","
                                + "\"recipeId\":\"recipe-3\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isOk());

                // valid ids + request & missing api key, fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"recipe-1\"}")
                                .header("X-API-Key", ""))
                                .andExpect(status().isUnauthorized());

                // valid ids + request & invalid api key, fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"recipe-1\"}")
                                .header("X-API-Key", "invalid-key"))
                                .andExpect(status().isUnauthorized());

                // valid api key & invalid mealplan id, fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"recipe-1\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isNotFound());

                // valid api key & invalid client id, fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-3", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"recipe-1\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isForbidden());

                // valid api key & forbidden client id, fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-2", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"recipe-1\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isForbidden());

                // valid inputs & invalid request (invalid recipe id), fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"invalid-id\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isNotFound());

                // valid inputs & invalid request (empty recipe id), fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"day\":\"MONDAY\","
                                + "\"mealtype\":\"BREAKFAST\","
                                + "\"recipeId\":\"\"}")
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isNotFound());

                // valid ids + valid api key & null request, fails
                mockMvc.perform(patch(
                        "/v1/clients/{client_id}/mealplan/{mealplan_id}",
                        "client-1", "plan-1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-Key", "valid-key"))
                                .andExpect(status().isBadRequest());
        }
}
