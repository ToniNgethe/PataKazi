package com.example.toni.patakazi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.toni.patakazi.Helpers.Global;
import com.example.toni.patakazi.Helpers.GpsTracker;
import com.google.android.gms.location.LocationListener;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class PostJobActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;

    private EditText title, desc, location, charges,workers;
    private Button locateMe, postJob;
    private ImageButton imageButton;

    private DatabaseReference mJobs;
    private StorageReference mJobPics;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Uri imageUri = null;
    private Toolbar toolbar;

    private ProgressDialog progressDialog;

    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        setContentView(R.layout.activity_post_job);

        setUpViews();
        initiateFireBase();
        getJobPicture();
        getLocation();
        submitJob();

    }

    private void getLocation() {

        locateMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                // create class object
               GpsTracker gps = new GpsTracker(PostJobActivity.this);

                // check if GPS enabled
                if(gps.canGetLocation()){

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    // \n is for new line
                   // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(PostJobActivity.this, Locale.getDefault());

                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                      String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        String city = addresses.get(0).getLocality();
                        location.setText(city+","+address);

//                        String state = addresses.get(0).getAdminArea();
//                        String country = addresses.get(0).getCountryName();
//                        String postalCode = addresses.get(0).getPostalCode();
//                        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                         }

                    }

                };

                if (runnable != null){

                    mHandler.post(runnable);

                }

            }

        });
    }

    private void submitJob() {

        postJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Posting Job....");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                if (imageUri != null) {

                    if ( !TextUtils.isEmpty(title.getText().toString()) && !TextUtils.isEmpty(desc.getText().toString()) && !TextUtils.isEmpty(workers.getText().toString())
                                && !TextUtils.isEmpty(location.getText().toString()) && !TextUtils.isEmpty(charges.getText().toString())) {

                        final DatabaseReference jobs = mJobs.push();

                        mJobPics.child(jobs.getKey()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                mJobs.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        jobs.child("status").setValue(0);
                                        jobs.child("workers").setValue(Integer.parseInt(workers.getText().toString()));
                                        jobs.child("title").setValue(title.getText().toString());
                                        jobs.child("desc").setValue(desc.getText().toString());
                                        jobs.child("charges").setValue(Integer.parseInt(charges.getText().toString()));
                                        jobs.child("location").setValue(location.getText().toString());
                                        jobs.child("image").setValue(taskSnapshot.getDownloadUrl().toString());
                                        jobs.child("uid").setValue(mAuth.getCurrentUser().getUid().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(PostJobActivity.this, MainPanel.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                                }else {

                                                    progressDialog.dismiss();
                                                    //Global.showDialog("Firebase Error",task.getException().getMessage(),PostJobActivity.this);
                                                }
                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                        progressDialog.dismiss();
                                        Global.showDialog("Firebase Error",databaseError.getMessage(),PostJobActivity.this);
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                progressDialog.dismiss();
                                Global.showDialog("Firebase Error",e.getMessage(),PostJobActivity.this);

                            }
                        });


                    } else {

                        progressDialog.dismiss();
                        Global.showDialog("Input Fields","Field(s) cannot be empty",PostJobActivity.this);
                    }

                } else {

                    progressDialog.dismiss();

                    Global.showDialog("Job image","Please select an image of the job..",PostJobActivity.this);
                }

            }
        });

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

    private void initiateFireBase() {

        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);

        mJobPics = FirebaseStorage.getInstance().getReference().child("Job_pictures");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(PostJobActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

            }
        };

    }

    private void setUpViews() {

        mHandler = new Handler();

        title = (EditText) findViewById(R.id.etJobTitle);
        desc = (EditText) findViewById(R.id.etDesc);
        location = (EditText) findViewById(R.id.etLoc);
        charges = (EditText) findViewById(R.id.etCharges);
        workers = (EditText) findViewById(R.id.workers);

        locateMe = (Button) findViewById(R.id.btnLocateMe);
        postJob = (Button) findViewById(R.id.btnPostJob);

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
        mAuth.addAuthStateListener(mAuthListener);
    }

}
