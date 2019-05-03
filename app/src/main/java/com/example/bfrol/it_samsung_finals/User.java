package com.example.bfrol.it_samsung_finals;

import java.io.Serializable;

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
    public User() {}

    public User(String firstName, String lastName, String socialMediaLink, String country, String city, String demands, String uID,int rating) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.socialMediaLink = socialMediaLink;
        this.country = country;
        this.city = city;
        this.demands = demands;
        this.uID = uID;
        this.rating = rating;
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

    public void setRating(int rating) {
        this.rating = rating;
    }
}
