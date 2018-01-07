package com.toni.patakazi.ui.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.toni.patakazi.Api.ApiClient;
import com.toni.patakazi.R;
import com.toni.patakazi.model.requests.PostJobRequest;
import com.toni.patakazi.model.responses.LocationResponse;
import com.toni.patakazi.utils.Global;
import com.toni.patakazi.utils.GpsTracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.toni.patakazi.utils.PrefManager;
import com.toni.patakazi.utils.Utills;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostJobActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int GALLERY_REQUEST = 1;
    private static final int MY_PERMISSIONS_FINE_LOCATION = 102;
    private static final int MY_PERMISSIONS_COURSE_LOCATION = 103;

    private EditText title, desc, location, charges, workers;
    private Button locateMe;
    private CardView postJob;
    private ImageButton imageButton;

    private DatabaseReference mJobs;
    private StorageReference mJobPics;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Uri imageUri = null;
    private Toolbar toolbar;

    private ProgressDialog progressDialog;

    private Handler mHandler;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private double longitude;
    private double latitude;

    private String city, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        setContentView(R.layout.activity_post_job);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        setUpViews();
        getJobPicture();
        getLocation();
        submitJob();


    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void getLocation() {

        locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
//
//                        // create class object
                        GpsTracker gps = new GpsTracker(PostJobActivity.this);

//                        // check if GPS enabled
                        if (gps.canGetLocation()) {
//
//                            double latitude = gps.getLatitude();
//                            double longitude = gps.getLongitude();
//
//                            // \n is for new line
//                            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                            displayLocation();

                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(PostJobActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                city = addresses.get(0).getLocality();
                                location.setText(city + "," + address);
                                Utills.writeLog(PostJobActivity.class, "addrss:" + address + "  lat:" + String.valueOf(latitude) + " lang:" + String.valueOf(longitude));
//                        String state = addresses.get(0).getAdminArea();
//                        String country = addresses.get(0).getCountryName();
//                        String postalCode = addresses.get(0).getPostalCode();
//                        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {
                            // can't get location
                            // GPS or Network is not enabled
                            // Ask user to enable GPS/network in settings
                            gps.showSettingsAlert();
                        }


                    }

                };

                if (runnable != null) {

                    mHandler.post(runnable);

                }

            }

        });
    }

    private void submitJob() {

        postJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (TextUtils.isEmpty(title.getText())) {
                    title.setError("Job title is required");
                    return;
                }

                if (TextUtils.isEmpty(desc.getText())) {
                    desc.setError("Job description is required");
                    return;
                }

                if (TextUtils.isEmpty(location.getText())) {
                    location.setError("Press locate me to get your location");
                    return;
                }
                if (TextUtils.isEmpty(charges.getText())) {
                    charges.setError("Charges required");
                    return;
                }

                if (TextUtils.isEmpty(workers.getText())) {
                    workers.setError("No. of worker(s) required");
                    return;
                }


                if (imageUri != null) {


                    if (!TextUtils.isEmpty(title.getText().toString()) && !TextUtils.isEmpty(desc.getText().toString()) && !TextUtils.isEmpty(workers.getText().toString())
                            && !TextUtils.isEmpty(location.getText().toString()) && !TextUtils.isEmpty(charges.getText().toString())) {

                        // display  progress
                        final SweetAlertDialog progress = Utills.showProgress(PostJobActivity.this, "Posting job..");
                        progress.show();

                        // initialize storage
                        mJobPics = FirebaseStorage.getInstance().getReference().child("Job_pics");

                        mJobPics.child(imageUri.getLastPathSegment()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                PostJobRequest postJobRequest = new PostJobRequest();
                                postJobRequest.setStatus(0);
                                postJobRequest.setWorkers(Integer.parseInt(workers.getText().toString()));
                                postJobRequest.setTitle(title.getText().toString());
                                postJobRequest.setDescription(desc.getText().toString());
                                postJobRequest.setCharges(Integer.parseInt(charges.getText().toString()));
                                postJobRequest.setLocation(location.getText().toString());
                                postJobRequest.setCity(city);
                                postJobRequest.setLat(latitude);
                                postJobRequest.setLang(longitude);
                                postJobRequest.setImageUrl(taskSnapshot.getDownloadUrl().toString());
                                postJobRequest.setUserEmail(PrefManager.userEmail());

                                // post job
                                Call<LocationResponse> call = ApiClient.apiInterface().postJob(postJobRequest);
                                call.enqueue(new Callback<LocationResponse>() {
                                    @Override
                                    public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {

                                        progress.dismissWithAnimation();

                                        if (response.code() == 200) {

                                            if (response.body().getStatus().equals("00")) {

                                                SweetAlertDialog success = Utills.successWithButton(PostJobActivity.this, response.body().getMsg());
                                                success.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {

                                                        sweetAlertDialog.dismissWithAnimation();

                                                        // start activity...
                                                        startActivity(new Intent(PostJobActivity.this, MainPanel.class));
                                                        finish();

                                                    }
                                                });
                                                success.show();

                                            } else {

                                                // error in data
                                                Utills.showErrorToast(response.body().getMsg());

                                            }

                                        } else {

                                            Utills.showErrorToast(getString(R.string.global_error));

                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<LocationResponse> call, Throwable t) {

                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog.dismiss();
                                Global.showDialog("Firebase Error", e.getMessage(), PostJobActivity.this);

                            }
                        });


                    } else {

                        progressDialog.dismiss();
                        Global.showDialog("Input Fields", "Field(s) cannot be empty", PostJobActivity.this);
                    }

                } else {

                    progressDialog.dismiss();

                    Global.showDialog("Job image", "Please select an image of the job..", PostJobActivity.this);
                }

            }
        });

    }

    private String getDate() {

        Date date = new Date();
        //Date newDate = new Date(date.getTime() + (604800000L * 2) + (24 * 60 * 60));

        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
        String stringdate = dt.format(date);

        return stringdate;
    }

    private void getJobPicture() {

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, GALLERY_REQUEST);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();

                imageButton.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Log.d("Croping", error.getMessage());

            }

        }

    }


    private void setUpViews() {

        mHandler = new Handler();

        title = (EditText) findViewById(R.id.etJobTitle);
        desc = (EditText) findViewById(R.id.etDesc);
        location = (EditText) findViewById(R.id.etLoc);
        charges = (EditText) findViewById(R.id.etCharges);
        workers = (EditText) findViewById(R.id.workers);

        locateMe = (Button) findViewById(R.id.btnLocateMe);
        postJob = (CardView) findViewById(R.id.btnPostJob);

        imageButton = (ImageButton) findViewById(R.id.jobImage);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Post Job");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);


    }


    @Override
    protected void onStart() {
        super.onStart();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();


            } else {

                Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_LONG).show();
            }

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSIONS_FINE_LOCATION);
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, MY_PERMISSIONS_COURSE_LOCATION);
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLastLocation = LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient);

                        if (mLastLocation != null) {
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();


                        } else {

                            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_LONG).show();
                        }
                    }

                } else {

                    Toast.makeText(PostJobActivity.this, "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();

                }

                break;

            case MY_PERMISSIONS_COURSE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mLastLocation = LocationServices.FusedLocationApi
                                .getLastLocation(mGoogleApiClient);

                        if (mLastLocation != null) {

                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();


                        } else {

                            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_LONG).show();
                        }
                    }

                } else {

                    Toast.makeText(PostJobActivity.this, "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();

                }

                break;

        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            // startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stopLocationUpdates();
    }
}
