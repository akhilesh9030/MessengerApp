package com.example.bolla.firebase;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bolla on 11/19/2016.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>  {

    private ArrayList<User> list_users;
    private Context mContext;
    private iData inbox_interface;

    public interface iData{
      void onItemClick(User u);
    }

    public ContactsAdapter(ArrayList<User> list_users, Context mContext) {
        this.list_users = list_users;
        this.mContext = mContext;
        inbox_interface = (iData) mContext;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_contact, parent, false);
        return new ContactsViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(ContactsViewHolder holder, int position) {

      final User contact_user = list_users.get(position);
        Log.d("demo","Inside Bind"+ contact_user.getFirstname());
        holder.first_name.setText(contact_user.getFirstname());
        holder.last_name.setText(contact_user.getLastname());

        if (contact_user.getPhotoUrl().equals("") || contact_user.getPhotoUrl() == null) {
            holder.user_avatar
                    .setImageDrawable(ContextCompat.getDrawable(mContext,
                            R.drawable.ic_account_circle_black_36dp));
        } else {
            Picasso.with(mContext).load(contact_user.getPhotoUrl())
                    .into(holder.user_avatar);
        }



        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(inbox_interface != null) {
                  inbox_interface.onItemClick(contact_user);
              }
                else{
                  Log.d("demo","click failed");
              }

            }
        });
    }

    @Override
    public int getItemCount() {
        return list_users.size();
    }

    class ContactsViewHolder extends RecyclerView.ViewHolder {

        private TextView first_name,last_name;
        private CircleImageView user_avatar;
        private View container;

        public ContactsViewHolder(View itemView) {

            super(itemView);
            Log.d("demo","inside view Holder");
            first_name = (TextView) itemView.findViewById(R.id.nameInContact);
            last_name = (TextView) itemView.findViewById(R.id.lastNameInContact);
            user_avatar = (CircleImageView) itemView.findViewById(R.id.avatarInContact);
            container = (View) itemView.findViewById(R.id.containerLayout);

            //container.setOnClickListener(this);

        }

//        @Override
//        public void onClick(View view) {
//           if(view.getId() == R.id.containerLayout){
//
//           }
//        }
    }

}
