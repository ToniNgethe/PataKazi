package com.example.toni.patakazi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.Helpers.Global;
import com.example.toni.patakazi.model.Skills;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class BidderProfileActivity extends AppCompatActivity {

    private ImageView profilePic;
    private TextView userName;
    private TextView userEmail;
    private TextView phoneNumber;
    private RecyclerView skillsRv;
    private RecyclerView experiancesRv;
    private ImageButton call, back, message;

    private CardView cardView;

    private Button assignUser;

    private String key = null;

    private DatabaseReference mUsers, mSkills, mAssigns;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private TextView skilsEmpty, experianceSkills;

    private String jobID;

    private boolean process = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidder_profile);

        setUpViews();
        getUserKey();
        setupFireBase();
        setUpProfile();
        setUpClicks();
        checkIfAssigned();

    }

    private void setUpClicks() {

        //assing


        assignUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (assignUser.getText() == "Assign") {

                    new AlertDialog.Builder(BidderProfileActivity.this)
                            .setTitle("Confirm User..")
                            .setMessage("Assign job to this user?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                    assignJob();

                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();

                } else {
                    new AlertDialog.Builder(BidderProfileActivity.this)
                            .setTitle("Confirm User..")
                            .setMessage("Unassign job to this user?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                    assignJob();

                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!phoneNumber.getText().equals("No phone number found")) {


                } else {
                    Global.showDialog("Phone number", "Phone number not found..", BidderProfileActivity.this);
                }

            }
        });


    }

    private void assignJob() {

        process = true;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Assigning job to user....");
        progressDialog.show();

        final DatabaseReference assignDB = mAssigns.child(jobID);

        assignDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (process) {

                    if (dataSnapshot.hasChild(key)) {

                        assignDB.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    Handler handler = new Handler();

                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {

                                            //restore as bidder

                                            DatabaseReference mBids = FirebaseDatabase.getInstance().getReference("Bids");
                                            mBids.child(jobID).child(key).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        Log.d("Bidder", "removed");
                                                    }

                                                }
                                            });

                                        }
                                    };

                                    if (runnable != null) {
                                        handler.post(runnable);
                                    }
                                }
                            }
                        });

                        progressDialog.dismiss();

                        checkIfAssigned();


                        process = false;

                    } else {

                        assignDB.child(key).setValue(1).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    Handler handler = new Handler();

                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {

                                            //remove as bidder....

                                            DatabaseReference mBids = FirebaseDatabase.getInstance().getReference("Bids");
                                            mBids.child(jobID).child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d("Bidder", "Removed welll..");
                                                    }
                                                }
                                            });

                                        }
                                    };

                                    if (runnable != null) {
                                        handler.post(runnable);
                                    }

                                }

                            }
                        });

                        progressDialog.dismiss();

                        Global.showDialog("Job message", "The bidder will be contacted or You can contact him/her from provided no.", BidderProfileActivity.this);


                        checkIfAssigned();

                        process = false;

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void checkIfAssigned() {

        DatabaseReference db = mAssigns.child(jobID);

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(key)) {

                    cardView.setVisibility(View.VISIBLE);
                    assignUser.setText("Assigned");
                    assignUser.setTextColor(Color.parseColor("#ff9800"));
                    assignUser.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_done_black_24dp, 0, 0);

                } else {

                    cardView.setVisibility(View.GONE);
                    assignUser.setText("Assign");
                    assignUser.setTextColor(Color.BLACK);
                    assignUser.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.ic_person_add_black_24dp, 0, 0);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setUpProfile() {

        //get user name,email,image and phone number.....
        final DatabaseReference bidderInfo = mUsers.child(key);

        bidderInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    userName.setText((CharSequence) dataSnapshot.child("user").getValue());

                    if (dataSnapshot.child("location").getValue() != null) {
                        userEmail.setText((CharSequence) dataSnapshot.child("location").getValue());
                    } else {
                        userEmail.setText("User location not set");
                    }

                    if (dataSnapshot.child("number").getValue() != null) {
                        phoneNumber.setText((CharSequence) dataSnapshot.child("number").getValue());
                    } else {
                        phoneNumber.setText("Number not set..");
                    }


                    Glide.with(BidderProfileActivity.this).load(dataSnapshot.child("image").getValue())
                            .crossFade()
                            .thumbnail(0.5f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.mipmap.loading)
                            .into(profilePic);

                } else {

                    Global.showDialog("Info", "Selected user information not found..", BidderProfileActivity.this);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupFireBase() {

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);
        mSkills = FirebaseDatabase.getInstance().getReference().child("Skills");
        mSkills.keepSynced(true);

        mAssigns = FirebaseDatabase.getInstance().getReference().child("Assigns");
        mAssigns.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(BidderProfileActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

            }
        };

    }

    private void getUserKey() {
        key = getIntent().getExtras().getString("key");
    }

    private void setUpViews() {

        profilePic = (ImageView) findViewById(R.id.bidder_profilePic);
        call = (ImageButton) findViewById(R.id.call_bidder);
        back = (ImageButton) findViewById(R.id.backButton);

        userName = (TextView) findViewById(R.id.bidder_profile_name);
        userEmail = (TextView) findViewById(R.id.bidder_email);
        phoneNumber = (TextView) findViewById(R.id.bidder_number);

        skillsRv = (RecyclerView) findViewById(R.id.bidder_skills);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);

        skillsRv.setLayoutManager(lm);

        experiancesRv = (RecyclerView) findViewById(R.id.bidderExperiances);

        message = (ImageButton) findViewById(R.id.messageBidder);
        assignUser = (Button) findViewById(R.id.assignBidderBtn);

        cardView = (CardView) findViewById(R.id.infoCard);

        cardView.setVisibility(View.GONE);

        skilsEmpty = (TextView) findViewById(R.id.emptyTextView1);
        experianceSkills = (TextView) findViewById(R.id.emptyTextView2);

        jobID = getIntent().getExtras().getString("job");

        Log.d("Jobsssssss", jobID);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthStateListener);
        setUpskills();
    }

    private void setUpskills() {

        Query query = mSkills.orderByChild("uid").equalTo(key);

        final FirebaseRecyclerAdapter<Skills, SkillsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Skills, SkillsViewHolder>(

                Skills.class,
                R.layout.bidder_skills_row,
                SkillsViewHolder.class,
                query

        ) {
            @Override
            protected void populateViewHolder(SkillsViewHolder viewHolder, Skills model, int position) {

                if (model == null) {

                    skilsEmpty.setVisibility(View.VISIBLE);

                } else {

                    skilsEmpty.setVisibility(View.GONE);
                }

                viewHolder.setSkillTitle(model.getTitle());
                viewHolder.setSkillDesc(model.getDesc());
                viewHolder.setMessage(getApplicationContext(), model.getImage());

            }
        };

        skillsRv.setAdapter(firebaseRecyclerAdapter);

        firebaseRecyclerAdapter.notifyDataSetChanged();

    }


    public static class SkillsViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        public SkillsViewHolder(View itemView) {
            super(itemView);

            this.mView = itemView;
        }

        public void setMessage(Context ctx, String url) {

            ImageView imageView = (ImageView) mView.findViewById(R.id.bidder_skillProfile);

            Glide.with(ctx).load(url)
                    .crossFade()
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .into(imageView);

        }

        public void setSkillTitle(String title) {
            TextView textView = (TextView) mView.findViewById(R.id.bidderSkillTitle);
            textView.setText(title);
        }

        public void setSkillDesc(String desc) {
            TextView textView = (TextView) mView.findViewById(R.id.bidderSkillDesc);
            textView.setText(desc);
        }
    }
}
