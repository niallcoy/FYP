package com.example.fyp;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class FirebaseManager {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    public FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void saveBreakfastForUser(String date, String recipe, String imageUrl, String calories) {
        String userId = getCurrentUser().getUid();
        DatabaseReference mealsRef = mDatabase.child("Users").child(userId).child("meals");

        HashMap<String, String> mealData = new HashMap<>();
        mealData.put("recipe", recipe);
        mealData.put("imageUrl", imageUrl);
        mealData.put("calories", calories);

        mealsRef.child(date).child("breakfast").setValue(mealData);
    }

    public void deleteBreakfastFromUser(String date) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference breakfastRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                .child("meals").child(date).child("breakfast");
        breakfastRef.removeValue();
    }



    public void saveLunchForUser(String date, String recipe, String imageUrl, String calories) {
        String userId = getCurrentUser().getUid();
        DatabaseReference mealsRef = mDatabase.child("Users").child(userId).child("meals");

        HashMap<String, String> mealData = new HashMap<>();
        mealData.put("recipe", recipe);
        mealData.put("imageUrl", imageUrl);
        mealData.put("calories", calories);

        mealsRef.child(date).child("lunch").setValue(mealData);
    }

    public void deleteLunchFromUser(String date) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference lunchRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                .child("meals").child(date).child("lunch");
        lunchRef.removeValue();
    }
    public void saveDinnerForUser(String date, String recipe, String imageUrl, String calories) {
        String userId = getCurrentUser().getUid();
        DatabaseReference mealsRef = mDatabase.child("Users").child(userId).child("meals");

        HashMap<String, String> mealData = new HashMap<>();
        mealData.put("recipe", recipe);
        mealData.put("imageUrl", imageUrl);
        mealData.put("calories", calories);

        mealsRef.child(date).child("dinner").setValue(mealData);
    }

    public void deleteDinnerFromUser(String date) {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference dinnerRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                .child("meals").child(date).child("dinner");
        dinnerRef.removeValue();
    }

    public void saveFavoriteMealForUser(String id, String title, List<String> ingredients, String imageUrl, int calories) {
        String userId = getCurrentUser().getUid();
        DatabaseReference mealsRef = mDatabase.child("Users").child(userId).child("meals");

        // Create a HashMap to hold the meal data
        HashMap<String, Object> mealData = new HashMap<>();
        mealData.put("id", id);
        mealData.put("title", title);
        mealData.put("imageUrl", imageUrl);
        mealData.put("calories", calories);
        mealData.put("ingredients", ingredients);

        // Save the favorite meal under the "favorites" node
        mealsRef.child("favorites").child(id).setValue(mealData);
    }


}
