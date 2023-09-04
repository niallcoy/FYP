package com.example.fyp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class SearchRecipeAdapter extends RecyclerView.Adapter<SearchRecipeAdapter.ViewHolder> {

    private List<Recipe> recipeList;

    public SearchRecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public SearchRecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchRecipeAdapter.ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.titleTextView.setText(recipe.getRecipe());
        holder.caloriesTextView.setText(String.valueOf(recipe.getCalories()));


        Glide.with(holder.recipeImageView.getContext())
                .load(recipe.getImageUrl())
                .into(holder.recipeImageView);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView caloriesTextView;
        ImageView recipeImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.recipeTitleTextView);
            caloriesTextView = itemView.findViewById(R.id.recipeCaloriesTextView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
        }
    }

    public void updateData(List<Recipe> newRecipes) {
        this.recipeList.clear();
        this.recipeList.addAll(newRecipes);
        notifyDataSetChanged();
    }
}
