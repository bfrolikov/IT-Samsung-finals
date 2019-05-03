package com.example.bfrol.it_samsung_finals;

public class Message {
    private String sender;
    private String receiver;
    private String text;
    private String time;
    public Message() {}

    public Message(String sender, String receiver, String text, String time) {
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

    public String getTime() {
        return time;
    }
}
