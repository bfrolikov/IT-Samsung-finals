package com.example.bfrol.it_samsung_finals;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class User implements Serializable {
    //this is for convenience when managing users in the database
    private String firstName;
    private String lastName;
    private String socialMediaLink;
    private String country;
    private String city;
    private String demands;
    private String uID;
    private int rating;
    private Map<String,ArrayList<GeoPoint>> routes;
    public User() {}

    public User(String firstName, String lastName, String socialMediaLink, String country, String city, String demands, String uID, int rating, Map<String, ArrayList<GeoPoint>> routes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.socialMediaLink = socialMediaLink;
        this.country = country;
        this.city = city;
        this.demands = demands;
        this.uID = uID;
        this.rating = rating;
        this.routes = routes;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSocialMediaLink() {
        return socialMediaLink;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getDemands() {
        return demands;
    }

    public String getuID() { return uID; }

    public int getRating() {
        return rating;
    }

    public Map<String, ArrayList<GeoPoint>> getRoutes() { return routes; }

}
