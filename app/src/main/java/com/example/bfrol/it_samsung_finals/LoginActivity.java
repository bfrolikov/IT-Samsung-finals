package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth; // class that provides Firebase authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar loginToolbar = findViewById(R.id.login_toolbar);
        loginToolbar.setTitle("");
        setSupportActionBar(loginToolbar);
        Button loginSignUpButton = findViewById(R.id.login_sign_up_button);
        loginSignUpButton.setOnClickListener(v -> {
            Intent openRegisterActivityIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            LoginActivity.this.startActivity(openRegisterActivityIntent);
        });
        firebaseAuth = FirebaseAuth.getInstance();
        Button loginSignInButton = findViewById(R.id.login_sign_in_button);
        loginSignInButton.setOnClickListener(listener -> {
            EditText loginEmailEdit = findViewById(R.id.login_email_edit);
            EditText loginPasswordEdit = findViewById(R.id.login_password_edit);
            //TODO check password and email for validity
            firebaseAuth.signInWithEmailAndPassword(loginEmailEdit.getText().toString(), loginPasswordEdit.getText().toString())
                    .addOnSuccessListener(this, task -> {
                        //TODO implement snackbar with information about the login status
                        MainActivity.currentUser = null;
                        //authentication successful
                        openMainActivity();

                    }).addOnFailureListener(e -> {
                        Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (firebaseAuth.getCurrentUser() != null) // check if the user is signed in
        {
            openMainActivity();
        }
    }

    private void openMainActivity() {
        Intent openMainActivity = new Intent(this, MainActivity.class);
        openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// clear activity history
        startActivity(openMainActivity);
        finish();
        //login or register successful, open main activity

    }
}
