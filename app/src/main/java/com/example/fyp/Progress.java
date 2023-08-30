package com.example.fyp;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fyp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Progress extends AppCompatActivity  implements PopupMenu.OnMenuItemClickListener {

    private TextView totalCaloriesTextView;
    private TextView weekTextView;
    private LocalDate startDate;
    private LocalDate endDate;
    private DatabaseReference mealsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        totalCaloriesTextView = findViewById(R.id.total_cals_text_view);
        weekTextView = findViewById(R.id.weekTextView);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mealsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("meals");

        startDate = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1); // Start of the week (Monday)
        endDate = startDate.plusDays(6); // End of the week (Sunday)

        updateWeekView();
        updateTotalCaloriesForWeek();
    }

    public void NextWeekAction(View view) {
        startDate = startDate.plusWeeks(1);
        endDate = endDate.plusWeeks(1);
        updateWeekView();
        updateTotalCaloriesForWeek();
    }

    public void PreviousWeekAction(View view) {
        startDate = startDate.minusWeeks(1);
        endDate = endDate.minusWeeks(1);
        updateWeekView();
        updateTotalCaloriesForWeek();
    }

    private void updateWeekView() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        weekTextView.setText(String.format("%s to %s", startDate.format(formatter), endDate.format(formatter)));
    }

    private void updateTotalCaloriesForWeek() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String start = startDate.format(formatter);
        String end = endDate.format(formatter);

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String weekTotalCalories = "0";
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    if (isDateWithinWeek(date, start, end)) {
                        // Extract and sum up the calories as strings
                        String breakfastCalories = dateSnapshot.child("breakfast").child("calories").getValue(String.class);
                        String lunchCalories = dateSnapshot.child("lunch").child("calories").getValue(String.class);
                        String dinnerCalories = dateSnapshot.child("dinner").child("calories").getValue(String.class);

                        // Perform null checks
                        breakfastCalories = (breakfastCalories != null) ? breakfastCalories : "0";
                        lunchCalories = (lunchCalories != null) ? lunchCalories : "0";
                        dinnerCalories = (dinnerCalories != null) ? dinnerCalories : "0";

                        // Add them (you might want to convert them into integers to perform the addition)
                        int totalForDay = Integer.parseInt(breakfastCalories) + Integer.parseInt(lunchCalories) + Integer.parseInt(dinnerCalories);
                        int totalForWeek = Integer.parseInt(weekTotalCalories) + totalForDay;
                        weekTotalCalories = String.valueOf(totalForWeek);
                    }
                }

                // Update the UI
                totalCaloriesTextView.setText("Total Calories for the Week: " + weekTotalCalories);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean isDateWithinWeek(String date, String startDate, String endDate) {
        //  date comparison logic here
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
