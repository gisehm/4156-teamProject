package com.coms4156.mealplanningservice.model;

import java.util.List;

/** Represents a recipe request. */
public class RecipeRequest {

    /** The client's diet preferences. */
    private List<String> diet;

    /** The meal course. */
    private String course;

    /** The client's allergens. */
    private List<String> allergens;

    /** Default constructor. */
    public RecipeRequest() {
        // This constructor is intentionally empty.
    }

    /**
     * Gets the diet.
     *
     * @return the diet
     */
    public List<String> getDiet() {
        return diet;
    }

    /**
     * Sets the diet.
     *
     * @param diet the diet
     */
    public void setDiet(final List<String> diet) {
        this.diet = diet;
    }

    /**
     * Gets the meal course.
     *
     * @return the meal course
     */
    public String getCourse() {
        return course;
    }

    /**
     * Sets the meal course.
     *
     * @param course the meal course
     */
    public void setCourse(final String course) {
        this.course = course;
    }

    /**
     * Gets the client allergens.
     *
     * @return the client allergens
     */
    public List<String> getAllergens() {
        return allergens;
    }

    /**
     * Sets the client allergens.
     *
     * @param allergens the client allergens
     */
    public void setAllergens(final List<String> allergens) {
        this.allergens = allergens;
    }
}
