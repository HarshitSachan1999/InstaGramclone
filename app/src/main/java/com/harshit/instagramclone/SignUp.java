package com.harshit.instagramclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity implements View.OnKeyListener, View.OnClickListener {

    FirebaseAuth mAuth;
    EditText name, signUpEmail, signUpPassword;
    Intent intent;
    DatabaseReference mDatabase;
    FirebaseUser currentUser ;

    public void signUp(View view){

        if(signUpEmail.getText().toString().equals("") || signUpPassword.getText().toString().equals("")){
            Toast.makeText(this, "Enter Username/Password", Toast.LENGTH_LONG).show();
        }
        else{
                mAuth.createUserWithEmailAndPassword(signUpEmail.getText().toString(), signUpPassword.getText().toString())
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    currentUser = mAuth.getCurrentUser();
                                    mDatabase.child("Users").child(currentUser.getUid()).child("Name").setValue(name.getText().toString());
                                    mDatabase.child("Users").child(currentUser.getUid()).child("E-mail").setValue(signUpEmail.getText().toString());
                                    Toast.makeText(SignUp.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                    Toast.makeText(SignUp.this, "Log In to continue", Toast.LENGTH_LONG).show();
                                } else {

                                    Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }

                                mAuth.signOut();

                            }
                        });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        name = findViewById(R.id.name);
        signUpEmail = findViewById(R.id.signUpEmail);
        signUpPassword = findViewById(R.id.signUpPassword);
        intent = new Intent(this, MainActivity.class);
        ConstraintLayout layout = findViewById(R.id.layout);
        layout.setOnClickListener(this);
        signUpPassword.setOnKeyListener(this);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == event.KEYCODE_ENTER && event.getAction() == event.ACTION_DOWN)
            signUp(v);
        return false;
    }

    @Override
    public void onClick(View v) {
        InputMethodManager input = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        input.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}
