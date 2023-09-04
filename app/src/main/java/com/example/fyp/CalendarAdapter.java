package com.example.fyp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;
    private int calorieGoal;
    private final HashMap<LocalDate, Integer> totalCaloriesMap;

    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener, int calorieGoal, HashMap<LocalDate, Integer> totalCaloriesMap) {
        this.days = days;
        this.onItemListener = onItemListener;
        this.calorieGoal = calorieGoal;
        this.totalCaloriesMap = totalCaloriesMap;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(days.size() > 15)
            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        else
            layoutParams.height = (int) parent.getHeight();
        return new CalendarViewHolder(view, onItemListener, days);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LocalDate date = days.get(position);
        if (date != null) {
            holder.dayOfMonth.setText(Integer.toString(date.getDayOfMonth()));

            if (calorieGoal != -1) {
                int totalCaloriesForDate = getTotalCaloriesForDate(date);

                if (totalCaloriesForDate > 0) {
                    int calorieDifference = Math.abs(totalCaloriesForDate - calorieGoal);

                    if (calorieDifference <= 100) {
                        holder.parentView.setBackgroundColor(Color.GREEN);
                    } else if (calorieDifference >= 101 && calorieDifference <= 300) {
                        holder.parentView.setBackgroundColor(Color.YELLOW);
                    } else {
                        holder.parentView.setBackgroundColor(Color.RED);
                    }
                }
            }
        }
    }


    @Override
    public int getItemCount() {
        return days.size();
    }

    public interface OnItemListener {
        void onItemClick(int position, LocalDate date);
    }

    private int getTotalCaloriesForDate(LocalDate date) {
        // Fetch the total calories for the date from the map
        if (totalCaloriesMap.containsKey(date)) {
            return totalCaloriesMap.get(date);
        }
        return 0;
    }
}
