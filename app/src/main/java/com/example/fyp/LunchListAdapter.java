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

public class LunchListAdapter extends ArrayAdapter<LunchItem> {
    private Context context;
    private List<LunchItem> lunchItems;
    private FirebaseManager firebaseManager;
    private int totalCalories;

    public LunchListAdapter(Context context, List<LunchItem> lunchItems, FirebaseManager firebaseManager) {
        super(context, 0, lunchItems);
        this.context = context;
        this.lunchItems = lunchItems;
        this.firebaseManager = firebaseManager;
        // Initialize total calories to 0
        totalCalories = 0;

        // Calculate total calories for all items in the list
        for (LunchItem item : lunchItems) {
            totalCalories += Integer.parseInt(item.getCalories());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lunch_recipe_item, parent, false);
        }

        LunchItem lunchItem = getItem(position);

        ImageView lunchImage = convertView.findViewById(R.id.lunch_recipe_image);
        TextView lunchTitle = convertView.findViewById(R.id.lunch_recipe_title);
        TextView lunchCalories = convertView.findViewById(R.id.lunch_recipe_calories);

        lunchTitle.setText(lunchItem.getTitle());
        lunchCalories.setText(lunchItem.getCalories());
        Glide.with(context).load(lunchItem.getImageUrl()).into(lunchImage);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to delete this lunch recipe?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Delete the item from the Firebase database
                                firebaseManager.deleteLunchFromUser(CalendarUtils.selectedDate.toString());
                                WeekViewActivity activity = (WeekViewActivity) context;
                                activity.updateLunchItemsList();
                                activity.updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);

                                // Remove the item from the lunchItems list
                                lunchItems.remove(lunchItem);

                                // Update the total calories and refresh the list
                                resetTotalCalories();
                                notifyDataSetChanged();
                                Toast.makeText(context, "Lunch recipe deleted", Toast.LENGTH_SHORT).show();
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
                activity.addLunchToList(lunchItem.getTitle(), lunchItem.getCalories(), lunchItem.getImageUrl());
                firebaseManager.saveLunchForUser(CalendarUtils.selectedDate.toString(), lunchItem.getTitle(), lunchItem.getCalories(), lunchItem.getImageUrl());
                // Update the total calories
                totalCalories += Integer.parseInt(lunchItem.getCalories());
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
            LunchItem item = getItem(i);
            totalCalories += Integer.parseInt(item.getCalories());
        }

        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }

}
