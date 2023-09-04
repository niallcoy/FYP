package com.example.fyp;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecipeViewHolder extends RecyclerView.ViewHolder {
    private TextView recipeTitleTextView;
    private Button addToFavoritesButton;

    // Constructor
    public RecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
        addToFavoritesButton = itemView.findViewById(R.id.addToFavoritesButton);
    }

    // Method to bind data to the views
    public void bind(Recipe recipe, RecipeViewHolder.OnRecipeClickListener listener) {
        recipeTitleTextView.setText(recipe.getRecipe());

        addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAddToFavoritesClick(recipe);
                }
            }
        });
    }


    // Define an interface for the click listener
    public interface OnRecipeClickListener {
        void onAddToFavoritesClick(Recipe recipe);
    }
}

