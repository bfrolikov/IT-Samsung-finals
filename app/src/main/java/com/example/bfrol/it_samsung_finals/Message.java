package com.example.bfrol.it_samsung_finals;

import java.util.Date;

public class Message {
    private String sender;
    private String receiver;
    private String text;
    private Date time;
    public Message() {}

    public Message(String sender, String receiver, String text, Date time) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.time = time;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getText() {
        return text;
    }

    public Date getTime() {
        return time;
    }
}
