package com.example.toni.patakazi;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.Fragments.JobsFragment;
import com.example.toni.patakazi.Helpers.Global;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class SingleJobActiivity extends AppCompatActivity {

    private ImageView jobImage,profileImage;
    private TextView title, desc, location,user, email,number,price,workers,date;
    private String jobId;
    private Button bidBtn;

    private DatabaseReference mJobs;
    private DatabaseReference mUsers;
    private DatabaseReference mBids;
    private FirebaseAuth mAuth;
    private Query mBidQudery;
    private Boolean process = true;
    private Drawable d ;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_job_actiivity);

        setUpViews();
        setUpFirebase();
        checkIfBidded();
        initiateListeners();
        placeBid();

    }

    private void placeBid() {


        mJobs.child( getIntent().getExtras().getString("jobiD")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("status").getValue().toString() == String.valueOf(1)){

                    jobNotAvailable();

                }else {

                    jobAvailableForBidding();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void jobNotAvailable() {

        bidBtn.setBackgroundResource(R.color.cardview_dark_background);
        bidBtn.setText("Job closed by owner");

    }

    private void jobAvailableForBidding() {

        bidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Submitting request..");
                progressDialog.show();

                process = true;

                final String jobId = getIntent().getExtras().getString("jobiD");
                final String cu = mAuth.getCurrentUser().getUid();

                mBids.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (process) {

                            if (dataSnapshot.child(jobId).hasChild(mAuth.getCurrentUser().getUid())) {

                                mBids.child(jobId).child(cu).removeValue();
                                progressDialog.dismiss();
                                Global.showDialog("Bid feedback", "Bid successfully removed", SingleJobActiivity.this);

                                bidBtn.setText("Place Bid");
                                process = false;

                            } else {

                                mBids.child(jobId).child(cu).setValue(1);

                                progressDialog.dismiss();
                                Global.showDialog("Bid feedback", "Bid successfully submitted. You will be contacted soon..", SingleJobActiivity.this);
                                bidBtn.setText("Already bid (Unbid)");
                                process = false;


                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });


    }

    private void initiateListeners() {

        final String uid;

        mJobs.child(jobId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    if (mAuth.getCurrentUser().getUid().equals(dataSnapshot.child("uid").getValue().toString())){
                        bidBtn.setVisibility(View.GONE);
                    }

                    title.setText(dataSnapshot.child("title").getValue().toString());
                    desc.setText(dataSnapshot.child("desc").getValue().toString());
                    location.setText(dataSnapshot.child("location").getValue().toString());
                    price.setText(dataSnapshot.child("charges").getValue().toString());
                    workers.setText(dataSnapshot.child("workers").getValue().toString());

                    //dataSnapshot.child("")

                    Picasso.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).networkPolicy(NetworkPolicy.OFFLINE).into(jobImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(jobImage);
                        }
                    });


                    mUsers.child(dataSnapshot.child("uid").getValue().toString()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                user.setText(dataSnapshot.child("user").getValue().toString());

                                Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString())
                                        .crossFade()
                                        .thumbnail(0.5f)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .error(R.mipmap.loading)
                                        .into(profileImage);

//                                Picasso.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).error(R.mipmap.loading).networkPolicy(NetworkPolicy.OFFLINE).into(profileImage, new Callback() {
//                                    @Override
//                                    public void onSuccess() {
//
//                                    }
//
//                                    @Override
//                                    public void onError() {
//                                        Picasso.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).error(R.mipmap.loading).into(profileImage);
//                                    }
//                                });

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void setUpFirebase() {

        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        mBids = FirebaseDatabase.getInstance().getReference().child("Bids");

        mAuth = FirebaseAuth.getInstance();

    }

    private void setUpViews() {

        jobId = getIntent().getExtras().getString("jobiD");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSingle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("Job description");

        jobImage = (ImageView) findViewById(R.id.singleJobImage);
        profileImage = (ImageView) findViewById(R.id.singleProfileImage);

        title = (TextView) findViewById(R.id.singleTitle1);
        desc = (TextView) findViewById(R.id.singleDesc);
        location = (TextView) findViewById(R.id.singleLocation1);
        email = (TextView) findViewById(R.id.singleEmail1);
        number = (TextView) findViewById(R.id.singlePhoneNumber1);
        price = (TextView) findViewById(R.id.singleCharges1);
        workers = (TextView) findViewById(R.id.singleWorkers1);
        date = (TextView) findViewById(R.id.singleDate1);

        user = (TextView) findViewById(R.id.singleName1);

        bidBtn = (Button) findViewById(R.id.bidBtn);

        progressDialog = new ProgressDialog(this);

        d = bidBtn.getBackground();
    }

    private void checkIfBidded(){

   //   DatabaseReference db =   mBids.child(getIntent().getExtras().getString("jobiD"));

        mBids.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(getIntent().getExtras().getString("jobiD")).hasChild(mAuth.getCurrentUser().getUid())){

                    bidBtn.setBackgroundResource(R.color.cardview_dark_background);
                    bidBtn.setText("Already bid (Unbid)");


                }else {
                    bidBtn.setBackground(d);
                    bidBtn.setText("Place Bid");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
