package com.example.fyp;

import static com.example.fyp.CalendarUtils.daysToWeekArray;
import static com.example.fyp.CalendarUtils.monthYearFromDate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.util.UUID;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class WeekViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener, AdapterView.OnItemLongClickListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    static final int REQUEST_CODE = 1;
    private ListView breakfastListView;
    private BreakfastListAdapter breakfastListAdapter;
    public static ArrayList<BreakfastItem> breakfastItems = new ArrayList<>();
    private ListView lunchListView;
    private LunchListAdapter lunchAdapter;
    public static ArrayList<LunchItem> lunchItems = new ArrayList<>();
    public static WeekViewActivity currentInstance = null;
    private FirebaseManager firebaseManager;
    private HashMap<LocalDate, ArrayList<BreakfastItem>> breakfastsMap;
    private HashMap<LocalDate, ArrayList<LunchItem>> lunchesMap;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        firebaseManager = new FirebaseManager();
        setContentView(R.layout.activity_week_view);
        initWidgets();
        breakfastItems = new ArrayList<>(); // Initialize the breakfastItems
        breakfastListAdapter = new BreakfastListAdapter(this, breakfastItems, firebaseManager);
        breakfastListView.setAdapter(breakfastListAdapter);
        lunchItems = new ArrayList<>(); // Initialize the lunchItems
        lunchAdapter = new LunchListAdapter(this, lunchItems, firebaseManager);
        lunchListView.setAdapter(lunchAdapter);
        CalendarUtils.selectedDate = LocalDate.now();
        breakfastListView.setOnItemLongClickListener(this); // Set the OnItemLongClickListener for breakfasts
        lunchListView.setOnItemLongClickListener(this); // Set the OnItemLongClickListener for lunches

        currentInstance = this;
        retrieveMealData(); // Retrieve breakfast data for all dates user has inputted breakfasts into
        updateBreakfastItemsList();
        updateLunchItemsList();
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
        lunchesMap = new HashMap<>();
        breakfastListView = findViewById(R.id.breakfastList);
        lunchListView = findViewById(R.id.lunchList);
        monthYearText = findViewById(R.id.MonthYearTV);
        calendarRecyclerView = findViewById(R.id.CalendarRecyclerView);
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

    public void NewLunch(View view) {
        Intent intent = new Intent(this, LunchPopup.class);
        startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        // Generate a random UUID for the id
        String id = UUID.randomUUID().toString();

        firebaseManager.saveBreakfastForUser(date, recipe, imageUrl, calories); // Save the calorie value to Firebase database
        BreakfastItem item = new BreakfastItem(recipe, imageUrl, calories);

        // Add breakfast item to the corresponding list in breakfastsMap
        ArrayList<BreakfastItem> breakfastsList = breakfastsMap.get(CalendarUtils.selectedDate);
        if (breakfastsList == null) {
            breakfastsList = new ArrayList<>();
            breakfastsMap.put(CalendarUtils.selectedDate, breakfastsList);
        }
        breakfastsList.add(item);
        breakfastListAdapter.notifyDataSetChanged();
    }

    public void addLunchToList(String recipe, String calories, String imageUrl) {
        String date = CalendarUtils.selectedDate.toString();
        String combined = recipe + "|" + imageUrl + "|(" + calories + ")";

        // Generate a random UUID for the id
        String id = UUID.randomUUID().toString();

        firebaseManager.saveLunchForUser(date, recipe, imageUrl, calories); // Save the calorie value to Firebase database
        LunchItem item = new LunchItem(recipe, imageUrl, calories);

        // Add lunch item to the corresponding list in lunchesMap
        ArrayList<LunchItem> lunchesList = lunchesMap.get(CalendarUtils.selectedDate);
        if (lunchesList == null) {
            lunchesList = new ArrayList<>();
            lunchesMap.put(CalendarUtils.selectedDate, lunchesList);
        }
        lunchesList.add(item);
        lunchAdapter.notifyDataSetChanged();
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
        breakfastItems.addAll(breakfastsList);

        // Check if breakfastListAdapter is not null before calling notifyDataSetChanged()
        if (breakfastListAdapter != null) {
            breakfastListAdapter.notifyDataSetChanged();
        }

        // Check if there are lunches for the selected date in lunchesMap
        ArrayList<LunchItem> lunchesList = lunchesMap.get(date);
        if (lunchesList == null) {
            lunchesList = new ArrayList<>();
        }
        lunchItems.clear();
        lunchItems.addAll(lunchesList);
        lunchAdapter.notifyDataSetChanged();

        setWeekView();
    }


    private void retrieveMealData() {
        String userId = firebaseManager.getCurrentUser().getUid();
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("meals");

        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FIREBASE", "Retrieving meal data");
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();

                    // Retrieve breakfast data
                    DataSnapshot breakfastSnapshot = dateSnapshot.child("breakfast");
                    if (breakfastSnapshot.exists()) {
                        try {
                            String recipe = breakfastSnapshot.child("recipe").getValue(String.class);
                            String imageUrl = breakfastSnapshot.child("imageUrl").getValue(String.class);
                            String calories = breakfastSnapshot.child("calories").getValue(String.class);

                            // Create a BreakfastItem object and add it to the corresponding list in breakfastsMap
                            BreakfastItem item = new BreakfastItem(recipe, imageUrl, calories);
                            LocalDate localDate = LocalDate.parse(date);
                            ArrayList<BreakfastItem> breakfastsList = breakfastsMap.get(localDate);
                            if (breakfastsList == null) {
                                breakfastsList = new ArrayList<>();
                                breakfastsMap.put(localDate, breakfastsList);
                            }
                            breakfastsList.add(item);
                        } catch (ClassCastException e) {
                            Log.e("DEBUG", "Invalid breakfast data format: " + breakfastSnapshot.getValue());
                        }
                    }

                    // Retrieve lunch data
                    DataSnapshot lunchSnapshot = dateSnapshot.child("lunch");
                    if (lunchSnapshot.exists()) {
                        try {
                            String recipe = lunchSnapshot.child("recipe").getValue(String.class);
                            String imageUrl = lunchSnapshot.child("imageUrl").getValue(String.class);
                            String calories = lunchSnapshot.child("calories").getValue(String.class);

                            // Create a LunchItem object and add it to the corresponding list in lunchesMap
                            LunchItem item = new LunchItem(recipe, imageUrl, calories);
                            LocalDate localDate = LocalDate.parse(date);
                            ArrayList<LunchItem> lunchesList = lunchesMap.get(localDate);
                            if (lunchesList == null) {
                                lunchesList = new ArrayList<>();
                                lunchesMap.put(localDate, lunchesList);
                            }
                            lunchesList.add(item);
                        } catch (ClassCastException e) {
                            Log.e("DEBUG", "Invalid lunch data format: " + lunchSnapshot.getValue());
                        }
                    }
                }
                updateBreakfastItemsList(); // Update the breakfastItems list
                updateLunchItemsList(); // Update the lunchItems list
                setWeekView(); // Update the calendar view with the retrieved data
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }


    public void updateBreakfastItemsList() {
        if (breakfastsMap != null) {
            ArrayList<BreakfastItem> breakfasts = breakfastsMap.get(CalendarUtils.selectedDate);
            if (breakfasts != null) {
                breakfastItems.clear();
                breakfastItems.addAll(breakfasts);
                breakfastListAdapter.notifyDataSetChanged(); // Notify the adapter of the data change
            } else {
                breakfastItems.clear();
                breakfastListAdapter.notifyDataSetChanged(); // Notify the adapter of the data change
            }
        } else {
            breakfastItems.clear();
            breakfastListAdapter.notifyDataSetChanged(); // Notify the adapter of the data change
        }
    }

    private void updateLunchItemsList() {
        ArrayList<LunchItem> lunchesList = lunchesMap.get(CalendarUtils.selectedDate);
        if (lunchesList == null) {
            lunchesList = new ArrayList<>();
        }
        lunchItems.clear();
        lunchItems.addAll(lunchesList);
        lunchAdapter.notifyDataSetChanged();
    }


    public void logout(View view) {
        FirebaseAuth.getInstance().signOut(); // Sign out the user
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the activity stack
        startActivity(intent);
        finish(); // Finish the current activity
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String userId = firebaseManager.getCurrentUser().getUid();

        // Determine whether a breakfast or lunch item was clicked based on the parent ListView
        if (parent.getId() == R.id.breakfastList) {
            // Get the clicked breakfast item
            BreakfastItem item = breakfastItems.get(position);

            // Get the date of the clicked item
            LocalDate date = CalendarUtils.selectedDate;

            // Get the Firebase reference to the corresponding breakfast item
            DatabaseReference breakfastRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    .child("meals").child(date.toString()).child("breakfast").child(item.getRecipe());

            breakfastRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Breakfast item deleted", Toast.LENGTH_SHORT).show();
                    firebaseManager.deleteBreakfastFromUser(date.toString(), item.getRecipe()); // Remove the breakfast item from the user's meals list in Firebase
                    Log.d("FIREBASE", "Breakfast item deleted successfully");

                    // Remove the breakfast item from the corresponding list in breakfastsMap
                    ArrayList<BreakfastItem> breakfastsList = breakfastsMap.get(date);
                    if (breakfastsList != null) {
                        if (breakfastsList.remove(item)) { // Check if the item was removed from the list
                            if (breakfastsList.isEmpty()) {
                                breakfastsMap.remove(date);
                            }
                            breakfastItems.remove(position); // Remove the clicked breakfast item from the list and notify the adapter
                            breakfastListAdapter.notifyDataSetChanged();
                        } else {
                            // The item was not found in the list, so do nothing
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Failed to delete breakfast item", Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE", "Error deleting breakfast item", e);
                }
            });

        } else if (parent.getId() == R.id.lunchList) {
            // Get the clicked lunch item
            LunchItem item = lunchItems.get(position);

            // Get the date of the clicked item
            LocalDate date = CalendarUtils.selectedDate;

            // Get the Firebase reference to the corresponding lunch item
            DatabaseReference lunchRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    .child("meals").child(date.toString()).child("lunch").child(item.getRecipe());

            lunchRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Lunch item deleted", Toast.LENGTH_SHORT).show();
                    firebaseManager.deleteLunchFromUser(date.toString(), item.getRecipe()); // Remove the lunch item from the user's meals list in Firebase
                    Log.d("FIREBASE", "Lunch item deleted successfully");

                    // Remove the lunch item from the corresponding list in lunchesMap
                    ArrayList<LunchItem> lunchesList = lunchesMap.get(date);
                    if (lunchesList != null) {
                        if (lunchesList.remove(item)) { // Check if the item was removed from the list
                            if (lunchesList.isEmpty()) {
                                lunchesMap.remove(date);
                            }
                            lunchItems.remove(position); // Remove the clicked lunch item from the list and notify the adapter
                            lunchAdapter.notifyDataSetChanged();
                        }

                    }

                }
            });
        }
        return true;
    }
}

