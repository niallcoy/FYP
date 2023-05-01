package com.example.fyp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private JSONArray recipesArray;
    private Context context;
    private OnRecipeClickListener onRecipeClickListener;
    private OnRecipeClickListener mListener;

    public RecipeAdapter(JSONArray recipesArray, Context context, OnRecipeClickListener listener) {
        this.recipesArray = recipesArray;
        this.context = context;
        this.onRecipeClickListener = listener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.breakfast_recipe_item, parent, false);
        return new ViewHolder(view, onRecipeClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject recipe = recipesArray.getJSONObject(position);
            String title = recipe.getString("title");
            String imageUrl = recipe.optString("image", "");
            String calories = recipe.optString("calories", "N/A"); // get the calories value from the recipe object

            holder.recipeTitle.setText(title);
            holder.recipeCalories.setText(calories); // set the calories value to the recipeCalories TextView
            Glide.with(context).load(imageUrl).into(holder.recipeImage);
            Log.d("ADAPTER_DATA", "Binding data for position " + position + ": " + recipe.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return recipesArray.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView recipeImage;
        TextView recipeTitle;
        OnRecipeClickListener onRecipeClickListener;
        TextView recipeCalories;

        public ViewHolder(@NonNull View itemView, OnRecipeClickListener onRecipeClickListener) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeTitle = itemView.findViewById(R.id.recipe_title);
            recipeCalories = itemView.findViewById(R.id.recipe_calories);
            this.onRecipeClickListener = onRecipeClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onRecipeClickListener.onRecipeClick(getAdapterPosition());
        }
    }

    public interface OnRecipeClickListener {
        void onRecipeClick(int position);
    }
    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        mListener = listener;
    }
}