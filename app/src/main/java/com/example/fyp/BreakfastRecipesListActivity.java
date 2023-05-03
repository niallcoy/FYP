package com.example.fyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BreakfastRecipesListActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private RecyclerView recyclerView;
    private JSONArray recipesArray;
    private WeekViewActivity weekViewActivity;
    private TextView textViewCaloriesRange;
    private int minCalories;
    private int maxCalories;

    public BreakfastRecipesListActivity() {

    }

    @Override
    public void onRecipeClick(int position) {
        // Implement the method logic here
    }
    @Override
    public void onRecipeClick(int position, String title, String calories, String imageUrl) {
        try {
            JSONObject recipe = recipesArray.getJSONObject(position);
            String id = recipe.getString("id");

            SpoonacularService spoonacularService = new SpoonacularService(this);
            spoonacularService.getRecipeDetails(Integer.parseInt(id), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String extractedCalories = "Unknown";
                        if (response.has("nutrition")) {
                            JSONObject nutrition = response.getJSONObject("nutrition");
                            JSONArray nutrients = nutrition.getJSONArray("nutrients");

                            for (int i = 0; i < nutrients.length(); i++) {
                                JSONObject nutrient = nutrients.getJSONObject(i);
                                if (nutrient.getString("title").equals("Calories")) {
                                    extractedCalories = nutrient.getString("amount");
                                    break;
                                }
                            }
                        }

                        weekViewActivity.addBreakfastToList(title, calories, imageUrl);

                        Intent intent = new Intent(BreakfastRecipesListActivity.this, WeekViewActivity.class);
                        startActivity(intent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(BreakfastRecipesListActivity.this, "Error fetching recipe details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breakfast_recipes_list);

        // Get the min and max calories values from the intent extras
        int minCalories = getIntent().getIntExtra("minCalories", 0);
        int maxCalories = getIntent().getIntExtra("maxCalories", 0);

        // Set the text for the textViewCaloriesRange TextView
        textViewCaloriesRange = findViewById(R.id.text_calories_range);
        textViewCaloriesRange.setText("Breakfasts between " + minCalories + " and " + maxCalories);

        // Find the RecyclerView and set its layout manager
        recyclerView = findViewById(R.id.lunch_recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get the recipes array from the intent extras
        String recipesString = getIntent().getStringExtra("recipes");
        try {
            recipesArray = new JSONArray(recipesString);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Get the WeekViewActivity instance from the static currentInstance variable
        weekViewActivity = WeekViewActivity.currentInstance;

        // Create an instance of RecipeAdapter and set it as the adapter for the RecyclerView
        RecipeAdapter recipeAdapter = new RecipeAdapter(recipesArray, BreakfastRecipesListActivity.this, BreakfastRecipesListActivity.this);
        recyclerView.setAdapter(recipeAdapter);
    }



}


