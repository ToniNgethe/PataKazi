package com.example.toni.patakazi.Fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.toni.patakazi.Dialogs.ChangeEmailAddress;
import com.example.toni.patakazi.Dialogs.ChangePhoneNumber;
import com.example.toni.patakazi.Dialogs.ChangeUserNameDialog;
import com.example.toni.patakazi.Helpers.Global;
import com.example.toni.patakazi.Helpers.GpsTracker;
import com.example.toni.patakazi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by toni on 2/8/17.
 */

public class AccountFragment extends Fragment {

    final String TAG = AccountFragment.class.getSimpleName();

    private static final int GALLARY_INTENT = 200;
    private View mView;
    private TextView phoneNumber, email, location, userName, numberOfJobs, numberOfSkills;
    private ImageView profile;
    private ImageButton changeImage, changeNumber, changeEmail, changeLocation, changeUsername;

    private DatabaseReference mUsers, mJobs, mSkills;
    private FirebaseAuth mAuth;

    private Query mSkillNumber;
    private Query mJObsCount;

    private Handler mHandler;
    private ProgressDialog progressBar;
    private FloatingActionButton floatingActionButton;

    private Uri imageUri = null;
    private ProgressBar mProgressBar;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_user_profile, container, false);

        //INITIATE VIEWS
        setUpViews(mView);
        setupFirebase();
        changeProfilePic();
        countJobs();
        changeEmail();
        chnageUserName();
        getProfilePic();
        setLocation();
        setPhoneNumber();

        return mView;
    }

    private void chnageUserName() {

        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChangeUserNameDialog changeUserNameDialog = new ChangeUserNameDialog(getActivity());
                changeUserNameDialog.setCanceledOnTouchOutside(false);
                changeUserNameDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changeUserNameDialog.show();

            }
        });

    }

    private void changeEmail() {

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChangeEmailAddress changeEmailAddress = new ChangeEmailAddress(getActivity());
                changeEmailAddress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changeEmailAddress.setCancelable(false);
                changeEmailAddress.show();

            }
        });
    }

    private void countJobs() {


        mJObsCount = mJobs.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());

        mJObsCount.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                numberOfJobs.setText(String.valueOf(dataSnapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSkillNumber = mSkills.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());
        mSkillNumber.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                numberOfSkills.setText(String.valueOf(dataSnapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void changeProfilePic() {

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLARY_INTENT);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        try {
            // When an Image is picked
            if (requestCode == GALLARY_INTENT && resultCode == Activity.RESULT_OK
                    && null != data) {
                Uri selectedImage = data.getData();

                CropImage.activity(selectedImage)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(getContext(), this);
            }
            // when image is cropped
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Log.d("APP_DEBUG", result.toString());
                if (resultCode == Activity.RESULT_OK) {

                    imageUri = result.getUri();

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                    profile.setImageBitmap(bitmap);

                    uploadImage();

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            } else {
                Toast.makeText(getActivity(), "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Something went wrong" + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }

    }

    private void uploadImage() {

        if (imageUri != null) {
            final ProgressDialog mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Updating image...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            // Create a storage reference from our app
            final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            // Create a reference to the file to delete
            StorageReference desertRef = storageRef.child("Profile_images/" + mAuth.getCurrentUser().getUid());

            // Delete the file
            desertRef.delete().addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {

                    mProgressDialog.setMessage("Deleting old image...");

                    StorageReference newImage = storageRef.child("Profile_images");
                    newImage.child(mAuth.getCurrentUser().getUid()).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            mProgressDialog.setMessage("Setting new image...");

                            DatabaseReference mine = mUsers.child(mAuth.getCurrentUser().getUid());

                            mine.child("image").setValue(taskSnapshot.getDownloadUrl().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    mProgressDialog.setMessage("Getting new Image...");


                                    if (task.isSuccessful()) {

                                        mProgressDialog.dismiss();

                                    } else {

                                        mProgressDialog.dismiss();
                                        Global.showDialog("Error", task.getException().getMessage(), getActivity());

                                    }

                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Global.showDialog("Error", e.getMessage(), getActivity());
                        }
                    });

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    mProgressDialog.dismiss();
                    Global.showDialog("Error", exception.getMessage(), getActivity());
                }
            });


        }

    }

    private void setPhoneNumber() {

        changeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChangePhoneNumber changePhoneNumber = new ChangePhoneNumber(getActivity());
                changePhoneNumber.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                changePhoneNumber.setCanceledOnTouchOutside(false);
                changePhoneNumber.setCancelable(false);
                changePhoneNumber.show();

            }
        });
    }

    private void setLocation() {

        changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        progressBar.setMessage("Getting your location..");
                        progressBar.setCanceledOnTouchOutside(false);
                        progressBar.show();

                        // create class object
                        GpsTracker gps = new GpsTracker(getActivity());

                        // check if GPS enabled
                        if (gps.canGetLocation()) {

                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();


                            Geocoder geocoder;
                            List<Address> addresses;
                            geocoder = new Geocoder(getActivity(), Locale.getDefault());

                            try {
                                addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                                String city = addresses.get(0).getLocality();
                                location.setText(city + "," + address);

                                progressBar.setMessage("Location obtained....");

                                mUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        progressBar.setMessage("Updating your location...");

                                        mUsers.child(mAuth.getCurrentUser().getUid()).child("location").setValue(location.getText()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    progressBar.dismiss();

                                                } else {

                                                    progressBar.dismiss();
                                                    Global.showDialog("Error", task.getException().getMessage(), getActivity());
                                                }

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                        progressBar.dismiss();
                                        Global.showDialog("Error", databaseError.getMessage(), getActivity());

                                    }
                                });


                            } catch (IOException e) {
                                e.printStackTrace();

                                progressBar.dismiss();
                                Global.showDialog("Gps error", e.getMessage() + ". Try later", getActivity());
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

    private void getProfilePic() {

        if (mAuth.getCurrentUser() != null) {

            final String userID = mAuth.getCurrentUser().getUid();

            mUsers.child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                            //getting profile image....
                            // Loading profile image
                        try {

                            Glide.with(getActivity()).load(dataSnapshot.child("image").getValue().toString())
                                    .crossFade()
                                    .thumbnail(0.5f)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .error(R.mipmap.error_network)
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                                            mProgressBar.setVisibility(View.GONE);
                                            profile.setVisibility(View.VISIBLE);

                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                                            mProgressBar.setVisibility(View.GONE);
                                            profile.setVisibility(View.VISIBLE);

                                            return false;
                                        }
                                    })
                                    .into(profile);

                            //username
                            userName.setText(dataSnapshot.child("user").getValue().toString());

                            //email
                            email.setText(mAuth.getCurrentUser().getEmail());

                            //location
                            location.setText(dataSnapshot.child("location").getValue().toString());

                            //phone number
                            phoneNumber.setText(dataSnapshot.child("number").getValue().toString());

                        }catch (NullPointerException e){
                            e.getMessage();
                        }

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    private void setupFirebase() {

        mUsers = FirebaseDatabase.getInstance().getReference("Users");
        mUsers.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);

        mSkills = FirebaseDatabase.getInstance().getReference().child("Skills");
        mSkills.keepSynced(true);

    }

    private void setUpViews(View mView) {

        mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_userprofile);

        phoneNumber = (TextView) mView.findViewById(R.id.userPhoneNumber);
        email = (TextView) mView.findViewById(R.id.userEmail);
        location = (TextView) mView.findViewById(R.id.userLocation);
        userName = (TextView) mView.findViewById(R.id.accountName);

        profile = (ImageView) mView.findViewById(R.id.accountProfile44);
        profile.setVisibility(View.INVISIBLE);

        changeEmail = (ImageButton) mView.findViewById(R.id.changeEmail);
        changeLocation = (ImageButton) mView.findViewById(R.id.changeUserLocation);
        changeUsername = (ImageButton) mView.findViewById(R.id.changeName);
        changeNumber = (ImageButton) mView.findViewById(R.id.changeNumber);
        mHandler = new Handler();

        floatingActionButton = (FloatingActionButton) mView.findViewById(R.id.chooseNewImage);

        progressBar = new ProgressDialog(getActivity());

        numberOfJobs = (TextView) mView.findViewById(R.id.numberOfJobs);
        numberOfSkills = (TextView) mView.findViewById(R.id.numberOfSkills);

    }


}
