package com.example.bfrol.it_samsung_finals;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class User implements Serializable, Parcelable {
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

    protected User(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        socialMediaLink = in.readString();
        country = in.readString();
        city = in.readString();
        demands = in.readString();
        uID = in.readString();
        rating = in.readInt();
        Bundle bundle = in.readBundle(getClass().getClassLoader());
        ArrayList<String> keys = bundle.getStringArrayList("keys");
        ArrayList<ArrayList<LatLngSerializablePair>> serRoutes = (ArrayList<ArrayList<LatLngSerializablePair>>) bundle.getSerializable("arr");
        Map<String,ArrayList<GeoPoint>> routesTemp = new HashMap<>();
        for(int i=0;i<keys.size();i++)
        {
            ArrayList<GeoPoint> temp = new ArrayList<>();
            for(int j=0;j<serRoutes.get(i).size();j++)
            {
                LatLngSerializablePair pair = serRoutes.get(i).get(j);
                temp.add(new GeoPoint(pair.getLatitude(),pair.getLongitude()));
            }
            routesTemp.put(keys.get(i),temp);
        }
        routes = routesTemp;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(socialMediaLink);
        dest.writeString(country);
        dest.writeString(city);
        dest.writeString(demands);
        dest.writeString(uID);
        dest.writeInt(rating);
        ArrayList<String> keys = new ArrayList<>(routes.keySet());
        ArrayList<ArrayList<LatLngSerializablePair>> serRoutes = new ArrayList<>();
        for(int i=0;i<keys.size();i++)
        {
            ArrayList<LatLngSerializablePair> temp = new ArrayList<>();
            for(int j=0;j<routes.get(keys.get(i)).size();j++)
            {
                GeoPoint currPoint = routes.get(keys.get(i)).get(j);
                temp.add(new LatLngSerializablePair(currPoint.getLatitude(),currPoint.getLongitude()));
            }
            serRoutes.add(temp);
        }
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("keys",keys);
        bundle.putSerializable("arr",serRoutes);
        dest.writeBundle(bundle);
    }
}
