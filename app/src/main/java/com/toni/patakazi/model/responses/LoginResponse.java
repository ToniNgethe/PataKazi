package com.toni.patakazi.model.responses;

/**
 * Created by toni on 12/22/17.
 */

public class LoginResponse {
    private String msg;
    private Data data;
    private String status;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class Data {
        private int id;
        private String fName;
        private String lName;
        private String username;
        private String email;
        private String phone_number;
        private int valid_email;
        private String image_url;
        private int valid_mobile;
        private String createdOn;


        public String getlName() {
            return lName;
        }

        public void setlName(String lName) {
            this.lName = lName;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

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
}
