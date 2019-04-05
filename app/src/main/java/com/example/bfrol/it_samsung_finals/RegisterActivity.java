package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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
                firebaseAuth.createUserWithEmailAndPassword(registerEmail.getText().toString(),registerPassword.getText().toString())
                        .addOnCompleteListener(this,task -> {
                            if(task.isSuccessful())
                            {
                                //successfully added user with email and password to the Firebase Auth system which is NOT the database
                                FirebaseFirestore database = FirebaseFirestore.getInstance(); //get an instance of the Cloud Firestore Database
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                database.collection("users").document(firebaseUser.getUid()).set(constructUser()).//add current user to the database
                                        addOnSuccessListener(aVoid -> {
                                            openMainActivity(); //successfully added user to the database
                                        }).
                                        addOnFailureListener(e -> {
                                            Toast.makeText(getApplicationContext(),"Register failed",Toast.LENGTH_LONG).show();
                                            //adding user to the database failed
                                            firebaseUser.delete();//delete user record in the Firebase Auth
                                        });

                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Register failed",Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }
    private void openMainActivity()
    {
        Intent openMainActivity = new Intent(this,MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// clear activity history
        startActivity(openMainActivity);
        finish();
    }
    private User constructUser()
    {
        //Constructs a new User instance based on the UI
        String name = ((EditText)findViewById(R.id.register_name)).getText().toString();
        String lastName = ((EditText)findViewById(R.id.register_surname)).getText().toString();
        String socialMedia = ((EditText)findViewById(R.id.register_social_media)).getText().toString();
        String country = ((EditText)findViewById(R.id.register_country)).getText().toString();
        String city = ((EditText)findViewById(R.id.register_city)).getText().toString();
        String demands = ((EditText)findViewById(R.id.register_demands)).getText().toString();
        String profileUrl =""; //TODO image query
        return new User(name,lastName,socialMedia,country,city,demands,profileUrl,0);
    }
}
