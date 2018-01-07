package com.toni.patakazi.ui.intro_ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.toni.patakazi.Api.BackGroundCalls;
import com.toni.patakazi.R;
import com.toni.patakazi.ui.Activities.MainPanel;
import com.toni.patakazi.ui.Main2Activity;
import com.toni.patakazi.utils.Global;
import com.toni.patakazi.utils.GpsTracker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.toni.patakazi.utils.PrefManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetLocationActivity extends AppCompatActivity {

    private static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
    private static final String TAG = GetLocationActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 10;
    public static final String MY_PREFS_NAME = "MY_LOCATIONS";
    private TextView loading;

    private ProgressBar progressBar;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        //views

        loading = (TextView) findViewById(R.id.tv_getlocation_loading);
        progressBar = (ProgressBar) findViewById(R.id.progress_getlocation);

        if (Global.isConnected(this)) {



            if (PrefManager.isLoggedIn()) {
                checkAndroidVersion();
            } else {

                // redirect to login.....
                startActivity(new Intent(GetLocationActivity.this, LoginActivity.class));
                finish();
            }


        } else {
            progressBar.setVisibility(View.GONE);
            loading.setText("No Connection");
        }
    }

    private void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        } else {
            displayLocation();
        }

    }

    private void checkPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) + ContextCompat
                .checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.ACCESS_COARSE_LOCATION)) {


            } else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(
                            new String[]{Manifest.permission
                                    .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSIONS_MULTIPLE_REQUEST);
                }
            }

        } else {
            displayLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:

                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraPermission && readExternalFile) {
                        // write your logic here
                        Toast.makeText(GetLocationActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                        displayLocation();
                    } else {

                        Toast.makeText(this, "Please Grant permission to get Your location", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.

        } else {

            GpsTracker gps = new GpsTracker(GetLocationActivity.this);

            if (gps.canGetLocation()) {

                // displayLocation();
                if (Geocoder.isPresent()) {
                    Geocoder geocoder;
                    List<Address> addresses;

                    // displayLocation();

                    geocoder = new Geocoder(this, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        // address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                        String loc = addresses.get(0).getLocality();
                        loading.setText("Current location: " + loc);
                        // Log.d(TAG, String.valueOf(longitude) + " ," + String.valueOf(latitude));
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "CITY :" + loc);

                        saveToPreferances(loc, gps.getLatitude(), gps.getLongitude());

                    } catch (IOException e) {

                        Log.d(TAG, e.getMessage());
                        loading.setText("Location Error: Try restarting your phone");
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                        loading.setText("Current Location : Unknown location, try restarting app");

                    }

                } else {
                    progressBar.setVisibility(View.GONE);
                    loading.setText("Current Location : Unknown location, try restarting app");
                }
            } else {
                gps.showSettingsAlert();
            }
        }
    }

    private void saveToPreferances(String loc, double latitude, double longitude) {

        // save location to backend..
        BackGroundCalls.storeUserLocation(new com.toni.patakazi.model.requests.LocationRequest(
                PrefManager.userEmail(), latitude, loc, longitude));

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("location", loc);
        editor.apply();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(GetLocationActivity.this, MainPanel.class));
                finish();
            }
        }, 2000);

    }


}
