package com.toni.patakazi.model.requests;

/**
 * Created by toni on 12/22/17.
 */

public class LocationRequest {

    private String email;
    private double latitude;
    private String location;
    private double longitude;


    public LocationRequest(String email, double latitude, String location, double longitude) {
        this.email = email;
        this.latitude = latitude;
        this.location = location;
        this.longitude = longitude;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }
}
