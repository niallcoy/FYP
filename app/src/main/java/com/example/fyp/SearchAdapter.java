package com.example.fyp;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private List<Recipe> searchResults;

    public SearchAdapter(List<Recipe> searchResults) {
        this.searchResults = searchResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = searchResults.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    // ViewHolder class for search results
    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView recipeTitleTextView;
        private TextView recipeCaloriesTextView;
        private TextView recipeIngredientsTextView;
        private ImageView recipeImageView;
        private ImageButton addToFavoritesButton;  // Changed to ImageButton for star icon

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeTitleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            recipeCaloriesTextView = itemView.findViewById(R.id.recipeCaloriesTextView);
            recipeIngredientsTextView = itemView.findViewById(R.id.recipeIngredientsTextView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            addToFavoritesButton = itemView.findViewById(R.id.addToFavoritesButton);
        }

        public void bind(final Recipe recipe) {
            Log.d("BIND_METHOD", "Binding recipe: " + recipe.getTitle());
            recipeTitleTextView.setText(recipe.getTitle());
            recipeCaloriesTextView.setText(String.valueOf(recipe.getCalories()));
            recipeIngredientsTextView.setText(String.join(", ", recipe.getIngredients()));
            Glide.with(itemView.getContext())
                    .load(recipe.getImageUrl())
                    .into(recipeImageView);

            addToFavoritesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Save this recipe as a favorite in Firebase
                    FirebaseManager firebaseManager = new FirebaseManager();
                    firebaseManager.saveFavoriteMealForUser(
                            recipe.getId(),
                            recipe.getTitle(),
                            recipe.getIngredients(),
                            recipe.getImageUrl(),
                            recipe.getCalories()
                    );
                }
            });
        }
    }
}
