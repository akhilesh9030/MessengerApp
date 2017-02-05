package com.example.bolla.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.facebook.login.LoginManager;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

public class InboxActivity extends AppCompatActivity implements ContactsAdapter.iData,InboxAdapter.inbox_data,GoogleApiClient.OnConnectionFailedListener{
    RecyclerView contacts_rv, inbox_rv;

    LinearLayoutManager messageLayoutManager,contactsLayoutManager;
    ContactsAdapter adapter_contacts;
    InboxAdapter adapter_inbox;
    ArrayList<User> all_users = new ArrayList<>();
    User current;
    ArrayList<InboxObject> allInboxObjects = new ArrayList<>();

    public static final String CONTACT_TAG = "contact_user";
    public static final String CURRENT_USER = "current_user";

    GoogleApiClient mGoogleApiClient;


    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();
    StorageReference f_storage = FirebaseStorage.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        contacts_rv = (RecyclerView) findViewById(R.id.contactsRecyclerView);
        contactsLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        contacts_rv.setLayoutManager(contactsLayoutManager);

        inbox_rv = (RecyclerView) findViewById(R.id.inboxsRecyclerView);
        messageLayoutManager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false);
        inbox_rv.setLayoutManager(messageLayoutManager);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this/*Fragment Activity*/,this/*On ConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("demo","onStart");
        f_database.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                all_users.clear();
                Log.d("demo","onStart:inside onDatachange");
                for (com.google.firebase.database.DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    User u = snapshot.child("profile").getValue(User.class);
                    all_users.add(u);
                }

                Iterator<User> iter = all_users.iterator();
                while(iter.hasNext()){
                    User user = iter.next();
                    if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(user.getUid())){
                        current=user;
                        iter.remove();
                    }
                }
                Log.d("demo","Number of Users is"+ all_users.size());

                adapter_contacts = new ContactsAdapter(all_users,InboxActivity.this);
                Log.d("demo","after adapter");
                contacts_rv.setAdapter(adapter_contacts);
                Log.d("demo","set adapter");
                adapter_contacts.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxobjects").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("demo","onStart:inside inbox data change");
                allInboxObjects.clear();
                for (com.google.firebase.database.DataSnapshot snapshot: dataSnapshot.getChildren()) {


                    if(snapshot.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                    }
                    else{
                        InboxObject io = snapshot.getValue(InboxObject.class);
                        allInboxObjects.add(io);
                    }

                }


                Log.d("demo","Number of inbox is"+ allInboxObjects.size());

                Collections.sort(allInboxObjects, new Comparator<InboxObject>() {
                    @Override
                    public int compare(InboxObject inboxObject, InboxObject t1) {
                        Date d1 = null;
                        Date d2 = null;
                        try {
                            d1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inboxObject.getLastMessage().getCreatedAt());
                            d2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(t1.getLastMessage().getCreatedAt());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if(d1.before(d2)){
                            return 1;
                        }
                        else if(d1.after(d2)){
                            return  -1;
                        }
                        else{
                            return 0;
                        }
                    }
                });

                adapter_inbox = new InboxAdapter(allInboxObjects,InboxActivity.this);
                inbox_rv.setAdapter(adapter_inbox);
                adapter_inbox.notifyDataSetChanged();
                //
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
                Log.d("demo","inside menu  selected");
                FirebaseAuth.getInstance().signOut();

                LoginManager.getInstance().logOut();

                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                Intent intent = new Intent(InboxActivity.this,SignInActivity.class);
                startActivity(intent);
                break;
            case R.id.myProfile:
                Intent intent_profile = new Intent(InboxActivity.this,EditProfileActivity.class);
                startActivity(intent_profile);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(User u) {
        Intent intent = new Intent(InboxActivity.this,ViewContactProfile.class);
        intent.putExtra(CONTACT_TAG,u);
        startActivity(intent);
    }

    @Override
    public void onInboxClick(User u) {
        Intent intent = new Intent(InboxActivity.this,ChatActivity.class);
        intent.putExtra(CONTACT_TAG,u);
        intent.putExtra(CURRENT_USER,current);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
