package com.toni.patakazi.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.toni.patakazi.R;
import com.toni.patakazi.model.Jobs;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toni on 2/20/17.
 */

public class CompletedJobsFragment extends Fragment {

    private static final String TAG = CompletedJobsFragment.class.getSimpleName();
    private View mView;
    private RecyclerView rv;
    private ProgressBar progressBar;

    private DatabaseReference mCompleted;
    private DatabaseReference mUsers;
    private DatabaseReference mJobs;
    private FirebaseAuth mAuth;

    private String userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_completedjobfragment, container, false);

        //setup views.....
        rv = (RecyclerView) mView.findViewById(R.id.rv_completedjobsfragment_completed);
        progressBar = (ProgressBar) mView.findViewById(R.id.pb_completedjobsfragment_load);

        //linearmanager
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setStackFromEnd(true);
        lm.setReverseLayout(true);

        rv.setLayoutManager(lm);

        //setup firebase.....
        mCompleted = FirebaseDatabase.getInstance().getReference().child("Completed");
        mCompleted.keepSynced(true);

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final List<Jobs> jobList = new ArrayList<>();

        //get job key
        Query query = mCompleted.orderByChild(userId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    final String jobKey = snapshot.getKey();

                    mCompleted.child(jobKey).child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {


                            if (dataSnapshot.exists()) {

                                Log.d(TAG, snapshot.getKey());

                                mJobs.child(jobKey).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        jobList.clear();
                                        jobList.add(dataSnapshot.getValue(Jobs.class));

                                        MyCompletedJobsAdapter myCompletedJobsAdapter = new MyCompletedJobsAdapter(getActivity(), jobList, jobKey);
                                        rv.setAdapter(myCompletedJobsAdapter);

                                        myCompletedJobsAdapter.notifyDataSetChanged();
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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static class MyCompletedJobsAdapter extends RecyclerView.Adapter<MyCompletedJobsViewHolder> {

        private Context ctx;
        private List<Jobs> jobList = new ArrayList<>();
        private String jobKey;
        private FirebaseAuth mAuth;

        public MyCompletedJobsAdapter(Context activity, List<Jobs> jobList, String jobKey) {

            this.ctx = activity;
            this.jobList = jobList;
            this.jobKey = jobKey;

        }

        @Override
        public MyCompletedJobsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_mycompletedjobsadapter_row, parent, false);
            return new MyCompletedJobsViewHolder(ctx, v);

        }

        @Override
        public void onBindViewHolder(final MyCompletedJobsViewHolder holder, int position) {

            if (holder != null) {

                mAuth = FirebaseAuth.getInstance();

                Jobs jobs = jobList.get(position);
                holder.setMyCompletedJobImage(jobs.getImage());
                holder.setMyCompletedJobTitle(jobs.getTitle());

                //get ratings and reviews
                DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Feedback");
                db.child(mAuth.getCurrentUser().getUid()).child(jobKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            try {
                                holder.setMyCompletedJobRating(dataSnapshot.child("Rating").getValue());
                                holder.setMyCompletedJobReview(dataSnapshot.child("Review").getValue().toString());
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

        @Override
        public int getItemCount() {
            return jobList.size();
        }
    }

    public static class MyCompletedJobsViewHolder extends RecyclerView.ViewHolder {

        private Context context;
        private View mView;

        public MyCompletedJobsViewHolder(Context ctx, View itemView) {
            super(itemView);

            context = ctx;
            mView = itemView;
        }

        public void setMyCompletedJobImage(String url) {
            final ImageView imageView = (ImageView) mView.findViewById(R.id.iv_mycompletedjobadapter_jobimg);
            final ProgressBar progressBar = (ProgressBar) mView.findViewById(R.id.pb_mycompletedjobs_loading);

            Glide.with(context).load(url)
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

        public void setMyCompletedJobTitle(String title) {
            TextView textView = (TextView) mView.findViewById(R.id.tv_mycompletedjobadapt_title);
            textView.setText(title);
        }

        public void setMyCompletedJobRating(Object rate) {

            RatingBar ratingBar = (RatingBar) mView.findViewById(R.id.rb_mycompletedjobadapt_rates);
            ratingBar.setRating(Float.parseFloat((String) rate));

            TextView textView = (TextView) mView.findViewById(R.id.tv_mycompleted_rate);
            textView.setText((String) rate + "/10");

        }

        public void setMyCompletedJobReview(String review) {

            TextView textView = (TextView) mView.findViewById(R.id.tv_mycompletedjobadapter_review);
            textView.setText(review);

        }
    }
}
