package com.example.bolla.firebase;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bolla on 11/19/2016.
 */
public class ContactsListViewAdapter extends ArrayAdapter<User> {
    Context mContext;
    List<User> mData;
    int mResource;

    public ContactsListViewAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
        this.mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.user_name = (TextView) convertView.findViewById(R.id.nameInContact);
            holder.user_avatar = (CircleImageView) convertView.findViewById(R.id.avatarInContact);
            holder.container = (View) convertView.findViewById(R.id.containerLayout);
            convertView.setTag(holder);
        }

        User contact_user= mData.get(position);
        holder = (ViewHolder) convertView.getTag();

        Log.d("demo","contact user is "+contact_user.getFirstname());


        holder.user_name.setText(contact_user.getFirstname()+" "+contact_user.getLastname());
        if(contact_user.getPhotoUrl().equals("") || contact_user.getPhotoUrl() == null){

        }
        else{
            
        }
        Picasso.with(mContext).load(contact_user.getPhotoUrl())
                .into(holder.user_avatar);

        convertView.setBackgroundColor(Color.GRAY);
        return convertView;
    }

    static class ViewHolder{
        private TextView user_name;
        private CircleImageView user_avatar;
        private View container;

    }
}
