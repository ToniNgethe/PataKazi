package com.example.toni.patakazi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SetupActivity extends AppCompatActivity {

    private static final int GALLERY_INTENT = 1;
    private ImageView profile;
    private EditText username;
    private Button submit;

    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private StorageReference mProfiles;
    private Uri imageUri = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setUpViews();
        initilizeFirebase();
        getImage();
        submitingUser();

    }


    private void setUpViews() {

        profile = (ImageView) findViewById(R.id.setUpProfilePic);
        username = (EditText) findViewById(R.id.setUpUsername);
        submit = (Button) findViewById(R.id.setupSubmitBtn);

        progressDialog = new ProgressDialog(this);

    }

    private void initilizeFirebase() {

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        mProfiles= FirebaseStorage.getInstance().getReference().child("Profile_images");

    }

    private void getImage() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,GALLERY_INTENT);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK){

            Uri uri = data.getData();

            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);


        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();

                profile.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("Cropiiiiiii",error.getMessage());
                // Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void submitingUser() {

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Adding user..");
                progressDialog.show();

                StorageReference cu = mProfiles.child(mAuth.getCurrentUser().getUid());

                cu.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        DatabaseReference cu = mUsers.child(mAuth.getCurrentUser().getUid());
                        cu.child("user").setValue(username.getText().toString().trim());
                        cu.child("image").setValue(taskSnapshot.getDownloadUrl().toString());
                        cu.child("email").setValue(mAuth.getCurrentUser().getDisplayName());
                        cu.child("uid").setValue(mAuth.getCurrentUser().getUid());

                        progressDialog.dismiss();

                        startActivity(new Intent(SetupActivity.this,MainPanel.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        });

    }

}
