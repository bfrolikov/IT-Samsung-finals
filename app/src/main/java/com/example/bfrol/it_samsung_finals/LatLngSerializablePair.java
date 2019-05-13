package com.example.bfrol.it_samsung_finals;

import java.io.Serializable;

public class LatLngSerializablePair implements Serializable {
    private double latitude;
    private double longitude;

    public LatLngSerializablePair(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
