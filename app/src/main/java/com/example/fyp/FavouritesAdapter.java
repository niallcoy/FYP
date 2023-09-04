package com.example.fyp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.fyp.R;
import com.example.fyp.Recipe;

import java.util.ArrayList;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.ViewHolder> {

    private final ArrayList<Recipe> recipes;

    // Constructor for initializing the list
    public FavouritesAdapter(ArrayList<Recipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);


        holder.recipeTitleTextView.setText(recipe.getRecipe());
        holder.recipeCaloriesTextView.setText(String.valueOf(recipe.getCalories()));


        Glide.with(holder.recipeImageView.getContext())
                .load(recipe.getImageUrl())
                .into(holder.recipeImageView);


        holder.addToFavoritesButton.setOnClickListener(v -> {

        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView recipeImageView;
        public TextView recipeTitleTextView;
        public TextView recipeCaloriesTextView;
        public Button addToFavoritesButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            recipeCaloriesTextView = itemView.findViewById(R.id.recipeCaloriesTextView);
            addToFavoritesButton = itemView.findViewById(R.id.addToFavoritesButton);
        }
    }
}
