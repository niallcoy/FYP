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

public class DinnerListAdapter extends ArrayAdapter<DinnerItem> {
    private Context context;
    private List<DinnerItem> dinnerItems;
    private FirebaseManager firebaseManager;
    private int totalCalories;

    public DinnerListAdapter(Context context, List<DinnerItem> dinnerItems, FirebaseManager firebaseManager) {
        super(context, 0, dinnerItems);
        this.context = context;
        this.dinnerItems = dinnerItems;
        this.firebaseManager = firebaseManager;
        // Initialize total calories to 0
        totalCalories = 0;

        // Calculate total calories for all items in the list
        for (DinnerItem item : dinnerItems) {
            totalCalories += Integer.parseInt(item.getCalories());
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.dinner_recipe_item, parent, false);
        }

        DinnerItem dinnerItem = getItem(position);

        ImageView dinnerImage = convertView.findViewById(R.id.dinner_recipe_image);
        TextView dinnerTitle = convertView.findViewById(R.id.dinner_recipe_title);
        TextView dinnerCalories = convertView.findViewById(R.id.dinner_recipe_calories);

        dinnerTitle.setText(dinnerItem.getTitle());
        dinnerCalories.setText(dinnerItem.getCalories());
        Glide.with(context).load(dinnerItem.getImageUrl()).into(dinnerImage);

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to delete this dinner recipe?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Delete the item from the Firebase database
                                firebaseManager.deleteDinnerFromUser(CalendarUtils.selectedDate.toString());
                                WeekViewActivity activity = (WeekViewActivity) context;
                                activity.updateDinnerItemsList();
                                activity.updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);

                                // Remove the item from the dinnerItems list
                                dinnerItems.remove(dinnerItem);

                                // Update the total calories and refresh the list
                                resetTotalCalories();
                                notifyDataSetChanged();
                                Toast.makeText(context, "Dinner recipe deleted", Toast.LENGTH_SHORT).show();
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
                activity.addDinnerToList(dinnerItem.getTitle(), dinnerItem.getCalories(), dinnerItem.getImageUrl());
                firebaseManager.saveDinnerForUser(CalendarUtils.selectedDate.toString(), dinnerItem.getTitle(), dinnerItem.getCalories(), dinnerItem.getImageUrl());
                // Update the total calories by adding the calories of the new item
                totalCalories += Integer.parseInt(dinnerItem.getCalories());
                activity.updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate); // Update the total calories displayed in the UI
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
            DinnerItem item = getItem(i);
            totalCalories += Integer.parseInt(item.getCalories());
        }

        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }


}