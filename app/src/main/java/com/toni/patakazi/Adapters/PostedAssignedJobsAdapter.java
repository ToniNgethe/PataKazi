package com.toni.patakazi.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.toni.patakazi.Dialogs.ConfirmedAssigned;
import com.toni.patakazi.Dialogs.UnconfirmedDialog;
import com.toni.patakazi.R;
import com.toni.patakazi.model.Jobs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by toni on 2/22/17.
 */

public class PostedAssignedJobsAdapter extends RecyclerView.Adapter<PostedJobsViewHolder> {
    final String TAG = PostedAssignedJobsAdapter.class.getSimpleName();
    private Context ctx;
    private List<Jobs> jobsList;
    private Handler handler;

    public PostedAssignedJobsAdapter(Context ctx, List<Jobs> list) {
        this.ctx = ctx;
        this.jobsList = list;
    }

    @Override
    public PostedJobsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PostedJobsViewHolder(ctx, LayoutInflater.from(parent.getContext()).inflate(R.layout.posted_assigned_jobs_row, parent, false));
    }

    @Override
    public void onBindViewHolder(final PostedJobsViewHolder holder, int position) {

        if (holder != null) {


            final Jobs jobs = jobsList.get(position);
            holder.postesAssignedJobTitle(jobs.getTitle());
            holder.postedAssignedImage(jobs.getImage());
            holder.postedAssignedJobDate(jobs.getDate());
            getJobId(holder, jobs);

            holder.confirmedBidders.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Jobs");
                    Query query = db.orderByChild("image").equalTo(jobs.getImage());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                ConfirmedAssigned confirmedAssigned = new ConfirmedAssigned(ctx, ds.getKey());
                                confirmedAssigned.setCanceledOnTouchOutside(false);
                                confirmedAssigned.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                confirmedAssigned.show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });

            //get unconfirmed


            holder.unConfirmedBidders.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Jobs");
                    Query query = db.orderByChild("image").equalTo(jobs.getImage());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                UnconfirmedDialog confirmedAssigned = new UnconfirmedDialog(ctx, ds.getKey());
                                confirmedAssigned.setCanceledOnTouchOutside(false);
                                confirmedAssigned.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                confirmedAssigned.show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });


            //close the job
            holder.closeJob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    handler = new Handler();


                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            final ProgressDialog progressDialog = new ProgressDialog(ctx);
                            progressDialog.setMessage("Closing Job..");
                            progressDialog.setCanceledOnTouchOutside(false);
                            progressDialog.show();

                            final boolean[] process = {true};
                            //get Job id
                            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Jobs");
                            Query query = db.orderByChild("image").equalTo(jobs.getImage());
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                        final String jobId = ds.getKey();

                                        DatabaseReference jobToBeClosed = FirebaseDatabase.getInstance().getReference().child("Jobs");

                                        jobToBeClosed.child(jobId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                Log.d(TAG, dataSnapshot.child("status").getValue().toString());


                                                if (process[0]) {


                                                    if (dataSnapshot.child("status").getValue().toString() == String.valueOf(0)) {


                                                        dataSnapshot.getRef().child("status").setValue(1);

                                                        progressDialog.dismiss();

                                                        Toast.makeText(ctx, "Job closed successfully", Toast.LENGTH_SHORT).show();

                                                        process[0] = false;

                                                    } else {

                                                        dataSnapshot.getRef().child("status").setValue(0);

                                                        progressDialog.dismiss();

                                                        Toast.makeText(ctx, "Job Opened successfully", Toast.LENGTH_SHORT).show();

                                                        process[0] = false;
                                                    }

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
                    };

                    if (runnable != null) {

                        handler.post(runnable);

                    }
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return jobsList.size();
    }

    public void getJobId(final PostedJobsViewHolder holder, Jobs jobs) {


        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Jobs");
        Query query = db.orderByChild("image").equalTo(jobs.getImage());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Log.d("sadsds", ds.getKey());
                    String key = ds.getKey();
                    holder.getTotalConfirmed(key);
                    holder.checkJobStatus(key);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

class PostedJobsViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    private ProgressBar progressBar;
    private Context ctx;
    public Button closeJob;
    public ImageButton confirmedBidders, unConfirmedBidders;
    private FirebaseAuth mAuth;
    private DatabaseReference mJobs;
    private long a, b;
    private DatabaseReference mAssigns, mConfirms;

    private TextView totalConf, confirmed, unconfirmed;

    public PostedJobsViewHolder(Context ctx, View itemView) {
        super(itemView);
        this.ctx = ctx;
        mView = itemView;

        progressBar = (ProgressBar) mView.findViewById(R.id.postedAssignedIndicator);
        closeJob = (Button) mView.findViewById(R.id.postedAssignedCloseBtn);

        confirmedBidders = (ImageButton) mView.findViewById(R.id.postedAssignedConfimedBtn);
        unConfirmedBidders = (ImageButton) mView.findViewById(R.id.postedAssignedUnConfimedBtn);

        mAssigns = FirebaseDatabase.getInstance().getReference().child("Assigns");
        mAssigns.keepSynced(true);
        mConfirms = FirebaseDatabase.getInstance().getReference().child("Confirms");
        mConfirms.keepSynced(true);
        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);


        confirmed = (TextView) mView.findViewById(R.id.postedAssignedConfirmedTxt);

    }


    //get total confirmed
    public void getTotalConfirmed(final String jobId) {

        DatabaseReference db = mConfirms.child(jobId);
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    confirmed.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                    b = dataSnapshot.getChildrenCount();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //set title.....
    public void postesAssignedJobTitle(String title) {
        TextView textView = (TextView) mView.findViewById(R.id.postedAssignedTitle);
        textView.setText(title);
    }

    //setDate...
    public void postedAssignedJobDate(String date) {
        TextView textView = (TextView) mView.findViewById(R.id.postedAssignedDate);
        textView.setText(date);
    }

    //setImage
    public void postedAssignedImage(String url) {
        final ImageView imageView = (ImageView) mView.findViewById(R.id.postedAssignedImage);
        imageView.setVisibility(View.GONE);
        Glide.with(ctx).load(url)
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.loading)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                        progressBar.setVisibility(View.GONE);
                        imageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .crossFade()
                .into(imageView);
    }

    public void checkJobStatus(String jobId) {


        mJobs.child(jobId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("status").getValue().toString() == String.valueOf(1)) {

                    closeJob.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.check, 0, 0, 0);
                    closeJob.setText("Open this job");

                } else {

                    closeJob.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_close_black_24dp, 0, 0, 0);
                    closeJob.setText("close this job");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}