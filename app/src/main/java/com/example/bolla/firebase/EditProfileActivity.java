package com.example.bolla.firebase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    EditText first_name, last_name;
    Switch gender_switch;
    CircleImageView avatar_vw;
    Button save_button, cancel_button;
    String gender;
    TextView gender_value;
    User old_user, new_user;
    public final static int GALLERY_CODE = 100;
    GoogleApiClient mGoogleApiClient;

    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();
    StorageReference f_storage = FirebaseStorage.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/*Fragment Activity*/,this/*On ConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        first_name = (EditText) findViewById(R.id.firstNameEdit);
        last_name = (EditText) findViewById(R.id.lastNameEdit);
        gender_switch = (Switch) findViewById(R.id.genderSwitch);
        save_button = (Button) findViewById(R.id.saveProfileButton);
        cancel_button = (Button) findViewById(R.id.cancelButton);
        avatar_vw = (CircleImageView) findViewById(R.id.avatar);
        gender_value = (TextView) findViewById(R.id.genderValue);

        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                old_user = dataSnapshot.getValue(User.class);
                first_name.setText(old_user.getFirstname());
                last_name.setText(old_user.getLastname());
                gender_value.setText(old_user.getGender());
                Log.d("demo",gender_value.getText().toString());
                if(old_user.getGender().equals("M")){
                    Log.d("demo","inside M");
                    gender_switch.setChecked(true);
                }
                else{
                    Log.d("demo","inside F");
                    gender_switch.setChecked(false);
                }

                if (old_user.getPhotoUrl().equals("") || old_user.getPhotoUrl() == null) {
                    avatar_vw
                            .setImageDrawable(ContextCompat.getDrawable(EditProfileActivity.this,
                                    R.drawable.ic_account_circle_black_36dp));
                } else {
//                    Glide.with(EditProfileActivity.this)
//                            .load(old_user.getPhotoUrl())
//                            .into(avatar_vw);

                    Picasso.with(EditProfileActivity.this).load(old_user.getPhotoUrl())
                            .into(avatar_vw);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = ((BitmapDrawable)avatar_vw.getDrawable()).getBitmap();

                Uri tempUri = getImageUri(getApplicationContext(), bitmap);

                f_storage.child("images").child(tempUri.getLastPathSegment()).putFile(tempUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d("demo","Dow nload url:"+taskSnapshot.getDownloadUrl().toString());

                        new_user = new User();
                        new_user.setEmail(old_user.getEmail());
                        new_user.setFirstname(first_name.getText().toString());
                        new_user.setLastname(last_name.getText().toString());
                        new_user.setUid(old_user.getUid());
                        if(gender_switch.isChecked()){
                            new_user.setGender(gender_switch.getTextOn().toString());
                        }
                        else{
                            new_user.setGender(gender_switch.getTextOff().toString());
                        }
                        new_user.setPhotoUrl(taskSnapshot.getDownloadUrl().toString());
                        new_user.setPassword("");

                       // f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new_user);
                        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").setValue(new_user);

                        Toast.makeText(EditProfileActivity.this, "Profile Saved", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        avatar_vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_CODE);
            }
        });

        gender_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    gender_value.setText("M");
                }
                else{
                    gender_value.setText("F");
                }
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            Uri uri = data.getData();

            Picasso.with(EditProfileActivity.this).load(uri.toString())
                           .into(avatar_vw);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onStart() {
        super.onStart();


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

                LoginManager.getInstance().logOut();

                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                Intent intent = new Intent(EditProfileActivity.this,SignInActivity.class);
                startActivity(intent);
                break;
            case R.id.inbox:
                Intent inbox_intent = new Intent(EditProfileActivity.this,InboxActivity.class);
                startActivity(inbox_intent);


                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
