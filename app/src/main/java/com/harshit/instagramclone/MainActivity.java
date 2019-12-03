package com.harshit.instagramclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnKeyListener, View.OnClickListener {

        EditText logInEmail, logInPassword;
        Intent intent, intent2;
    FirebaseAuth mAuth;

    public void logIn(View view){

        if(logInEmail.getText().toString().equals("") || logInPassword.getText().toString().equals("")){

            Toast.makeText(this, "Enter Email/Password", Toast.LENGTH_LONG).show();
        }else{

            mAuth.signInWithEmailAndPassword(logInEmail.getText().toString(), logInPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                startActivity(intent);
                            }else{
                                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        logInEmail = findViewById(R.id.loginEmail);
        logInPassword = findViewById(R.id.loginpaswd);
        intent = new Intent(this, loggedIn.class);
        intent2 = new Intent(this, SignUp.class);
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setOnClickListener(this);
        logInPassword.setOnKeyListener(this);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            startActivity(intent);
        }

    }



/*
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Toast.makeText(this, currentUser.getEmail(), Toast.LENGTH_SHORT).show();
    }*/

    public void newUser(View v) {

        startActivity(intent2);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if(keyCode == event.KEYCODE_ENTER && event.getAction() == event.ACTION_DOWN)
            logIn(v);
        return false;
    }

    @Override
    public void onClick(View v) {

        InputMethodManager input = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        input.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}
