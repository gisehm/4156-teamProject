package com.coms4156.mealplanningservice.mealplan;

import java.time.DayOfWeek;

/** Represents a request to update a mealplan. */
public class UpdateMealplanRequest {

    /** Mealplan day the client wants to update. */
    private DayOfWeek day;

    /** The meal type the client wants to update. */
    private MealType mealtype;

    /** The recipe ID of the replacing recipe. */
    private String recipeId;

    /**
     * Constructs a request.
     *
     * @param day      the day the client wants to update
     * @param mealtype the meal type the client wants to update
     * @param recipeId the recipe ID of the replacing recipe
     */
    public UpdateMealplanRequest(
            final DayOfWeek day,
            final MealType mealtype,
            final String recipeId) {
        this.day = day;
        this.mealtype = mealtype;
        this.recipeId = recipeId;
    }

    /**
     * Gets the day.
     *
     * @return the day
     */
    public DayOfWeek getDay() {
        return day;
    }

    /**
     * Gets the index for the corresponding day.
     *
     * @return the day index
     */
    public int getDayIndex() {
        return day.getValue() - 1;
    }

    /**
     * Sets the chosen day.
     *
     * @param day the day
     */
    public void setDay(final DayOfWeek day) {
        this.day = day;
    }

    /**
     * Gets the meal type.
     *
     * @return the meal type
     */
    public MealType getMealtype() {
        return mealtype;
    }

    /**
     * Sets the mealtype.
     *
     * @param mealtype the mealtype
     */
    public void setMealtype(final MealType mealtype) {
        this.mealtype = mealtype;
    }

    /**
     * Gets the replacing recipe ID.
     *
     * @return the recipe ID
     */
    public String getRecipeId() {
        return recipeId;
    }
}
