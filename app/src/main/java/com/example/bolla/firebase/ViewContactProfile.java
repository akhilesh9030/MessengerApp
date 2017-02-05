package com.example.bolla.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewContactProfile extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    TextView first_name, last_name,gender;
    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();

    CircleImageView avatar_vw;
    Button send_button;

    User contact_user;
    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_contact_profile);

        first_name = (TextView) findViewById(R.id.firstNameValue);
        last_name = (TextView) findViewById(R.id.lastNameValue);
        gender = (TextView) findViewById(R.id.genderValue);

        send_button = (Button) findViewById(R.id.sendMessageButton);

        avatar_vw = (CircleImageView) findViewById(R.id.avatar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/*Fragment Activity*/,this/*On ConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        if(getIntent() != null){
            if(getIntent().getSerializableExtra(InboxActivity.CONTACT_TAG) != null){
                contact_user = (User) getIntent().getSerializableExtra(InboxActivity.CONTACT_TAG);

                first_name.setText(contact_user.getFirstname());
                last_name.setText(contact_user.getLastname());

                if (contact_user.getPhotoUrl().equals("") || contact_user.getPhotoUrl() == null) {
                    avatar_vw
                            .setImageDrawable(ContextCompat.getDrawable(ViewContactProfile.this,
                                    R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(ViewContactProfile.this)
                            .load(contact_user.getPhotoUrl())
                            .into(avatar_vw);
                }

                gender.setText(contact_user.getGender());

            }
        }

        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User login_user = dataSnapshot.getValue(User.class);
                        Intent intent = new Intent(ViewContactProfile.this,ChatActivity.class);
                        intent.putExtra(InboxActivity.CONTACT_TAG,contact_user);
                        intent.putExtra(InboxActivity.CURRENT_USER,login_user);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                Log.d("demo","inside menu  selected");
                FirebaseAuth.getInstance().signOut();

                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                Intent intent = new Intent(ViewContactProfile.this,SignInActivity.class);
                startActivity(intent);
                break;
            case R.id.myProfile:
                Intent intent_profile = new Intent(ViewContactProfile.this,EditProfileActivity.class);
                startActivity(intent_profile);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ViewContactProfile.this,InboxActivity.class);
        startActivity(intent);
    }
}
