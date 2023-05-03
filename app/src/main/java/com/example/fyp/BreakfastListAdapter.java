package com.example.fyp;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;

import java.util.List;

public class BreakfastListAdapter extends ArrayAdapter<BreakfastItem> {
    private Context context;
    private List<BreakfastItem> breakfastItems;
    private FirebaseManager firebaseManager;
    private int totalCalories;

    public BreakfastListAdapter(Context context, List<BreakfastItem> breakfastItems, FirebaseManager firebaseManager) {
        super(context, 0, breakfastItems);
        this.context = context;
        this.breakfastItems = breakfastItems;
        this.firebaseManager = firebaseManager;
        // Initialize total calories to 0
        totalCalories = 0;

        // Calculate total calories for all items in the list
        for (BreakfastItem item : breakfastItems) {
            totalCalories += Integer.parseInt(item.getCalories());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.breakfast_recipe_item, parent, false);
        }

        BreakfastItem breakfastItem = getItem(position);

        ImageView breakfastImage = convertView.findViewById(R.id.breakfast_recipe_image);
        TextView breakfastTitle = convertView.findViewById(R.id.breakfast_recipe_title);
        TextView breakfastCalories = convertView.findViewById(R.id.breakfast_recipe_calories);

        breakfastTitle.setText(breakfastItem.getTitle());
        breakfastCalories.setText(breakfastItem.getCalories());
        Glide.with(context).load(breakfastItem.getImageUrl()).into(breakfastImage);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to delete this breakfast recipe?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Delete the item from the Firebase database
                                firebaseManager.deleteBreakfastFromUser(CalendarUtils.selectedDate.toString());
                                WeekViewActivity activity = (WeekViewActivity) context;
                                activity.updateBreakfastItemsList();
                                activity.updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);

                                // Remove the item from the breakfastItems list
                                breakfastItems.remove(breakfastItem);

                                // Update the total calories and refresh the list
                                resetTotalCalories();
                                notifyDataSetChanged();
                                Toast.makeText(context, "Breakfast recipe deleted", Toast.LENGTH_SHORT).show();
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Do nothing
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the selected recipe and calories back to the WeekViewActivity
                WeekViewActivity activity = (WeekViewActivity) context;
                activity.addBreakfastToList(breakfastItem.getTitle(), breakfastItem.getCalories(), breakfastItem.getImageUrl());
                firebaseManager.saveBreakfastForUser(CalendarUtils.selectedDate.toString(), breakfastItem.getTitle(), breakfastItem.getCalories(), breakfastItem.getImageUrl());
                // Update the total calories
                totalCalories += Integer.parseInt(breakfastItem.getCalories());
            }
        });

        return convertView;
    }



    public int getTotalCalories() {
        return totalCalories;
    }

        public void resetTotalCalories() {
        totalCalories = 0;

        // Calculate total calories for all items in the list
        for (int i = 0; i < getCount(); i++) {
            BreakfastItem item = getItem(i);
            totalCalories += Integer.parseInt(item.getCalories());
        }

        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }


}
