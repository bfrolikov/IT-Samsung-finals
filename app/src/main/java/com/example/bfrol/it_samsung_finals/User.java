package com.example.bfrol.it_samsung_finals;

public class User {
    //this is for convenience when managing users in the database
    private String firstName;
    private String lastName;
    private String socialMediaLink;
    private String country;
    private String city;
    private String demands;
    private String profileImageUrl;
    private int rating;
    public User() {}

    public User(String firstName, String lastName, String socialMediaLink, String country, String city, String demands, String profileImageUrl,int rating) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.socialMediaLink = socialMediaLink;
        this.country = country;
        this.city = city;
        this.demands = demands;
        this.profileImageUrl = profileImageUrl;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}