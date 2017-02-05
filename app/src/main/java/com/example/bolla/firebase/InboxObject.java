package com.example.bolla.firebase;

import java.io.Serializable;

/**
 * Created by bolla on 11/19/2016.
 */
public class InboxObject implements Serializable {
    String sender_uid;
    Message lastMessage;
    Boolean lastMessageRead;
    User receiver_user;


    public String getSender_uid() {
        return sender_uid;
    }

    public void setSender_uid(String sender_uid) {
        this.sender_uid = sender_uid;
    }

    public Boolean getLastMessageRead() {
        return lastMessageRead;
    }

    public void setLastMessageRead(Boolean lastMessageRead) {
        this.lastMessageRead = lastMessageRead;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public User getReceiver_user() {
        return receiver_user;
    }

    public void setReceiver_user(User receiver_user) {
        this.receiver_user = receiver_user;
    }
}
