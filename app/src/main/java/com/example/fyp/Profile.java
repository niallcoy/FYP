package com.example.fyp; // Update with your package name

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView nameAgeTextView;
    private EditText calorieGoalEditText;
    private Button saveButton;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameAgeTextView = findViewById(R.id.nameAgeTextView);
        calorieGoalEditText = findViewById(R.id.calorieGoalEditText);
        saveButton = findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue().toString();
                    String age = snapshot.child("age").getValue().toString();
                    String calorieGoal = snapshot.child("calorieGoal").getValue() != null
                            ? snapshot.child("calorieGoal").getValue().toString()
                            : "";

                    nameAgeTextView.setText(getString(R.string.name_age_format, name, age));

                    calorieGoalEditText.setText(calorieGoal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newCalorieGoal = calorieGoalEditText.getText().toString().trim();
                userRef.child("calorieGoal").setValue(newCalorieGoal);
            }
        });
    }
}
