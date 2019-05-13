package com.example.bfrol.it_samsung_finals;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;

public class Message {
    private String sender;
    private String receiver;
    private String text;
    private Date time;
    private String routeName;
    private ArrayList<GeoPoint> routePoints;
    public Message() {}


    public Message(String sender, String receiver, String text, Date time, String routeName, ArrayList<GeoPoint> routePoints) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.time = time;
        this.routeName = routeName;
        this.routePoints = routePoints;
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

    public ArrayList<GeoPoint> getRoutePoints() { return routePoints; }

    public String getRouteName() { return routeName; }
}
