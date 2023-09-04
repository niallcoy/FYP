package com.example.fyp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SearchRecipe extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private EditText searchEditText;
    private Button searchButton;

    // Initialize Firestore and FirebaseAuth instances
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipe);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        recyclerView = findViewById(R.id.recipesRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with an empty JSONArray
        //recipeAdapter = new RecipeAdapter(new JSONArray(), this);
        recyclerView.setAdapter(recipeAdapter);

        SpoonacularService spoonacularService = new SpoonacularService(this);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                spoonacularService.searchRecipesByQuery(query, new Response.Listener<List<Recipe>>() {
                    @Override
                    public void onResponse(List<Recipe> recipes) {
                        JSONArray jsonArray = new JSONArray();
                        for (Recipe recipe : recipes) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                //jsonObject.put("title", recipe.getTitle());
                                jsonObject.put("image", recipe.getImageUrl());
                                jsonObject.put("calories", recipe.getCalories());
                                jsonArray.put(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        recipeAdapter.updateData(jsonArray);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error here
                    }
                });
            }
        });

        // Sample code for adding a favorite recipe
        Recipe recipe = new Recipe("Brussels Sprout Carbonara with Fettuccini", "https://spoonacular.com/recipeImages/636360-312x231.jpg", 549);
        addFavorite(recipe);
    }

    @Override
    public void onRecipeClick(int position) {
        // Handle click
        // You can remove this if you implement the below method for handling clicks with more details
    }

    @Override
    public void onRecipeClick(int position, String title, String calories, String imageUrl) {
        // Handle click with more details
    }

    public void addFavorite(Recipe recipe) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .add(recipe)
                    .addOnSuccessListener(documentReference -> Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.w("Firestore", "Error adding document", e));
        } else {
            Log.w("Firestore", "User not logged in");
        }
    }
    //public void popUp(View v) {
       // AppUtils.showPopUp(this, v, this);
   // }

//    @Override
//    public boolean onMenuItemClick(MenuItem item) {
//        AppUtils.handleMenuItemClick(item, this);
//        return true;
//    }
}
