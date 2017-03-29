package com.example.toni.patakazi.model;

import android.widget.TextView;

/**
 * Created by toni on 2/8/17.
 */

public class Jobs {

    private String image,title,postKey,location,uid,desc;
    private long  charges,status,workers;

    public Jobs(){

    }

    public Jobs(String image, String postKey, String title, String location, String uid, String desc, long charges, long workers, long status) {
        this.image = image;
        this.postKey = postKey;
        this.title = title;
        this.location = location;
        this.uid = uid;
        this.desc = desc;
        this.charges = charges;
        this.workers = workers;
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getWorkers() {
        return workers;
    }

    public void setWorkers(long workers) {
        this.workers = workers;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCharges() {
        return charges;
    }

    public void setCharges(long charges) {
        this.charges = charges;
    }
}
