package com.example.fyp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BreakfastListAdapter extends ArrayAdapter<BreakfastItem> {
    private Context context;
    private List<BreakfastItem> breakfastItems;

    public BreakfastListAdapter(Context context, List<BreakfastItem> breakfastItems) {
        super(context, 0, breakfastItems);
        this.context = context;
        this.breakfastItems = breakfastItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.breakfast_recipe_item, parent, false);
        }

        BreakfastItem breakfastItem = getItem(position);

        ImageView breakfastImage = convertView.findViewById(R.id.recipe_image);
        TextView breakfastTitle = convertView.findViewById(R.id.recipe_title);
        // TextView breakfastCalories = convertView.findViewById(R.id.recipe_calories);

        breakfastTitle.setText(breakfastItem.getTitle());
        //breakfastCalories.setText(breakfastItem.getCalories());
        Glide.with(context).load(breakfastItem.getImageUrl()).into(breakfastImage);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the selected recipe and calories back to the WeekViewActivity
                WeekViewActivity activity = (WeekViewActivity) context;
                activity.addBreakfastToList(breakfastItem.getTitle(), breakfastItem.getCalories(), breakfastItem.getImageUrl());
            }
        });

        return convertView;
    }

    public void setBreakfastItems(List<BreakfastItem> breakfastItems) {
        this.breakfastItems = breakfastItems;
        notifyDataSetChanged();
    }
}