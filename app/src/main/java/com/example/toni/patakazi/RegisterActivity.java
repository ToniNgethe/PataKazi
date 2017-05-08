package com.example.toni.patakazi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.toni.patakazi.Dialogs.CustomProgressDialog;
import com.example.toni.patakazi.Helpers.Global;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class RegisterActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    private ImageView profileImage;
    private EditText userName, email,pass;
    private Button backToLogin, regUser;

    private DatabaseReference mUsers;
    private StorageReference mProfile;
    private FirebaseAuth mAuth;

    private Uri imageUri = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setViews();
        setFireBase();
        getImage();
        register();
    }

    private void getImage() {

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery,GALLERY_REQUEST);

            }
        });

        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GALLERY_REQUEST && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUri = result.getUri();

                profileImage.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d("Cropiiiiiii",error.getMessage());
               // Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void register() {

        regUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userEmail = email.getText().toString().trim();
                final String userPass = pass.getText().toString().trim();
                final String user = userName.getText().toString().trim();

                if (!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass) && !TextUtils.isEmpty(user)){


                    final CustomProgressDialog customProgressDialog = new CustomProgressDialog(RegisterActivity.this,"Adding user....");
                    customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    customProgressDialog.setCanceledOnTouchOutside(false);
                    customProgressDialog.setCancelable(false);
                    customProgressDialog.show();

                    mAuth.createUserWithEmailAndPassword(userEmail,userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                final String userID = mAuth.getCurrentUser().getUid();

                                StorageReference filePath = mProfile.child(userID);

                                filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        final DatabaseReference currentUser = mUsers.child(userID);

                                        currentUser.child("user").setValue(user);
                                        currentUser.child("image").setValue(taskSnapshot.getDownloadUrl().toString());
                                        currentUser.child("email").setValue(mAuth.getCurrentUser().getEmail().toString());
                                        currentUser.child("uid").setValue(mAuth.getCurrentUser().getUid().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){

                                                    customProgressDialog.dismiss();
                                                    startActivity(new Intent(RegisterActivity.this,GetLocationActivity.class));
                                                    finish();

                                                }else {

                                                    customProgressDialog.dismiss();
                                                    Global.showDialog("Register error",task.getException().getMessage(),RegisterActivity.this);

                                                }

                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        customProgressDialog.dismiss();
                                        Global.showDialog("Image saving error",e.getMessage(),RegisterActivity.this);

                                    }
                                });

                            }else {

                                customProgressDialog.dismiss();
                                Global.showDialog("Register error",task.getException().getMessage(),RegisterActivity.this);

                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    });

                }else {

                   // progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(),"Field(s) empty",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void setFireBase() {

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mProfile = FirebaseStorage.getInstance().getReference().child("Profile_images");
        mAuth = FirebaseAuth.getInstance();

    }

    private void setViews() {

        profileImage = (ImageView) findViewById(R.id.iv_getlocation);
        userName = (EditText) findViewById(R.id.regUsername);
        email = (EditText) findViewById(R.id.regEmail);
        pass = (EditText)findViewById(R.id.regPassword);
        backToLogin = (Button) findViewById(R.id.backToLogin);
        regUser = (Button) findViewById(R.id.regButton);

        progressDialog = new ProgressDialog(this);

    }

}
