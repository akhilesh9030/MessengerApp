package com.example.bolla.firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    ImageView contact_image, add_msg, add_img;
    TextView contact_username;
    RecyclerView rv;
    EditText sendMsg;
    LinearLayoutManager messageLayoutManager;
    User contact_user,current_user;
    public final static int GALLERY_CODE = 100;
    ArrayList<Message> all_messages = new ArrayList<>();
    MessageRecyclerAdapter adapter;
    GoogleApiClient mGoogleApiClient;

    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();
    StorageReference f_storage = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/*Fragment Activity*/,this/*On ConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        rv = (RecyclerView) findViewById(R.id.recVw);
        messageLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(messageLayoutManager);

        contact_image = (ImageView) findViewById(R.id.messengerImageView);
        add_msg = (ImageView) findViewById(R.id.msgsend);
        add_img = (ImageView) findViewById(R.id.imgsend);

        sendMsg = (EditText) findViewById(R.id.sendmsg);
        contact_username = (TextView) findViewById(R.id.messengerTextView);



        if(getIntent() != null){
            if(getIntent().getSerializableExtra(InboxActivity.CONTACT_TAG) != null){
                contact_user = (User) getIntent().getSerializableExtra(InboxActivity.CONTACT_TAG);
            }
        }

       /* if(getIntent() != null){
            if(getIntent().getSerializableExtra(InboxActivity.CURRENT_USER) != null){
                current_user = (User) getIntent().getSerializableExtra(InboxActivity.CURRENT_USER);
            }
        }*/

        contact_username.setText(contact_user.getFirstname()+" "+contact_user.getLastname());

        if(contact_user.getPhotoUrl() == null || contact_user.getPhotoUrl().equals("")){
            contact_image
                    .setImageDrawable(ContextCompat.getDrawable(ChatActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
        }
        else{
            Picasso.with(ChatActivity.this).load(contact_user.getPhotoUrl())
                    .into(contact_image);
        }

        add_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = sendMsg.getText().toString();
                InboxObject ib = new InboxObject();
                Message m = new Message();
                m.setMessage(message);
                m.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                m.setImageUrl("");
                m.setSender_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                m.setReceiver_uid(contact_user.getUid());



                String key = f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(contact_user.getUid()).child("messages").push().getKey();

                Log.d("demo","Signed in User email  is and his uid is" + FirebaseAuth.getInstance().getCurrentUser().getUid() + " "+ FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                Log.d("demo","receiver is "+contact_user.getUid());

                m.setId(key);
                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(contact_user.getUid()).child("messages").child(key).setValue(m);
                f_database.child("users").child(contact_user.getUid()).child("inbox").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("messages").child(key).setValue(m);

                ib.setLastMessage(m);
                ib.setSender_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());


                ib.setLastMessageRead(false);
                ib.setReceiver_user(current_user);
                f_database.child("users").child(contact_user.getUid()).child("inboxobjects").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ib);

                ib.setLastMessageRead(true);
                ib.setReceiver_user(contact_user);
                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxobjects").child(contact_user.getUid()).setValue(ib);



            }
        });

        add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,GALLERY_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_CODE && resultCode == RESULT_OK){
            Uri uri = data.getData();

            f_storage.child("messageImages").child(uri.getLastPathSegment()).putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    InboxObject ib = new InboxObject();
                    Message m = new Message();
                    m.setMessage("");
                    m.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                    m.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                    m.setSender_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    m.setReceiver_uid(contact_user.getUid());

                    String key = f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(contact_user.getUid()).child("messages").push().getKey();
                    m.setId(key);
                    f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(contact_user.getUid()).child("messages").child(key).setValue(m);
                    f_database.child("users").child(contact_user.getUid()).child("inbox").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("messages").child(key).setValue(m);

                    m.setMessage("image");
                    ib.setLastMessage(m);
                    ib.setSender_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());


                    ib.setLastMessageRead(false);
                    ib.setReceiver_user(current_user);
                    f_database.child("users").child(contact_user.getUid()).child("inboxobjects").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(ib);

                    ib.setLastMessageRead(true);
                    ib.setReceiver_user(contact_user);
                    f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxobjects").child(contact_user.getUid()).setValue(ib);

                    Log.d("demo","Download url:"+taskSnapshot.getDownloadUrl().toString());
                    Toast.makeText(ChatActivity.this, "Image Sent", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatActivity.this, "Image Sent Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                current_user = dataSnapshot.getValue(User.class);
//                               if(us!=null){
//                                   if(us.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){}
//                               else{
//                                       all_users.add(us);
//                                   }
//
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(contact_user.getUid()).child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                all_messages.clear();

                InboxObject ib = new InboxObject();
                Message m = new Message();

                for (com.google.firebase.database.DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    all_messages.add(snapshot.getValue(Message.class));
                }

                Log.d("demo","M essage length  is"+ all_messages.size());

               if(all_messages.size() == 0){
                    m.setImageUrl(" ");
                    m.setMessage("");
                    m.setReceiver_uid(contact_user.getUid());
                    m.setSender_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                   m.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
                    m.setId("");
                }else{
                    m = all_messages.get(all_messages.size()-1);
                }

                ib.setLastMessage(m);
                ib.setSender_uid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                ib.setLastMessageRead(true);
                ib.setReceiver_user(contact_user);
                Log.d("chat",FirebaseAuth.getInstance().getCurrentUser().getUid());
                Log.d("chat",contact_user.getUid());

                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxobjects").child(contact_user.getUid()).setValue(ib);


                adapter = new MessageRecyclerAdapter(all_messages,ChatActivity.this);
                rv.setAdapter(adapter);
                adapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                Log.d("demo"," inside menu  selected");

                FirebaseAuth.getInstance().signOut();

                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                LoginManager.getInstance().logOut();


                Intent intent = new Intent(ChatActivity.this,SignInActivity.class);
                startActivity(intent);

                break;
            case R.id.myProfile:
                Intent intent_profile = new Intent(ChatActivity.this,EditProfileActivity.class);
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
        Intent intent = new Intent(ChatActivity.this, InboxActivity.class);
        startActivity(intent);

    }
}
