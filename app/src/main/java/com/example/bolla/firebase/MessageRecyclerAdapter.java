package com.example.bolla.firebase;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ocpsoft.pretty.time.PrettyTime;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by bolla on 11/19/2016.
 */
public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MessgaeHolder> {
    private ArrayList<Message> messages;
    private Context mContext;

    DatabaseReference f_database = FirebaseDatabase.getInstance().getReference();
    StorageReference f_storage = FirebaseStorage.getInstance().getReference();

    public MessageRecyclerAdapter(ArrayList<Message> messages, Context mContext) {
        this.messages = messages;
        this.mContext = mContext;
    }

    @Override
    public MessgaeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_message, parent, false);
        return new MessgaeHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(MessgaeHolder holder, int position) {
        final Message msg = messages.get(position);

        PrettyTime p = new PrettyTime();
        Date d = null;
        try {
            d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(msg.getCreatedAt());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (msg.getSender_uid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

              holder.send_time.setText(p.format(d));
              holder.receiver_layout.setVisibility(View.GONE);
              holder.sender_layout.setBackgroundColor(Color.GREEN);

              if(msg.getImageUrl().equals("") || msg.getImageUrl() == null){
                  holder.send_msg.setText(msg.getMessage());
                  holder.send_img.setVisibility(View.GONE);
              }
              else{
                  Picasso.with(mContext).load(msg.getImageUrl()).into(holder.send_img);
                  holder.send_msg.setVisibility(View.GONE);
              }
        }
        else{
            holder.sender_layout.setVisibility(View.GONE);
            holder.receiver_layout.setBackgroundColor(Color.BLUE);
            holder.rcv_time.setText(p.format(d));
            if(msg.getImageUrl().equals("") || msg.getImageUrl() == null){
                holder.rcv_msg.setText(msg.getMessage());
                holder.rcv_img.setVisibility(View.GONE);
            }
            else{
                Picasso.with(mContext).load(msg.getImageUrl()).into(holder.rcv_img);
                holder.rcv_msg.setVisibility(View.GONE);
            }
        }

        holder.sender_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(msg.getReceiver_uid()).child("messages").child(msg.getId()).removeValue();
                return true;
            }
        });

        holder.receiver_layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                f_database.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inbox").child(msg.getSender_uid()).child("messages").child(msg.getId()).removeValue();
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MessgaeHolder extends RecyclerView.ViewHolder{
        TextView send_msg, send_time, rcv_msg, rcv_time;
        ImageView send_img, rcv_img;

        LinearLayout sender_layout, receiver_layout;

        public MessgaeHolder(View itemView) {
            super(itemView);

            send_msg=(TextView)itemView.findViewById(R.id.messageSender);
            send_time=(TextView)itemView.findViewById(R.id.senderMsgTime);
            rcv_msg=(TextView)itemView.findViewById(R.id.messageReceiver);
            rcv_time = (TextView) itemView.findViewById(R.id.receiverMsgTime);

            send_img=(ImageView) itemView.findViewById(R.id.imageSender);
            rcv_img = (ImageView) itemView.findViewById(R.id.imageReceiver);

            sender_layout=(LinearLayout) itemView.findViewById(R.id.senderLayout);
            receiver_layout = (LinearLayout) itemView.findViewById(R.id.receiverLayout);


        }
    }
}
