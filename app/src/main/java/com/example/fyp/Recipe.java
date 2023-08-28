package com.example.fyp;

import java.util.List;

public class Recipe {
    private String id;
    private String title;
    private List<String> ingredients;
    private String instructions;
    private String imageUrl;
    private int calories;

    // Default constructor
    public Recipe() {
    }

    // Parameterized constructor
    public Recipe(String id, String title, List<String> ingredients, String instructions, String imageUrl, int calories) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageUrl = imageUrl;
        this.calories = calories;
    }

    // Getters and setters for each field
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
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
    public void setCalories(int calories){
        this.calories = calories;
    }
}



