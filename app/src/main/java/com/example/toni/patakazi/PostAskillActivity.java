package com.example.toni.patakazi;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.TextView;

import com.example.toni.patakazi.Dialogs.CustomProgressDialog;
import com.example.toni.patakazi.Helpers.Global;
import com.example.toni.patakazi.Helpers.GpsTracker;
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

public class PostAskillActivity extends AppCompatActivity {

    private static final int GALLERY_INTENT = 1 ;
    private Toolbar mToolbar;
    private EditText title,desc,location,charges;
    private Button locationBtn,submit;
    private ImageButton imageButton;

    private DatabaseReference mSkills;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private StorageReference mSkillsPics;
    private String city;

    private Handler mHandler;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        setContentView(R.layout.activity_post_askill);

        setUpViews();
        setUpFireBase();
        getLocation();
        getImage();
        submitSkill();
    }

    private void submitSkill() {

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(location.getText())) {

                    if (imageUri != null) {

                        if (!TextUtils.isEmpty(title.getText()) && !TextUtils.isEmpty(desc.getText()) && !TextUtils.isEmpty(charges.getText())) {

                            final CustomProgressDialog customProgressDialog = new CustomProgressDialog(PostAskillActivity.this,"Posting skill...");
                            customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            customProgressDialog.setCanceledOnTouchOutside(false);
                            customProgressDialog.show();

                            final DatabaseReference mdDatabaseReference = mSkills.push();

                            mSkillsPics.child(mdDatabaseReference.getKey()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                                    mdDatabaseReference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {


                                            mdDatabaseReference.child("title").setValue(title.getText().toString());
                                            mdDatabaseReference.child("desc").setValue(desc.getText().toString());
                                            mdDatabaseReference.child("location").setValue(location.getText().toString());
                                            mdDatabaseReference.child("city").setValue(city);
                                            mdDatabaseReference.child("charges").setValue(Integer.parseInt(charges.getText().toString()));
                                            mdDatabaseReference.child("image").setValue(taskSnapshot.getDownloadUrl().toString());
                                            mdDatabaseReference.child("uid").setValue(mAuth.getCurrentUser().getUid().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        customProgressDialog.dismiss();

                                                        startActivity(new Intent(PostAskillActivity.this,MainPanel.class));

                                                    }else {
                                                        customProgressDialog.dismiss();
                                                        Global.showDialog("Submission Error",task.getException().getMessage(),PostAskillActivity.this);
                                                    }

                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                            customProgressDialog.dismiss();
                                            Global.showDialog("Database Error",databaseError.getMessage(),PostAskillActivity.this);
                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Global.showDialog("Saving Image Error",e.getMessage(),PostAskillActivity.this);

                                }
                            });


                        }else {
                            Global.showDialog("Fields","Fields cannot be empty",PostAskillActivity.this);
                        }

                    }else {
                        Global.showDialog("Image","Attach an image to describe your skill",PostAskillActivity.this);
                    }

                }else {
                    Global.showDialog("Information","Press locate me button to get your location",PostAskillActivity.this);
                }
            }
        });

    }

    private void getImage() {

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,GALLERY_INTENT);

            }
        });

    }


    private void getLocation() {

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        // create class object
                        GpsTracker gps = new GpsTracker(PostAskillActivity.this);

                        // check if GPS enabled
                        if(gps.canGetLocation()){

                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();

                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(PostAskillActivity.this, Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                city = addresses.get(0).getLocality();
                                location.setText(city+","+address);


                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }else{
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

    private void setUpFireBase() {

        mSkills = FirebaseDatabase.getInstance().getReference().child("Skills");
        mAuth  = FirebaseAuth.getInstance();

        mSkillsPics = FirebaseStorage.getInstance().getReference().child("Skill_pics");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(PostAskillActivity.this,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

            }
        };

    }

    private void setUpViews() {

        mToolbar = (Toolbar) findViewById(R.id.skillToolBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (EditText) findViewById(R.id.skillJobTitle);
        desc = (EditText) findViewById(R.id.skillDescription);
        location = (EditText) findViewById(R.id.skillLocation);
        charges = (EditText) findViewById(R.id.skillChargeRate);

        locationBtn = (Button) findViewById(R.id.skilLocateMe);

        imageButton = (ImageButton) findViewById(R.id.skillPhoto);

        submit = (Button) findViewById(R.id.submitSkill);

        mHandler = new Handler();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode ==RESULT_OK){


            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();

                imageButton.setImageURI(imageUri);

            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){

                Exception error = result.getError();
                Log.d("Croping", error.getMessage());

            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

}
