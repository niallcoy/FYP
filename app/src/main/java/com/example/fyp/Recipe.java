package com.example.fyp;

public class Recipe {

    private String recipe;
    private String imageUrl;
    private int calories;

    // Default constructor
    public Recipe() {
    }

    // Parameterized constructor
    public Recipe(String recipe, String imageUrl, int calories) {
        this.recipe = recipe;
        this.imageUrl = imageUrl;
        this.calories = calories;
    }

    // Getters and setters

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
}
