package com.example.fyp;

import static com.example.fyp.WeekViewActivity.REQUEST_CODE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
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

public class BreakfastPopup extends AppCompatActivity {
    private EditText editTextMinCalories;
    private EditText editTextMaxCalories;
    private ActivityResultLauncher<Intent> breakfastRecipesLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breakfast_popup);

        editTextMinCalories = findViewById(R.id.editTextMinCalories);
        editTextMaxCalories = findViewById(R.id.editTextMaxCalories);

        breakfastRecipesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            String selectedRecipe = data.getStringExtra("selectedRecipe");
                            String selectedCalories = data.getStringExtra("selectedCalories");
                            String selectedImageUrl = data.getStringExtra("selectedImageUrl");
                            // Pass the selected recipe and calories back to the WeekViewActivity
                            Intent intent = new Intent();
                            intent.putExtra("selectedRecipe", selectedRecipe);
                            intent.putExtra("selectedCalories", selectedCalories);
                            intent.putExtra("selectedImageUrl", selectedImageUrl);

                            setResult(Activity.RESULT_OK, intent);
                            finish();
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
                    Toast.makeText(BreakfastPopup.this, "Please enter both minimum and maximum calories", Toast.LENGTH_SHORT).show();
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
                new Response.Listener<JSONObject>() {
                    public void onResponse(JSONObject response) {
                        // Handle the API response
                        JSONArray recipes = null;
                        try {
                            recipes = response.getJSONArray("results");
                            Intent intent = new Intent(BreakfastPopup.this, BreakfastRecipesListActivity.class);
                            // Pass the WeekViewActivity instance to BreakfastRecipesListActivity
                            intent.putExtra("recipes", recipes.toString());
                            breakfastRecipesLauncher.launch(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        Toast.makeText(BreakfastPopup.this, "Error fetching recipes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
