package com.example.fyp;

public class BreakfastItem {
    private String title;
    private String imageUrl;
    private String calories;

    public BreakfastItem(String title, String imageUrl, String calories) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.calories = calories;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCalories() {
        return calories;
    }
}

