package com.example.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchRecipe extends AppCompatActivity {

    private RecyclerView recipesRecyclerView;
    private SearchAdapter searchAdapter;
    private List<Recipe> recipesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipe);

        // Initialize EditText, Button, and RecyclerView
        EditText searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView);

        // Initialize the list and adapter
        recipesList = new ArrayList<>();
        searchAdapter = new SearchAdapter(recipesList);

        // Set up the RecyclerView
        recipesRecyclerView.setAdapter(searchAdapter);
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create an instance of SpoonacularService
        SpoonacularService spoonacularService = new SpoonacularService(this);

        // Set the click listener for the search button
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchQuery = searchEditText.getText().toString();

                // Use the SpoonacularService instance to call searchAllRecipes
                spoonacularService.searchAllRecipes(searchQuery, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("API_RESPONSE", "Received JSON: " + response.toString());
                        // Clear the existing list
                        recipesList.clear();

                        try {
                            // Assume the API response is an object that contains an array under "results"
                            JSONObject fullResponse = new JSONObject(response.toString());
                            if (fullResponse.has("results")) {
                                JSONArray resultsArray = fullResponse.getJSONArray("results");

                                // Clear the existing list
                                recipesList.clear();

                                // Loop through the results and populate recipesList
                                for (int i = 0; i < resultsArray.length(); i++) {
                                    JSONObject recipeJson = resultsArray.getJSONObject(i);

                                    if (recipeJson.has("id")) {
                                        String id = recipeJson.getString("id");
                                        String title = recipeJson.getString("title");
                                        Log.d("API_RESPONSE", "Adding recipe: " + title);

                                        int calories = recipeJson.getInt("calories"); // Assuming calories is an integer
                                        String imageUrl = recipeJson.getString("image");

                                        // Fetch ingredients as a List<String> (if they are available in this API response)
                                        List<String> ingredients = new ArrayList<>();
                                        if (recipeJson.has("ingredients")) {
                                            JSONArray ingredientsArray = recipeJson.getJSONArray("ingredients");
                                            for (int j = 0; j < ingredientsArray.length(); j++) {
                                                JSONObject ingredientObject = ingredientsArray.getJSONObject(j);
                                                String ingredient = ingredientObject.getString("name");
                                                ingredients.add(ingredient);
                                            }
                                        }

                                        // Create a new Recipe object and add it to recipesList
                                        Recipe recipe = new Recipe(id, title, ingredients, "", imageUrl, calories);
                                        recipesList.add(recipe);
                                    }
                                }

                                // Refresh the RecyclerView
                                searchAdapter.notifyDataSetChanged();
                            }

                        } catch (JSONException e) {
                            Log.e("JSON_PARSE_ERROR", "Error parsing JSON: " + e.getMessage());
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("API_ERROR", "Volley Error: " + error.toString());
                        // Handle error, e.g., show a Toast message
                    }
                });
            }
        });

    }

}

