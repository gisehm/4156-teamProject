package com.coms4156.mealplanningservice.mealplan;

import java.time.DayOfWeek;
import java.util.Locale;

/**
 * Signals the first day for which the proposed greedy scheduling rules find no
 * combination.
 */
public final class UnfillableMealPlanException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /** First day that the scheduler could not fill. */
  private final DayOfWeek failedDay;

  /**
   * Creates a scheduling failure for the first unfillable day.
   *
   * @param failedDay the day that could not be filled
   */
  public UnfillableMealPlanException(final DayOfWeek failedDay) {
    super("No feasible meal combination for "
        + failedDay.name().toLowerCase(Locale.ROOT) + ".");
    this.failedDay = failedDay;
  }

  /**
   * Returns the failed day so the HTTP layer can report it in a 422 response.
   *
   * @return the first unfillable day
   */
  public DayOfWeek failedDay() {
    return failedDay;
  }
}
