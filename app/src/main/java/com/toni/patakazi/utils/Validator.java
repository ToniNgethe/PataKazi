package com.toni.patakazi.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

/**
 * Created by toni on 12/22/17.
 */

public class Validator {
    public static boolean isEmalValid(String email){
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
