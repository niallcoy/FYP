package com.example.fyp;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ViewFavourites extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    private RecyclerView recyclerView;
    private ArrayList<Recipe> recipes;
    private FavouritesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favourites);
        recyclerView = findViewById(R.id.favouritesRecyclerViewId);

        recipes = new ArrayList<>();
        adapter = new FavouritesAdapter(recipes);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users/0F69SKoEhsQoi9aiEFDavrRiJfm1/Favorites");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    recipes.add(recipe);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database errors
            }
        });
    }

    public void popUp(View v) {
        AppUtils.showPopUp((Activity) this, v, this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        AppUtils.handleMenuItemClick(item, this);
        return false;
    }
}
