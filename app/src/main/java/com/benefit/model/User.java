package com.benefit.model;

import android.location.Location;

/**
 * Model POJO for user
 */
public class User {

    private String UID;
    private String firstName;
    private String lastName;
    private String address;
    private Location location;
    private double rating;

    public User(){}

    public User(String UID){
        this.UID = UID;
    }

    public String getUID() {
        return UID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public Location getLocation() {
        return location;
    }

    public double getRating() {
        return rating;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
}
