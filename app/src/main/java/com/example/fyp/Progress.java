package com.example.fyp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Progress extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    private TextView totalCaloriesTextView;
    private TextView weekTextView;
    private TextView weeklyCalorieGoalTextView;
    private AnyChartView anyChartView;
    private LocalDate startDate;
    private LocalDate endDate;
    private DatabaseReference mealsRef;
    private String userId;
    private int weeklyCalorieGoal;
    private int lastUpdatedCalories = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        totalCaloriesTextView = findViewById(R.id.total_cals_text_view);
        weekTextView = findViewById(R.id.weekTextView);
        weeklyCalorieGoalTextView = findViewById(R.id.weekly_calorie_goal_text_view);
        anyChartView = findViewById(R.id.any_chart_view);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mealsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("meals");

        startDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1); // Start of the week (Monday)
        endDate = startDate.plusDays(6); // End of the week (Sunday)

        fetchAndCalculateWeeklyCalorieGoal();
        updateWeekView();
        updateTotalCaloriesForWeek();
    }

    private void fetchAndCalculateWeeklyCalorieGoal() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String calorieGoal = snapshot.child("calorieGoal").getValue(String.class);
                if (calorieGoal != null) {
                    int dailyCalorieGoal = Integer.parseInt(calorieGoal);
                    weeklyCalorieGoal = dailyCalorieGoal * 7;
                    updateWeeklyCalorieGoalUI();
                    updateTotalCaloriesForWeek();  // Move this call here
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that might occur during the database read
            }
        });
    }


    private void updateWeeklyCalorieGoalUI() {
        weeklyCalorieGoalTextView.setText("Weekly Calorie Goal: " + weeklyCalorieGoal);
    }


    private void updatePieChart(int totalCalories) {

        if (lastUpdatedCalories == totalCalories) {
            return;
        }
        lastUpdatedCalories = totalCalories; // Update the last known calorie count
        Log.d("Debug", "Update Pie Chart called with totalCalories: " + totalCalories);
        // Initialize the pie chart
        Pie pie = AnyChart.pie();
        // Prepare the data
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Total Calories", totalCalories));
        data.add(new ValueDataEntry("Remaining", weeklyCalorieGoal - totalCalories));
        // Set the data to the pie chart
        pie.data(data);
        // Set the pie chart to the AnyChartView
        anyChartView.setChart(pie);
        // Invalidate the view to force a redraw
        anyChartView.invalidate();
        Log.d("ChartDebug", "updatePieChart: Total Calories: " + totalCalories);
    }




    public void NextWeekAction(View view) {
        startDate = startDate.plusWeeks(1);
        endDate = endDate.plusWeeks(1);
        updateWeekView();
        fetchAndCalculateWeeklyCalorieGoal();
    }

    public void PreviousWeekAction(View view) {
        startDate = startDate.minusWeeks(1);
        endDate = endDate.minusWeeks(1);
        updateWeekView();
        fetchAndCalculateWeeklyCalorieGoal();
    }




    private void updateWeekView() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        weekTextView.setText(String.format("%s to %s", startDate.format(formatter), endDate.format(formatter)));
        updatePieChart(0);
    }

    private void updateTotalCaloriesForWeek() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = startDate.format(formatter);
        String end = endDate.format(formatter);

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int weekTotalCalories = 0;
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    if (isDateWithinWeek(date, start, end)) {
                        // Extract and sum up the calories
                        String breakfastCaloriesStr = dateSnapshot.child("breakfast").child("calories").getValue(String.class);
                        String lunchCaloriesStr = dateSnapshot.child("lunch").child("calories").getValue(String.class);
                        String dinnerCaloriesStr = dateSnapshot.child("dinner").child("calories").getValue(String.class);

                        int breakfastCalories = (breakfastCaloriesStr != null) ? Integer.parseInt(breakfastCaloriesStr) : 0;
                        int lunchCalories = (lunchCaloriesStr != null) ? Integer.parseInt(lunchCaloriesStr) : 0;
                        int dinnerCalories = (dinnerCaloriesStr != null) ? Integer.parseInt(dinnerCaloriesStr) : 0;


                        weekTotalCalories += (breakfastCalories + lunchCalories + dinnerCalories);
                    }
                }

                totalCaloriesTextView.setText("Total Calories for the Week: " + weekTotalCalories);
                updatePieChart(weekTotalCalories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors
            }
        });
    }

    private boolean isDateWithinWeek(String date, String startDate, String endDate) {
        LocalDate dateLD = LocalDate.parse(date);
        LocalDate startLD = LocalDate.parse(startDate);
        LocalDate endLD = LocalDate.parse(endDate);
        return (dateLD.isAfter(startLD) || dateLD.isEqual(startLD)) && (dateLD.isBefore(endLD) || dateLD.isEqual(endLD));
    }

    public void popUp(View v) {
        AppUtils.showPopUp(this, v, this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        AppUtils.handleMenuItemClick(item, this);
        return true;
    }
}
