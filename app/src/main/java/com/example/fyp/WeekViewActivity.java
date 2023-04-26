package com.example.fyp;

import static com.example.fyp.CalendarUtils.daysToWeekArray;
import static com.example.fyp.CalendarUtils.monthYearFromDate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class WeekViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    static final int REQUEST_CODE = 1;
    private ListView listView;
    private BreakfastListAdapter breakfastAdapter;
    private ArrayList<BreakfastItem> breakfastItems;
    public static WeekViewActivity currentInstance = null;
    private FirebaseManager firebaseManager;
    private HashMap<LocalDate, ArrayList<BreakfastItem>> breakfastsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseManager = new FirebaseManager();
        setContentView(R.layout.activity_week_view);
        initWidgets();
        listView = findViewById(R.id.breakfastList);
        breakfastItems = new ArrayList<>(); // Initialize the breakfastItems
        breakfastAdapter = new BreakfastListAdapter(this, breakfastItems);
        listView.setAdapter(breakfastAdapter);
        CalendarUtils.selectedDate = LocalDate.now();
        currentInstance = this;
        retrieveBreakfastData(); // retrieve breakfast data for all dates user has inputted breakfasts into
        updateBreakfastItemsList();
        setWeekView();
    }


    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysToWeekArray(CalendarUtils.selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void initWidgets() {
        breakfastsMap = new HashMap<>();
        calendarRecyclerView = findViewById(R.id.CalendarRecyclerView);
        monthYearText = findViewById(R.id.MonthYearTV);
    }

    public void NextWeekAction(View view) {

        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    public void PreviousWeekAction(View view) {

        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    public void NewBreakfast(View view) {
        Intent intent = new Intent(this, BreakfastPopup.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String selectedRecipe = data.getStringExtra("selectedRecipe");
                String selectedCalories = data.getStringExtra("selectedCalories");
                String selectedImageUrl = data.getStringExtra("selectedImageUrl");
                addBreakfastToList(selectedRecipe, selectedCalories, selectedImageUrl);
            }
        }
    }




    public void addBreakfastToList(String recipe, String calories, String imageUrl) {
        String date = CalendarUtils.selectedDate.toString();
        String combined = recipe + "|" + imageUrl + "|(" + calories + ")";
        firebaseManager.saveBreakfastForUser(date, combined);
        BreakfastItem item = new BreakfastItem(recipe, imageUrl, calories);

        // Add breakfast item to the corresponding list in breakfastsMap
        ArrayList<BreakfastItem> breakfastsList = breakfastsMap.get(CalendarUtils.selectedDate);
        if (breakfastsList == null) {
            breakfastsList = new ArrayList<>();
            breakfastsMap.put(CalendarUtils.selectedDate, breakfastsList);
        }
        breakfastsList.add(item);
        breakfastAdapter.notifyDataSetChanged();
    }


    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        Log.d("DEBUG", "onItemClick called");
        // Check if there are breakfasts for the selected date in breakfastsMap
        ArrayList<BreakfastItem> breakfastsList = breakfastsMap.get(date);
        if (breakfastsList == null) {
            breakfastsList = new ArrayList<>();
        }
        breakfastItems.clear();
        breakfastItems.addAll(breakfastsList); // Add this line to update the breakfastItems list
        breakfastAdapter.notifyDataSetChanged();
        setWeekView();
    }



    private void updateMealList() {
        String date = CalendarUtils.selectedDate.toString();
        ValueEventListener mealsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<BreakfastItem> breakfastsList = new ArrayList<>();
                for (DataSnapshot mealSnapshot : dataSnapshot.getChildren()) {
                    String meal = mealSnapshot.getValue(String.class);
                    // Recipe and calories stored together in firebase
                    String[] parts = meal.split(" \\("); // Split recipe and calories
                    String recipe = parts[0];
                    String calories = parts[1].substring(0, parts[1].length() - 1); // Remove the closing parenthesis

                    BreakfastItem item = new BreakfastItem(recipe, "https://your-image-url.com", calories);
                    breakfastsList.add(item);
                }
                // Add breakfastsList to the corresponding date in breakfastsMap
                breakfastsMap.put(CalendarUtils.selectedDate, breakfastsList);
                breakfastAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        firebaseManager.getMealsForDate(date, mealsListener);
    }

    private void retrieveBreakfastData() {
        String userId = firebaseManager.getCurrentUser().getUid();
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("meals");

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIREBASE", "Retrieving breakfast data");
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    DataSnapshot breakfastSnapshot = dateSnapshot.child("breakfast");
                    if (breakfastSnapshot.exists()) {
                        String meal = breakfastSnapshot.getValue(String.class);
                        Log.d("FIREBASE", "Retrieved breakfast data for date " + date + ": " + meal);

                        // Recipe, imageUrl, and calories stored together in firebase
                        String[] parts = meal.split("\\|\\("); // Use the delimiter to split the string
                        if (parts.length < 2) {
                            Log.e("DEBUG", "Invalid meal data format: " + meal);
                            continue;
                        }
                        String[] recipeAndImageUrl = parts[0].split("\\|"); // Split the recipe and imageUrl
                        if (recipeAndImageUrl.length < 2) {
                            Log.e("DEBUG", "Invalid meal data format: " + meal);
                            continue;
                        }
                        String recipe = recipeAndImageUrl[0];
                        String imageUrl = recipeAndImageUrl[1]; // Retrieve imageUrl
                        String calories = parts[1].substring(0, parts[1].length() - 1); // Remove the closing parenthesis

                        BreakfastItem item = new BreakfastItem(recipe, imageUrl, calories);
                        LocalDate localDate = LocalDate.parse(date);
                        ArrayList<BreakfastItem> breakfastsList = breakfastsMap.get(localDate);
                        if (breakfastsList == null) {
                            breakfastsList = new ArrayList<>();
                            breakfastsMap.put(localDate, breakfastsList);
                        }
                        breakfastsList.add(item);
                        breakfastAdapter.notifyDataSetChanged();
                    }
                }
                updateBreakfastItemsList(); // Add this line to update the breakfastItems list
                setWeekView(); // update the calendar view with the retrieved data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // handle error
            }
        });
    }



    private void updateBreakfastItemsList() {
        ArrayList<BreakfastItem> breakfastsList = breakfastsMap.get(CalendarUtils.selectedDate);
        if (breakfastsList == null) {
            breakfastsList = new ArrayList<>();
        }
        breakfastItems.clear();
        breakfastItems.addAll(breakfastsList);
        breakfastAdapter.notifyDataSetChanged();
    }



}