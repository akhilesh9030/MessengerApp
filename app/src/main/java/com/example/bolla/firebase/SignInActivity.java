package com.example.bolla.firebase;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {
    FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    EditText email, password;
    Button login;
    Button signUp;

    SignInButton signInButton;
    GoogleApiClient mGoogleApiClient;

    LoginButton fb_login;
    CallbackManager mCallbackManager;

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";

    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_sign_in);

        //  FirebaseApp.initializeApp(this);
        //Firebase.setAndroidContext(this);



       // FirebaseAuth.getInstance().signOut();
        Log.d("demo","on create started");

        try{
            Log.d("demo","on create started 11111111111111111");
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.bolla.firebase", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));

            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        mAuth = FirebaseAuth.getInstance();

        email = (EditText) findViewById(R.id.emailText);
        password = (EditText) findViewById(R.id.passwordText);
        login = (Button) findViewById(R.id.loginButton);
        signUp = (Button) findViewById(R.id.signUpButton);
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        login.setOnClickListener(this);
        signUp.setOnClickListener(this);
        signInButton.setOnClickListener(this);

        fb_login = (LoginButton) findViewById(R.id.login_button);
        fb_login.setReadPermissions("email","public_profile");
        mCallbackManager = CallbackManager.Factory.create();

        fb_login.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, " facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());


            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/*Fragment Activity*/,this/*On ConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("tag", "onAuthStateChanged:signed_in:" + user.getUid());

                    Intent i = new Intent(SignInActivity.this, InboxActivity.class);
                    startActivity(i);
                } else {
                    // User is signed out
                    Log.d("tag", "onAuthStateChanged:signed_out ");

                }
                // ...
            }
        };
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        Log.d("dmeo","inside facebook authentication:start");

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());


        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Facebook signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Log.d("dmeo","inside facebook authentication:failure");
                            Toast.makeText(SignInActivity.this, "Facebook Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.d("dmeo","inside facebook authentication:success");
                            final User fb_user = new User();

                            Log.d("demo","Face Book url is"+ FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());

                            fb_user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            fb_user.setPhotoUrl(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                            fb_user.setFirstname(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[0]);
                            fb_user.setLastname(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ")[1]);
                            fb_user.setEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                            fb_user.setGender("");

                            f_database.child("users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int flag = 0;

                                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                                        User temp = ds.child("profile").getValue(User.class);
                                        if(temp.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            flag = 1;
                                            break;
                                        }
                                    }
                                    if(flag != 1){
                                        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").setValue(fb_user);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                            f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").setValue(fb_user);

                            Intent i = new Intent(SignInActivity.this, InboxActivity.class);
                            startActivity(i);




                        }

                        // ...
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.loginButton:
                if(email.getText().toString().equals("") || password.getText().toString().equals("")){

                    Toast.makeText(SignInActivity.this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    validatelogin(email.getText().toString(),password.getText().toString());

                }

                break;
            case R.id.signUpButton:
                Intent k=new Intent(SignInActivity.this,SignUpActivity.class);
                startActivity(k);
                break;
            case R.id.sign_in_button:
                Log.d("demo", "pressed sign in button");
                googleSignIn();
                break;
        }
    }

    private void googleSignIn() {
        Log.d("demo", "inside sign in");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("demo", "inside activity result");

        if(requestCode == RC_SIGN_IN){
            Log.d("demo", "inside activity result iffff");
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
        else{
            Log.d("demo", "inside face book onActivity Result call back");
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        }

    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG,"handleSignInResult :"+result.isSuccess());
        if(result.isSuccess()){
            //Signed in Successfully, show authenticated UI

            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        final User user = new User();
        user.setFirstname(acct.getGivenName());
        user.setLastname(acct.getFamilyName());
        user.setEmail(acct.getEmail());
        user.setPassword("");
        if(acct.getPhotoUrl()!= null){
            user.setPhotoUrl(acct.getPhotoUrl().toString());}
        user.setGender("");
        Log.d(TAG,"Google user signed in:"+ acct.getDisplayName());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Log.d(TAG, "firebaseAuthWithGoogle: after credential");

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "sign InWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                        else{
                            user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            f_database.child("users").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    int flag = 0;

                                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                                        User temp = ds.child("profile").getValue(User.class);
                                        if(temp.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                            flag = 1;
                                              break;
                                        }
                                    }
                                    if(flag != 1){
                                        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").setValue(user);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            Intent i = new Intent(SignInActivity.this, InboxActivity.class);
                            startActivity(i);

                        }
                        // ...
                    }
                });
    }

    private void googleSignOut(){

        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {

            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void validatelogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("demo", "signInWithEmailAndPassword", task.getException());

                            Toast.makeText(SignInActivity.this, "Invalid credentials.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent j=new Intent(SignInActivity.this,InboxActivity.class);
                            startActivity(j);
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
