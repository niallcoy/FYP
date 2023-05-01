package com.example.fyp;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


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

    public void saveBreakfastForUser(String date, String meal) {
        String userId = getCurrentUser().getUid();
        DatabaseReference mealsRef = mDatabase.child("Users").child(userId).child("meals");
        mealsRef.child(date).child("breakfast").setValue(meal);
    }

    public void getMealsForDate(String date, ValueEventListener listener) {
        String userId = getCurrentUser().getUid();
        DatabaseReference mealsRef = mDatabase.child("Users").child(userId).child("meals").child(date);
        mealsRef.addListenerForSingleValueEvent(listener);
    }

    public void saveLunchForUser(String date, String lunch) {
        String userId = getCurrentUser().getUid();
        DatabaseReference mealsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("meals").child(date);
        mealsRef.child("lunch").setValue(lunch);
    }
    public void deleteBreakfastForUser(String date, String breakfastItem) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(getCurrentUser().getUid());
        DatabaseReference mealsRef = userRef.child("meals").child(date).child("breakfast");
        mealsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(String.class).equals(breakfastItem)) {
                    mealsRef.setValue(null); // Remove the breakfast item from Firebase
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }




}