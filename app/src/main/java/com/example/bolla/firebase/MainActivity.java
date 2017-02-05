package com.example.bolla.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

//import com.google.android.gms.common.api.ResultCallback;
//import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.OnConnectionFailedListener {
    SignInButton signInButton;
    Button signOutButon, sunny, foggy;
    TextView statusText, condVw;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    //DatabaseReference mCondRef = mRootRef.child("condition");
    DatabaseReference mMessgRef;
    ListView lv;
    Button login, signUp;
    EditText email, pwd, email_create, pwd_create;
    ArrayList<String> messages = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("demo", " onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("demo", "onAuthStateChanged:signed_out");
                }
            }
        };

        condVw = (TextView) findViewById(R.id.textViewCond);
        sunny = (Button) findViewById(R.id.buttonSunny);
        foggy = (Button) findViewById(R.id.buttonFoggy);
        login = (Button) findViewById(R.id.loginButton);
        signUp = (Button) findViewById(R.id.signUpButton);
        email = (EditText) findViewById(R.id.emailText);
        pwd = (EditText) findViewById(R.id.pwdText);
        email_create = (EditText) findViewById(R.id.emailCreate);
        pwd_create = (EditText) findViewById(R.id.pwdCreate);

        lv = (ListView) findViewById(R.id.listView);

        statusText = (TextView) findViewById(R.id.txtVw);
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

       // signOutButon = (Button) findViewById(R.id.buttonSigOut);
       // signOutButon.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/*Fragment Activity*/,this/*On ConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mMessgRef = mRootRef.child("messages");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,messages);
        lv.setAdapter(adapter);

        mAuth.addAuthStateListener(mAuthListener);



//        mCondRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                 String text =  dataSnapshot.getValue(String.class);
//                 condVw.setText(text);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//        sunny.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mCondRef.setValue("Sunny Button");
//            }
//        });
//        foggy.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mCondRef.setValue("Foggy Button");
//            }
//        });


        mMessgRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String message = dataSnapshot.getValue(String.class);
                Log.d("demo","Add"+message);
                messages.add(message);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String message = dataSnapshot.getValue(String.class);
                Log.d("demo","Change"+message);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String message = dataSnapshot.getValue(String.class);
                Log.d("demo","Removed"+message);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        Log.d("demo","in side onClick");
      switch (view.getId()){
          case R.id.sign_in_button:
              signIn();
              break;
          //case R.id.buttonSigOut:
           //   signOut();
          case R.id.signUpButton:
              Log.d("demo","Signed Up");
              mAuth.createUserWithEmailAndPassword(email_create.getText().toString(),pwd_create.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                  @Override
                  public void onComplete(@NonNull Task<AuthResult> task) {
                      Log.d("demo", "createUserWithEmail:onComplete:" + task.isSuccessful());

                      // If sign in fails, display a message to the user. If sign in succeeds
                      // the auth state listener will be notified and logic to handle the
                      // signed in user can be handled in the listener.
                      if (!task.isSuccessful()) {
                          Toast.makeText(MainActivity.this, "SignUp Failed",
                                  Toast.LENGTH_SHORT).show();
                      }
                  }
              });
          case R.id.loginButton:
              break;
      }
    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            //Signed in Successfully, show authenticated UI
            GoogleSignInAccount acct = result.getSignInAccount();
            statusText.setText("Hello, " + acct.getDisplayName());
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

//    private void signOut(){
//        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
//            @Override
//            public void onResult(@NonNull Status status) {
//               statusText.setText("Signed Out");
//            }
//        });
//
//    }


//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//      Log.d(TAG, "onConnectionFailed:" + connectionResult);
//    }
//}

