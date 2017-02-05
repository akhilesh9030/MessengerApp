package com.example.bolla.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    EditText firstname, lastname;
    EditText email;
    EditText choose_password, repeat_password;
    Button signup;
    Button cancel;
    User user=new User();

    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);



        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("demo", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("demo", "onAuthStateChanged:signed_out");
                }
            }
        };

        firstname=(EditText)findViewById(R.id.firstName);
        lastname=(EditText)findViewById(R.id.lastname);
        email=(EditText)findViewById(R.id.emailText);
        choose_password=(EditText)findViewById(R.id.pwdChoose);
        repeat_password=(EditText)findViewById(R.id.pwdRepeat);

        cancel=(Button)findViewById(R.id.cancelButton) ;
        signup=(Button)findViewById(R.id.signUp);
        signup.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.signUp:
                if(email.getText().toString().equals("") || choose_password.getText().toString().equals("")||firstname.getText().equals("")
                        || lastname.getText().toString().equals("") || repeat_password.getText().toString().equals("")){

                    Toast.makeText(SignUpActivity.this, "Email,Password and first name and last name cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(choose_password.getText().toString().equals(repeat_password.getText().toString())){
                        user.setEmail(email.getText().toString());
                        user.setFirstname(firstname.getText().toString());
                        user.setLastname(lastname.getText().toString());
                        user.setPassword(choose_password.getText().toString());
                        user.setGender("");
                        user.setPhotoUrl("");
                        signup(firstname.getText().toString(),email.getText().toString(),choose_password.getText().toString());
                    }
                    else {
                        Toast.makeText(SignUpActivity.this, "Passwords should match", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.cancelButton:
                finish();
                break;

        }
}

    private void signup(String name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(SignUpActivity.this, "Email already exists/choose password above 5 characters", Toast.LENGTH_SHORT).show();
                }else{

                    user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());


                  //  f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                    f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").setValue(user);
                    Toast.makeText(SignUpActivity.this, "Account created! Please, log in.", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();

                    Intent in = new Intent(SignUpActivity.this,SignInActivity.class);
                    startActivity(in);
                }
            }
        });
    }

        @Override
        protected void onStart() {
            super.onStart();
            mAuth.addAuthStateListener(mAuthListener);
        }

        @Override
        protected void onStop() {
            super.onStop();
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        }
}
