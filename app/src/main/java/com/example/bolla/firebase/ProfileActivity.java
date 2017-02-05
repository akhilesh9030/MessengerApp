package com.example.bolla.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    CircleImageView avatar;
    ImageView editVw;
    TextView first_name,last_name,gender;

    User u;
    public static final String USER_TAG = "user";

    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();
    StorageReference f_storage = FirebaseStorage.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        avatar = (CircleImageView) findViewById(R.id.avatarVw);
        editVw = (ImageView) findViewById(R.id.editImageView);
        first_name = (TextView) findViewById(R.id.firstNameView);
        last_name = (TextView) findViewById(R.id.lastNameView);
        gender = (TextView) findViewById(R.id.genderView);




        editVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra(USER_TAG,u);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                u = dataSnapshot.getValue(User.class);
                first_name.setText(u.getFirstname());
                last_name.setText(u.getLastname());
                gender.setText(u.getGender());

                if (u.getPhotoUrl().equals("") || u.getPhotoUrl() == null) {
                    avatar
                            .setImageDrawable(ContextCompat.getDrawable(ProfileActivity.this,
                                    R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(ProfileActivity.this)
                            .load(u.getPhotoUrl())
                            .into(avatar);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                Log.d("demo","inside menu  selected");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ProfileActivity.this,SignInActivity.class);
                startActivity(intent);
                break;
            case R.id.inbox:
                Intent inbox_intent = new Intent(ProfileActivity.this,InboxActivity.class);
                startActivity(inbox_intent);


                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
