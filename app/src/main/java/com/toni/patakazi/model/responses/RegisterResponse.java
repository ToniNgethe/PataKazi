package com.toni.patakazi.model.responses;

/**
 * Created by toni on 1/4/18.
 */

public class RegisterResponse {
    private String message;
    private String status;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
