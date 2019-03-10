package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
        loginSignInButton.setOnClickListener(listener->{
            EditText loginEmailEdit = findViewById(R.id.login_email_edit);
            EditText loginPasswordEdit = findViewById(R.id.login_password_edit);
            firebaseAuth.signInWithEmailAndPassword(loginEmailEdit.getText().toString(),loginPasswordEdit.getText().toString())
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful())
                        {
                            Intent openMainActivity = new Intent(this,MainActivity.class);
                            startActivity(openMainActivity);
                            finish();
                        }
                    });
        });
    }
}
