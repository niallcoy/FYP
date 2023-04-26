package com.example.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener{

    // Declare class variables
    private TextView title, registerButton;
    private EditText editRName, editREmail, editRAge, editRPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize class variables
        mAuth = FirebaseAuth.getInstance();
        title = (TextView) findViewById(R.id.title);
        title.setOnClickListener(this);
        registerButton = (Button) findViewById(R.id.registerButton);
        registerButton.setOnClickListener(this);
        editRName = (EditText) findViewById(R.id.rName);
        editREmail = (EditText) findViewById(R.id.rEmail);
        editRAge = (EditText) findViewById(R.id.rAge);
        editRPassword = (EditText) findViewById(R.id.rPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        // Handle click events for UI elements
        switch (v.getId()){
            case R.id.title:
                // If the title is clicked, go back to the main activity
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.registerButton:
                // If the register button is clicked, register the user
                registerUser();
                break;
        }
    }

    private void registerUser() {
        // Get user input data
        String name = editRName.getText().toString().trim();
        String email = editREmail.getText().toString().trim();
        String age = editRAge.getText().toString().trim();
        String password = editRPassword.getText().toString().trim();

        // Validate user input
        if(name.isEmpty()){
            editRName.setError("Full name is required");
            editRName.requestFocus();
            return;
        }
        if(email.isEmpty()){
            editREmail.setError("Email is required");
            editREmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editREmail.setError("Please provide a valid email");
            editREmail.requestFocus();
            return;
        }
        if(age.isEmpty()){
            editRAge.setError("Age is required");
            editRAge.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editRPassword.setError("Password is required");
            editRPassword.requestFocus();
            return;
        }
        if(password.length() < 6){
            editRPassword.setError("Password needs to be 6 or more characters");
            editRPassword.requestFocus();
            return;
        }

        // Show progress bar to indicate ongoing registration process
        progressBar.setVisibility(View.VISIBLE);

        // Use Firebase authentication to create a new user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            // If the account creation is successful, save the user's data to the Firebase Realtime Database
                            User user = new User(name, age, email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                // If the user data is saved successfully, show a success message to the user
                                                Toast.makeText(Register.this, "User has been Registered", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                            }else{
                                                // If there was an error saving the user data, show an error message to the user
                                                Toast.makeText(Register.this, "Failed to register", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });

                        }else{
                            // If there was an error making the user
                            Toast.makeText(Register.this, "Failed to register", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                    }
                };

    });


    }}