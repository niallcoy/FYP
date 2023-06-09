package com.example.fyp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DinnerPopup extends AppCompatActivity {
    private EditText editTextMinCalories;
    private EditText editTextMaxCalories;
    private ActivityResultLauncher<Intent> dinnerRecipesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_popup);

        editTextMinCalories = findViewById(R.id.dinnerEditTextMinCalories);
        editTextMaxCalories = findViewById(R.id.dinnerEditTextMaxCalories);

        dinnerRecipesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            String recipeUrl = data.getStringExtra("recipeUrl"); // pass the recipeUrl from DinnerRecipesListActivity

                            SpoonacularService spoonacularService = new SpoonacularService(DinnerPopup.this);
                            spoonacularService.getRecipeDetailsByExtract(recipeUrl, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        String title = response.getString("title");
                                        String imageUrl = response.getString("image");
                                        String calories = response.getString("calories"); // Default value when nutrition information is not available

                                        Intent intent = new Intent();
                                        intent.putExtra("selectedRecipe", title);
                                        intent.putExtra("selectedCalories", calories);
                                        intent.putExtra("selectedImageUrl", imageUrl);
                                        intent.putExtra("mealType", "dinner");
                                        setResult(Activity.RESULT_OK, intent);
                                        finish();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // Handle error
                                }
                            });
                        }
                    }
                }
        );

        // Set the OnClickListener for the "OK" button
        Button buttonOk = findViewById(R.id.confirm);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String minCaloriesString = editTextMinCalories.getText().toString();
                String maxCaloriesString = editTextMaxCalories.getText().toString();
                if (minCaloriesString.isEmpty() || maxCaloriesString.isEmpty()) {
                    Toast.makeText(DinnerPopup.this, "Please enter both minimum and maximum calories", Toast.LENGTH_SHORT).show();
                } else {
                    int minCalories = Integer.parseInt(minCaloriesString);
                    int maxCalories = Integer.parseInt(maxCaloriesString);
                    // Search recipes by calories
                    searchRecipesByCalories(minCalories, maxCalories);
                }
            }
        });

        // Set the OnClickListener for the "Cancel" button
        Button buttonCancel = findViewById(R.id.cancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });
    }

    private void searchRecipesByCalories(int minCalories, int maxCalories) {
        SpoonacularService spoonacularService = new SpoonacularService(this);

        spoonacularService.searchRecipesByCalories(minCalories, maxCalories,
                new Response.Listener<JSONArray>() {
                    public void onResponse(JSONArray response) {
                        // Handle the API response
                        List<JSONObject> recipes = new ArrayList<>();
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject recipe = response.getJSONObject(i);
                                recipes.add(recipe);
                            }
                            Intent intent = new Intent(DinnerPopup.this, DinnerRecipesListActivity.class);
                            intent.putExtra("recipes", recipes.toString());
                            intent.putExtra("minCalories", minCalories);
                            intent.putExtra("maxCalories", maxCalories);

                            dinnerRecipesLauncher.launch(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        Toast.makeText(DinnerPopup.this, "Error fetching recipes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}

