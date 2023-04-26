package com.example.fyp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;


import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder> {

    private final ArrayList<LocalDate> days;
    private final OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<LocalDate> days, OnItemListener onItemListener) {
        this.days = days;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if(days.size() > 15) // month view
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
            if (CalendarUtils.breakfastMap.containsKey(date)) {
                ArrayList<BreakfastItem> breakfasts = CalendarUtils.breakfastMap.get(date);
                String breakfastString = "";
                for (BreakfastItem breakfast : breakfasts) {
                    breakfastString += breakfast.getTitle() + " (" + breakfast.getCalories() + ")\n";
                }
                holder.breakfastText.setText(breakfastString);
            }
            if (date.equals(CalendarUtils.selectedDate)) {
                holder.parentView.setBackgroundResource(R.drawable.selected_date_background);
            } else {
                holder.parentView.setBackgroundResource(0);
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
}
