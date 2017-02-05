package com.example.bolla.firebase;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bolla on 11/19/2016.
 */
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {
    private ArrayList<InboxObject> inbox_list;
    private Context mContext;
    private inbox_data inbox_interface;

    public interface inbox_data{
        void onInboxClick(User u);
    }

    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();
    User contact_user;


  //  private iData inbox_interface;


    public InboxAdapter(ArrayList<InboxObject> inbox_list, Context mContext) {
        this.inbox_list = inbox_list;
        this.mContext = mContext;
        inbox_interface = (inbox_data) mContext;
    }

    @Override
    public InboxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_inbox, parent, false);
        return new InboxViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(final InboxViewHolder holder, int position) {

      final InboxObject io = inbox_list.get(position);

        holder.lastMsg.setText(io.getLastMessage().getMessage());

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(io.getLastMessage().getCreatedAt());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.time.setText(new SimpleDateFormat("HH:mm-EEE").format(date));

        if(io.getLastMessageRead() == true){
            holder.star.setVisibility(View.INVISIBLE);
        }else{
            holder.star.setVisibility(View.VISIBLE);
        }



        contact_user = io.getReceiver_user();
        holder.contact_username.setText(contact_user.getFirstname()+" "+contact_user.getLastname());

        if(contact_user.getPhotoUrl() == null || contact_user.getPhotoUrl().equals("")){
            holder.contact_avatar
                    .setImageDrawable(ContextCompat.getDrawable(mContext,
                            R.drawable.ic_account_circle_black_36dp));
        }
        else{
            Picasso.with(mContext).load(contact_user.getPhotoUrl())
                    .into(holder.contact_avatar);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                InboxObject current_object = io;
                contact_user=io.getReceiver_user();
                current_object.setLastMessageRead(true);
                if(inbox_interface != null) {
                    f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxobjects").child(contact_user.getUid()).setValue(current_object);
                    inbox_interface.onInboxClick(contact_user);
                }
                else{
                    Log.d("demo","click failed");
                }
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxobjects").child(contact_user.getUid()).removeValue();
                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(contact_user.getUid()).removeValue();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return inbox_list.size();
    }

    public class InboxViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView contact_avatar;
        private TextView contact_username, time;
        private TextView lastMsg;
        private View container;
        private ImageView star;


        public InboxViewHolder(View itemView) {
            super(itemView);
            contact_username = (TextView) itemView.findViewById(R.id.messengerTextView);
            lastMsg = (TextView) itemView.findViewById(R.id.messageTextView);
            contact_avatar = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            container = (View) itemView.findViewById(R.id.rowContainer);

            time = (TextView) itemView.findViewById(R.id.timeView);
            star = (ImageView) itemView.findViewById(R.id.starView);

            //container.setOnClickListener(this);
        }

    }
}
