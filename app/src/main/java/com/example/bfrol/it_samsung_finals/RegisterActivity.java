package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //TODO add user profile image loading
        firebaseAuth = FirebaseAuth.getInstance();
        Button registerRegisterButton = findViewById(R.id.register_register_button);
        registerRegisterButton.setOnClickListener(view -> {
            EditText registerPassword = findViewById(R.id.register_password);
            EditText registerPasswordRepeat = findViewById(R.id.register_password_repeat);
            //TODO check password and email for validity
            if (!registerPassword.getText().toString().equals(registerPasswordRepeat.getText().toString())) {
                Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_LONG).show();
            } else {
                EditText registerEmail = findViewById(R.id.register_email);
                firebaseAuth.createUserWithEmailAndPassword(registerEmail.getText().toString(), registerPassword.getText().toString())
                        .addOnSuccessListener(this, task -> {
                            //successfully added user with email and password to the Firebase Auth system which is NOT the database
                            FirebaseFirestore database = FirebaseFirestore.getInstance(); //get an instance of the Cloud Firestore Database
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            database.collection("users").document(firebaseUser.getUid()).set(constructUser(firebaseUser.getUid())).//add current user to the database
                                    addOnSuccessListener(aVoid -> {
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                Uri imageUri = Uri.parse("android.resource://com.example.bfrol.it_samsung_finals/drawable/user_placeholder");
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                    storage.getReference("images/" + firebaseUser.getUid() + ".jpg").putStream(inputStream);
                                } catch (FileNotFoundException e) {
                                    Log.e("error", e.getMessage());
                                }
                                MainActivity.currentUser = null;
                                openMainActivity(); //successfully added user to the database
                            }).
                                    addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "Register failed", Toast.LENGTH_LONG).show();
                                        //adding user to the database failed
                                        firebaseUser.delete();//delete user record in the Firebase Auth
                                    });


                        }).addOnFailureListener(e -> {
                            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                });
            }
        });

    }

    private void openMainActivity() {
        Intent openMainActivity = new Intent(this, MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// clear activity history
        startActivity(openMainActivity);
        finish();
    }

    private User constructUser(String uid) {

        //Constructs a new User instance based on the UI
        String name = ((EditText) findViewById(R.id.register_name)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.register_surname)).getText().toString();
        String socialMedia = ((EditText) findViewById(R.id.register_social_media)).getText().toString();
        String country = ((EditText) findViewById(R.id.register_country)).getText().toString();
        String city = ((EditText) findViewById(R.id.register_city)).getText().toString();
        String demands = ((EditText) findViewById(R.id.register_demands)).getText().toString();
        return new User(name, lastName, socialMedia, country, city, demands, uid, 0, new HashMap<>());
    }
}
