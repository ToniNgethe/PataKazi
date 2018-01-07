package com.toni.patakazi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.toni.patakazi.Dialogs.RateWorkerDialog;
import com.toni.patakazi.Dialogs.ReviewWorkerDialog;
import com.toni.patakazi.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CompletedWorkers extends AppCompatActivity {

    private static final String TAG = CompletedWorkers.class.getSimpleName();
    private Context ctx;
    private String jobKey;
    private RecyclerView mRv;
    private ProgressBar progressBar;
    private DatabaseReference mJobs;
    private DatabaseReference mCoompleted;
    private DatabaseReference mUsers;
    private FirebaseAuth mAurth;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_workers);

        //set up views....
        mRv = (RecyclerView) findViewById(R.id.completedWorkersDialogRv);
        progressBar = (ProgressBar) findViewById(R.id.completedWorkersDialogProgressBar);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        lm.setReverseLayout(true);
        mRv.setLayoutManager(lm);

        //setup firebase....
        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mCoompleted = FirebaseDatabase.getInstance().getReference().child("Completed");
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAurth = FirebaseAuth.getInstance();

        //get job key
        jobKey = getIntent().getExtras().getString("JOBKEY");

        mToolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Completed workers");

        Log.d(TAG,jobKey);

    }


    @Override
    protected void onStart() {
        super.onStart();

        final List<Users> jobs = new ArrayList<>();

        //get confirmed people....
        mCoompleted.child(jobKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleShot : dataSnapshot.getChildren()) {

                    jobs.clear();

                    final String userId = singleShot.getKey();

                    Log.d(TAG,userId);

                    mUsers.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Users u = dataSnapshot.getValue(Users.class);

                            jobs.add(dataSnapshot.getValue(Users.class));

                            Log.d(TAG, u.getUser() + " " + u.getNumber());

                            CompletedWorkersAdapter completeddAdapter = new CompletedWorkersAdapter(CompletedWorkers.this, jobs,jobKey, userId);
                            mRv.setAdapter(completeddAdapter);
                            completeddAdapter.notifyDataSetChanged();

                            progressBar.setVisibility(View.GONE);

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

    public class CompletedWorkersAdapter extends RecyclerView.Adapter<CompletedWorkersViewHolder> {

        private Context ctx;
        private List<Users> userList = new ArrayList<>();
        private String jobKey;
        private String userId;

        public CompletedWorkersAdapter(Context ctx, List<Users> users, String key, String userKey) {

            this.ctx = ctx;
            this.userList = users;
            this.jobKey = key;
            userId = userKey;

        }

        @Override
        public CompletedWorkersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.completed_workers_row, parent, false);

            return new CompletedWorkersViewHolder(ctx, v);

        }

        @Override
        public void onBindViewHolder(CompletedWorkersViewHolder holder, int position) {

            if (holder != null) {

                Log.d(TAG,"User :" + userId + "Key :" + jobKey);

                final Users users = userList.get(position);
                holder.setCompletedWorkersImageView(users.getImage());
                holder.setCompletedWorkersTitle(users.getUser());
                holder.setCompleteddWorkersNumber(users.getNumber());

                holder.setCompletedJobRating(jobKey,userId);

                holder.setCompletedJobReview(jobKey,userId);

                holder.reviewCompletedWorker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //open review dialog
                        ReviewWorkerDialog reviewWorkerDialog = new ReviewWorkerDialog(ctx,userId,jobKey);
                        reviewWorkerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        reviewWorkerDialog.setCanceledOnTouchOutside(false);
                        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
                        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);

                        reviewWorkerDialog.getWindow().setLayout(width, height);
                        reviewWorkerDialog.show();

                    }
                });

                holder.rateCompletedWorker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //open rating bar
                        RateWorkerDialog rateWorkerDialog = new RateWorkerDialog(ctx,userId,jobKey);
                        rateWorkerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        rateWorkerDialog.setCanceledOnTouchOutside(false);
                        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
                        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.90);

                        rateWorkerDialog.getWindow().setLayout(width, height);
                        rateWorkerDialog.show();

                    }
                });


            }else {
                Toast.makeText(ctx, "No user found", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }
    }

    public static class CompletedWorkersViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private Context ctx;
        private ProgressBar progressBar;
        private DatabaseReference mRatings;
        private TextView textView;
        public Button rateCompletedWorker, reviewCompletedWorker;

        public CompletedWorkersViewHolder(Context ctx, View itemView) {
            super(itemView);

            mView = itemView;
            this.ctx = ctx;
            progressBar = (ProgressBar) mView.findViewById(R.id.completedWorkRowProgress);

            rateCompletedWorker = (Button) mView.findViewById(R.id.rateCompletedWorker);
            reviewCompletedWorker = (Button) mView.findViewById(R.id.reviewCompletedWorker);
            textView = (TextView) mView.findViewById(R.id.completedWorkRated);

            mRatings = FirebaseDatabase.getInstance().getReference().child("Feedback");
        }

        public void setCompletedWorkersImageView(String url) {
            final ImageView imageView = (ImageView) mView.findViewById(R.id.completedWorkRowImage);

            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            imageView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            return false;

                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            imageView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                            return false;
                        }
                    })
                    .crossFade()
                    .into(imageView);
        }

        public void setCompletedWorkersTitle(String title) {

            TextView textView = (TextView) mView.findViewById(R.id.completedWorkRowUsername);
            textView.setText(title);

        }

        public void setCompleteddWorkersNumber(String title) {
            TextView textView = (TextView) mView.findViewById(R.id.completedWorkRowNumber);
            textView.setText(title);
        }

        public void setCompletedJobRating(String jobId,String userId){

            mRatings.child(userId).child(jobId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("Rating").exists()){

                        textView.setText("Rated : " + dataSnapshot.child("Rating").getValue());
                        textView.setTextColor(Color.parseColor("#ff9800"));

                        rateCompletedWorker.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_done_black_24dp,0,0,0);
                        rateCompletedWorker.setText("Worker rated ");
                        rateCompletedWorker.setClickable(false);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setCompletedJobReview(String jobId,String userId){

            mRatings.child(userId).child(jobId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("Review").exists()){

                        reviewCompletedWorker.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_done_black_24dp,0,0,0);
                        reviewCompletedWorker.setText("Worker reviewed ");
                        reviewCompletedWorker.setClickable(false);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }
}
