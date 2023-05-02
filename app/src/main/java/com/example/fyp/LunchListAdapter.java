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

    public LunchListAdapter(Context context, List<LunchItem> lunchItems, FirebaseManager firebaseManager) {
        super(context, 0, lunchItems);
        this.context = context;
        this.lunchItems = lunchItems;
        this.firebaseManager = firebaseManager;
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
                                // Delete the item from the list and Firebase database
                                lunchItems.remove(lunchItem);
                                firebaseManager.deleteLunchFromUser(CalendarUtils.selectedDate.toString(), lunchItem.getTitle());
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
            }
        });


        return convertView;
    }

    public void setLunchItems(List<LunchItem> lunchItems) {
        this.lunchItems = lunchItems;
        notifyDataSetChanged();
    }
}
