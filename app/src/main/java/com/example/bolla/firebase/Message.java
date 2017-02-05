package com.example.bolla.firebase;

/**
 * Created by bolla on 11/19/2016.
 */
public class Message {
    String id,message, imageUrl,createdAt, sender_uid,receiver_uid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    public String getReceiver_uid() {
        return receiver_uid;
    }

    public void setReceiver_uid(String receiver_uid) {
        this.receiver_uid = receiver_uid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Message)) return false;
        Message o = (Message) obj;

        return o.id.equals(this.id) && o.message.equals(this.message) && o.imageUrl.equals(this.imageUrl)
                && o.createdAt.equals(this.createdAt) && o.sender_uid.equals(this.sender_uid) && o.receiver_uid.equals(this.receiver_uid);
    }
}
