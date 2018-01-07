package com.toni.patakazi.model;

/**
 * Created by toni on 2/12/17.
 */

public class Skills {

    private String image,location,title,desc;
    private long charges;

    public Skills(String image, String location, String title, long charges,String desc) {
        this.image = image;
        this.location = location;
        this.title = title;
        this.charges = charges;
        this.desc = desc;
    }

    public Skills() {

    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getCharges() {

        return charges;
    }

    public void setCharges(long charges) {
        this.charges = charges;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
