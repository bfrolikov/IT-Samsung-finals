package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

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
                                openMainActivity();
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
}
