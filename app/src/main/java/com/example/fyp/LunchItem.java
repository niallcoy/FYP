
package com.example.fyp;

public class LunchItem {
    private String title;
    private String imageUrl;
    private String calories;

    public LunchItem( String title, String imageUrl, String calories) {

        this.title = title;
        this.imageUrl = imageUrl;
        this.calories = calories;
    }
    public String getRecipe() {
        return title;
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
