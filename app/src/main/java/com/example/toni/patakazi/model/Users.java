package com.example.toni.patakazi.model;

import android.media.Image;

/**
 * Created by toni on 2/10/17.
 */

public class Users {
    private String image,user,email,uid,number;

    public Users(){

    }

    public Users(String user, String image, String email, String uid,String number) {
        this.user = user;
        this.image = image;
        this.email = email;
        this.uid = uid;
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
