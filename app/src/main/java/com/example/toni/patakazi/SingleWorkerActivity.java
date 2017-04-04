package com.example.toni.patakazi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.toni.patakazi.Fragments.WorkersFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SingleWorkerActivity extends AppCompatActivity {

    private static final int PERMISSION_SEND_SMS = 100;
    private static final int PERMISSION_CALL = 101;
    private static final String TAG = SingleWorkerActivity.class.getSimpleName();
    private ImageView workerImage;
    private ProgressBar mProgressBar;
    private TextView name, charges, location;
    private TextView description;
    private Button contact;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference mSkills;
    private String skillKey = null;
    private String number;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_worker);

        //views
        views();
        //get key
        key();
        //Firebase
        firebase();
        //get info
        info();
        //attach listener
        buttonListener();
    }

    private void buttonListener() {
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                DatabaseReference mUsers = FirebaseDatabase.getInstance().getReference()
                        .child(userId);

                mUsers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child("number").exists()) {

                            number = dataSnapshot.child("number").getValue().toString();
                            openCallAndMessage(v);

                        } else {

                            Toast.makeText(SingleWorkerActivity.this, "User has not attached his/her number", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        if (databaseError != null){
                            Log.d(TAG, databaseError.getMessage());
                            Toast.makeText(SingleWorkerActivity.this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    private void openCallAndMessage(View v) {

        registerForContextMenu(v);
        openContextMenu(v);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.call_text, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_call:

                makeCall();

                break;
            case R.id.action_sms:

                int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS);

                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    sendSms();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, PERMISSION_SEND_SMS);
                }

                break;
        }

        return super.onContextItemSelected(item);
    }

    private void makeCall() {
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:" + Integer.parseInt(number)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(i);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL);
        }

    }

    private void sendSms() {
        String message = "Hallo,i am interested in your skill and would like to hire you\n" +
                "-From Patakazi";
        SmsManager sm
                = SmsManager.getDefault();
        sm.sendTextMessage(number, null, message, null, null);
        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_SEND_SMS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSms();
                } else {
                    Toast.makeText(this, "You do not have required permission to make the action", Toast.LENGTH_SHORT).show();
                }

                break;
            case PERMISSION_CALL:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeCall();
                } else {
                    Toast.makeText(this, "Please grant the permission to call", Toast.LENGTH_SHORT).show();
                }

        }
    }

    private void info() {


        if (skillKey != null) {

            mSkills.child(skillKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        //load image
                        Glide.with(SingleWorkerActivity.this).load(dataSnapshot.child("image").getValue().toString())
                                .thumbnail(0.5f)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .error(R.mipmap.loading)
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        workerImage.setVisibility(View.VISIBLE);
                                        mProgressBar.setVisibility(View.GONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        workerImage.setVisibility(View.VISIBLE);
                                        mProgressBar.setVisibility(View.GONE);
                                        
                                        return false;
                                    }
                                })
                                .crossFade()
                                .into(workerImage);

                        //load others
                        name.setText(dataSnapshot.child("title").getValue().toString());
                        charges.setText(dataSnapshot.child("charges").getValue().toString());
                        location.setText(dataSnapshot.child("location").getValue().toString());
                        description.setText(dataSnapshot.child("desc").getValue().toString());

                        userId = dataSnapshot.child("uid").getValue().toString();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    private void firebase() {

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(SingleWorkerActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                }

            }
        };

        mSkills = FirebaseDatabase.getInstance().getReference().child("Skills");

    }

    private void key() {
        skillKey = getIntent().getExtras().getString(WorkersFragment.SKILL_KEY);
    }

    private void views() {

        workerImage = (ImageView) findViewById(R.id.iv_singleworker_image);
        name = (TextView) findViewById(R.id.tv_seingleworker_title);
        charges = (TextView) findViewById(R.id.tv_seingleworker_price);
        location = (TextView) findViewById(R.id.tv_seingleworker_location);
        description = (TextView) findViewById(R.id.tv_seingleworker_desc);
        contact = (Button) findViewById(R.id.btn_seingleworker);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_singleworker);

        workerImage.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

    }
}
