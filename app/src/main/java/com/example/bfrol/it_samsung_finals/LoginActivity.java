package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {
    private Toolbar loginToolbar;
    private Button loginSignUpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginToolbar = findViewById(R.id.login_toolbar);
        loginToolbar.setTitle("");
        setSupportActionBar(loginToolbar);
        loginSignUpButton = findViewById(R.id.login_sign_up_button);
        loginSignUpButton.setOnClickListener(listener ->
        {
            Intent openRegisterActivityIntent = new Intent(this,RegisterActivity.class);
            startActivity(openRegisterActivityIntent);
        });
    }
}
