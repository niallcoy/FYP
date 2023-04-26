package com.example.fyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.fyp.RecipeAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BreakfastRecipesListActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {
    private RecyclerView recyclerView;
    private JSONArray recipesArray;
    private WeekViewActivity weekViewActivity;

    public BreakfastRecipesListActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breakfast_recipes_list);

        recyclerView = findViewById(R.id.recipes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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



    @Override
    public void onRecipeClick(int position) {
        try {
            JSONObject recipe = recipesArray.getJSONObject(position);
            String title = recipe.getString("title");
            String id = recipe.getString("id");
            String imageUrl = recipe.getString("image");

            SpoonacularService spoonacularService = new SpoonacularService(this);
            spoonacularService.getRecipeDetails(Integer.parseInt(id), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject nutrition = response.getJSONObject("nutrition");
                        JSONArray nutrients = nutrition.getJSONArray("nutrients");
                        String calories = "Unknown";

                        for (int i = 0; i < nutrients.length(); i++) {
                            JSONObject nutrient = nutrients.getJSONObject(i);
                            if (nutrient.getString("title").equals("Calories")) {
                                calories = nutrient.getString("amount");
                                break;
                            }
                        }

                        weekViewActivity.addBreakfastToList(title, calories, imageUrl);

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("selectedRecipe", title);
                        resultIntent.putExtra("selectedCalories", calories);
                        resultIntent.putExtra("selectedImageUrl", imageUrl);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();

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




}
