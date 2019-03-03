package com.example.bfrol.it_samsung_finals;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class LoginActivity extends AppCompatActivity {
    private Toolbar loginToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(loginToolbar);
    }
}
