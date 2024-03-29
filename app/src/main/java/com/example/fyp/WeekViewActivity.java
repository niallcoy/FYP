package com.example.fyp;

import static com.example.fyp.CalendarUtils.daysToWeekArray;
import static com.example.fyp.CalendarUtils.monthYearFromDate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Locale;
import java.util.UUID;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class WeekViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener, AdapterView.OnItemLongClickListener, PopupMenu.OnMenuItemClickListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    static final int REQUEST_CODE = 1;
    private ListView breakfastListView;
    private BreakfastListAdapter breakfastListAdapter;
    public static ArrayList<BreakfastItem> breakfastItems = new ArrayList<>();
    private ListView lunchListView;
    private LunchListAdapter lunchAdapter;
    public static ArrayList<LunchItem> lunchItems = new ArrayList<>();
    private ListView dinnerListView;
    private DinnerListAdapter dinnerListAdapter;
    public static ArrayList<DinnerItem> dinnerItems = new ArrayList<>();
    private FirebaseManager firebaseManager;
    private HashMap<LocalDate, ArrayList<BreakfastItem>> breakfastsMap;
    private HashMap<LocalDate, ArrayList<LunchItem>> lunchesMap;
    private HashMap<LocalDate, ArrayList<DinnerItem>> dinnersMap;
    public static WeekViewActivity currentInstance = null;
    private Context context;
    LocalDate selectedDate = CalendarUtils.selectedDate;
    private int calorieGoal;



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
        dinnerItems = new ArrayList<>();
        dinnerListAdapter = new DinnerListAdapter(this, dinnerItems, firebaseManager);
        dinnerListView.setAdapter(dinnerListAdapter);
        fetchCalorieGoal();
        CalendarUtils.selectedDate = LocalDate.now();
        breakfastListView.setOnItemLongClickListener(this); // Set the OnItemLongClickListener for breakfasts
        lunchListView.setOnItemLongClickListener(this); // Set the OnItemLongClickListener for lunches
        dinnerListView.setOnItemLongClickListener(this);


        currentInstance = this;
        retrieveMealData(); // Retrieve breakfast data for all dates user has inputted breakfasts into
        updateBreakfastItemsList();
        updateLunchItemsList();
        setWeekView();
        fetchCalorieGoal();


        // Update the total calories after setting the week view
        updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);
    }

    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysToWeekArray(CalendarUtils.selectedDate);
        HashMap<LocalDate, Integer> totalCaloriesMap = CalendarUtils.totalCaloriesMap;

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this, calorieGoal, totalCaloriesMap); // Added the fourth argument

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCalorieGoal();

    }




    private void initWidgets() {
        breakfastsMap = new HashMap<>();
        lunchesMap = new HashMap<>();
        breakfastListView = findViewById(R.id.breakfastList);
        lunchListView = findViewById(R.id.lunchList);
        dinnerListView = findViewById(R.id.dinnerList);
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
    public void NewDinner(View view) {
        Intent intent = new Intent(this, DinnerPopup.class);
        startActivityForResult(intent, REQUEST_CODE);
    }
    private void fetchMealsAndCalculateTotalCalories() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("meals");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                        String date = dateSnapshot.getKey();
                        LocalDate localDate = LocalDate.parse(date);
                        int totalCaloriesForDate = 0;

                        DataSnapshot breakfastSnapshot = dateSnapshot.child("breakfast");
                        if (breakfastSnapshot.exists()) {
                            String calories = breakfastSnapshot.child("calories").getValue(String.class);
                            totalCaloriesForDate += Integer.parseInt(calories);
                        }

                        // Do the same for lunch and dinner...

                        CalendarUtils.totalCaloriesMap.put(localDate, totalCaloriesForDate);
                    }

                    // Refresh your CalendarAdapter here
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void fetchCalorieGoal() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("calorieGoal");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            calorieGoal = Integer.parseInt(snapshot.getValue(String.class));
                            // Initialize the CalendarAdapter here or refresh it.
                            setWeekView();

                            // NEW: Fetch meals and calculate total calories for each date
                            fetchMealsAndCalculateTotalCalories();
                        } catch (NumberFormatException e) {
                            // Handle exception: this means the string could not be parsed into an integer.
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String selectedRecipe = data.getStringExtra("selectedRecipe");
                String selectedCalories = data.getStringExtra("selectedCalories");
                String selectedImageUrl = data.getStringExtra("selectedImageUrl");

                String mealType = data.getStringExtra("mealType");
                if (mealType != null) {
                    switch (mealType) {
                        case "breakfast":
                            addBreakfastToList(selectedRecipe, selectedCalories, selectedImageUrl);
                            break;
                        case "lunch":
                            addLunchToList(selectedRecipe, selectedCalories, selectedImageUrl);
                            break;
                        case "dinner":
                            addDinnerToList(selectedRecipe, selectedCalories, selectedImageUrl);
                            break;
                    }
                }
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
    public void addDinnerToList(String recipe, String calories, String imageUrl) {
        String date = CalendarUtils.selectedDate.toString();
        String combined = recipe + "|" + imageUrl + "|(" + calories + ")";

        // Generate a random UUID for the id
        String id = UUID.randomUUID().toString();

        firebaseManager.saveDinnerForUser(CalendarUtils.selectedDate.toString(), recipe, imageUrl, calories);
        DinnerItem item = new DinnerItem(recipe, imageUrl, calories);

        // Add dinner item to the corresponding list in dinnersMap
        ArrayList<DinnerItem> dinnersList = dinnersMap.get(CalendarUtils.selectedDate);
        if (dinnersList == null) {
            dinnersList = new ArrayList<>();
            dinnersMap.put(CalendarUtils.selectedDate, dinnersList);
        }
        dinnersList.add(item);
        dinnerListAdapter.notifyDataSetChanged();
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

        // Check if there are dinners for the selected date in dinnersMap
        ArrayList<DinnerItem> dinnersList = null;
        if (dinnersMap != null) {
            dinnersList = dinnersMap.get(date);
        }

        if (dinnersList == null) {
            dinnersList = new ArrayList<>();
        }
        dinnerItems.clear();
        dinnerItems.addAll(dinnersList);

        // Check if dinnerListAdapter is not null before calling notifyDataSetChanged()
        if (dinnerListAdapter != null) {
            dinnerListAdapter.notifyDataSetChanged();
        }


        updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);


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
                            Log.e("DEBUG" , "Invalid breakfast data format: " + breakfastSnapshot.getValue());
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
                    // Retrieve dinner data
                    DataSnapshot dinnerSnapshot = dateSnapshot.child("dinner");
                    if (dinnerSnapshot.exists()) {
                        try {
                            String recipe = dinnerSnapshot.child("recipe").getValue(String.class);
                            String imageUrl = dinnerSnapshot.child("imageUrl").getValue(String.class);
                            String calories = dinnerSnapshot.child("calories").getValue(String.class);

                            // Create a DinnerItem object and add it to the corresponding list in dinnersMap
                            DinnerItem item = new DinnerItem(recipe, imageUrl, calories);
                            LocalDate localDate = LocalDate.parse(date);
                            if (dinnersMap == null) {
                                dinnersMap = new HashMap<>();
                            }
                            ArrayList<DinnerItem> dinnersList = dinnersMap.get(localDate);


                            if (dinnersList == null) {
                                dinnersList = new ArrayList<>();
                                dinnersMap.put(localDate, dinnersList);
                            }
                            dinnersList.add(item);
                        } catch (ClassCastException e) {
                            Log.e("DEBUG", "Invalid dinner data format: " + dinnerSnapshot.getValue());
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

    public void updateLunchItemsList() {
        ArrayList<LunchItem> lunchesList = lunchesMap.get(CalendarUtils.selectedDate);
        if (lunchesList == null) {
            lunchesList = new ArrayList<>();
        }
        lunchItems.clear();
        lunchItems.addAll(lunchesList);
        lunchAdapter.notifyDataSetChanged();
    }

    public void updateDinnerItemsList() {
        ArrayList<DinnerItem> dinnersList = dinnersMap.get(CalendarUtils.selectedDate);
        if (dinnersList == null) {
            dinnersList = new ArrayList<>();
        }
        dinnerItems.clear();
        dinnerItems.addAll(dinnersList);
        dinnerListAdapter.notifyDataSetChanged();
    }


    public void logout() {
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
                    .child("meals").child(date.toString()).child("breakfast").child(item.getTitle());

            breakfastRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Breakfast item deleted", Toast.LENGTH_SHORT).show();
                    firebaseManager.deleteBreakfastFromUser(date.toString());// Remove the breakfast item from the user's meals list in Firebase
                    updateBreakfastItemsList();
                    updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);

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
                    firebaseManager.deleteLunchFromUser(date.toString());// Remove the lunch item from the user's meals list in Firebase
                    updateLunchItemsList();
                    updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);

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
        }else if (parent.getId() == R.id.dinnerList) {
            // Get the clicked dinner item
            DinnerItem item = dinnerItems.get(position);

            // Get the date of the clicked item
            LocalDate date = CalendarUtils.selectedDate;

            // Get the Firebase reference to the corresponding dinner item
            DatabaseReference dinnerRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    .child("meals").child(date.toString()).child("dinner").child(item.getRecipe());

            dinnerRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(context, "Dinner item deleted", Toast.LENGTH_SHORT).show();
                    firebaseManager.deleteDinnerFromUser(date.toString());// Remove the dinner item from the user's meals list in Firebase
                    updateDinnerItemsList();
                    updateTotalCaloriesForSelectedDay(CalendarUtils.selectedDate);

                    Log.d("FIREBASE", "Dinner item deleted successfully");

                    // Remove the dinner item from the corresponding list in dinnersMap
                    ArrayList<DinnerItem> dinnersList = dinnersMap.get(date);
                    if (dinnersList != null) {
                        if (dinnersList.remove(item)) { // Check if the item was removed from the list
                            if (dinnersList.isEmpty()) {
                                dinnersMap.remove(date);
                            }
                            dinnerItems.remove(position); // Remove the clicked dinner item from the list and notify the adapter
                            dinnerListAdapter.notifyDataSetChanged();
                        }

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Failed to delete dinner item", Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE", "Error deleting dinner item", e);
                }
            });
        }

        return true;
    }


    public void updateTotalCaloriesForSelectedDay(LocalDate selectedDate) {
        String userId = firebaseManager.getCurrentUser().getUid();
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("meals").child(selectedDate.toString());

        ValueEventListener mealsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalCalories = 0;

                // Reset total calories to 0 before calculating it again
                breakfastListAdapter.resetTotalCalories();
                lunchAdapter.resetTotalCalories();
                dinnerListAdapter.resetTotalCalories();

                // Get total calories from breakfast, lunch, and dinner adapters
                totalCalories += breakfastListAdapter.getTotalCalories();
                totalCalories += lunchAdapter.getTotalCalories();
                totalCalories += dinnerListAdapter.getTotalCalories();

                // Update the TextView with the total calories
                TextView totalCalsTextView = findViewById(R.id.total_cals_text_view);
                totalCalsTextView.setText(String.format(Locale.getDefault(), "Total Calories: %d", totalCalories));

                // Update the totalCaloriesMap
                CalendarUtils.totalCaloriesMap.put(selectedDate, totalCalories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("FIREBASE", "Error fetching meals data.", databaseError.toException());
            }
        };

        mealsRef.addListenerForSingleValueEvent(mealsListener);
    }

    public void popUp(View v){
        PopupMenu popUp = new PopupMenu(this, v);
        popUp.setOnMenuItemClickListener(this);
        popUp.inflate(R.menu.popup_menu);
        popUp.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.logout:
                logout();
                return true;
            case R.id.searchRecipe:
                Intent intent = new Intent(this, SearchRecipe.class);
                startActivity(intent);
                return true;
            case R.id.weekView:
                Intent intent1 = new Intent(this, WeekViewActivity.class);
                startActivity(intent1);
                return true;
            case R.id.profile:
                Intent intent2 = new Intent(this, Profile.class);
                startActivity(intent2);
                return true;
            case R.id.favourites:
                Intent intent3 = new Intent(this, ViewFavourites.class);
                startActivity(intent3);
                return true;
            default:
                return false;
        }
    }



}
