package com.example.fyp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


}

