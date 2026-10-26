package com.coms4156.mealplanningservice;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coms4156.mealplanningservice.controller.MealPlanPostController;
import com.coms4156.mealplanningservice.service.LocalMealPlanCandidates;
import com.coms4156.mealplanningservice.service.MealPlanPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Verifies ordinary local startup does not expose the synthetic POST. */
@SpringBootTest
@ActiveProfiles({"test", "local"})
@Import(TestConfig.class)
class MealPlanPostProfileTest {

  /** Local web context started without the explicit demo profile. */
  @Autowired
  private WebApplicationContext context;

  @Test
  void postAndSyntheticBeansAreAbsentWithoutTheLocalDemoProfiles()
      throws Exception {
    assertTrue(context.getBeansOfType(LocalMealPlanCandidates.class).isEmpty());
    assertTrue(context.getBeansOfType(MealPlanPostService.class).isEmpty());
    assertTrue(context.getBeansOfType(MealPlanPostController.class).isEmpty());

    MockMvcBuilders.webAppContextSetup(context).build()
        .perform(post("/v1/clients/client-1/mealplan")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"time_frame":"day","max_calories":1800,"max_prep_time_mins":30}
                """))
        .andExpect(status().isNotFound());
  }
}
