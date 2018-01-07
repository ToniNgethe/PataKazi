package com.toni.patakazi.model.responses;

import java.util.List;

/**
 * Created by toni on 12/22/17.
 */

public class JobsResponse {
    private List<Data> data;
    private String status;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class User {
        private int id;
        private String fName;
        private String lName;
        private String username;
        private String email;
        private String image_url;
        private String phone_number;
        private int valid_email;
        private int valid_mobile;
        private String createdOn;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getFName() {
            return fName;
        }

        public void setFName(String fName) {
            this.fName = fName;
        }

        public String getLName() {
            return lName;
        }

        public void setLName(String lName) {
            this.lName = lName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public int getValid_email() {
            return valid_email;
        }

        public void setValid_email(int valid_email) {
            this.valid_email = valid_email;
        }

        public int getValid_mobile() {
            return valid_mobile;
        }

        public void setValid_mobile(int valid_mobile) {
            this.valid_mobile = valid_mobile;
        }

        public String getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(String createdOn) {
            this.createdOn = createdOn;
        }
    }

    public static class Data {
        private int id;
        private String title;
        private String description;
        private String location;
        private double latittude;
        private double longitude;
        private double charges;
        private int workers;
        private String image_url;
        private String city;
        private int status;
        private User user;
        private String createdOn;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public double getLatittude() {
            return latittude;
        }

        public void setLatittude(int latittude) {
            this.latittude = latittude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(int longitude) {
            this.longitude = longitude;
        }

        public double getCharges() {
            return charges;
        }

        public void setCharges(double charges) {
            this.charges = charges;
        }

        public int getWorkers() {
            return workers;
        }

        public void setWorkers(int workers) {
            this.workers = workers;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getCreatedOn() {
            return createdOn;
        }

        public void setCreatedOn(String createdOn) {
            this.createdOn = createdOn;
        }
    }
}
