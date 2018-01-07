package com.toni.patakazi.Api;

/**
 * Created by toni on 8/24/17.
 */

public class APIError {
    private int statusCode;
    private String message;

    public APIError() {
    }

    public int status() {
        return statusCode;
    }

    public String message() {
        return message;
    }
}
