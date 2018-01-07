package com.toni.patakazi.Fragments.myjobs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.toni.patakazi.BiddedJobActivity;
import com.toni.patakazi.R;
import com.toni.patakazi.model.Jobs;
import com.toni.patakazi.ui.intro_ui.LoginActivity;
import com.toni.patakazi.utils.Global;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by toni on 2/21/17.
 */

public class OpenJobsFragment extends Fragment {

    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.layout_empty)
    ConstraintLayout layoutEmpty;
    Unbinder unbinder;
    private View mView;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mJobs;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mBids;
    private Query myJobs;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_poste_jobs, container, false);

        setUpViews();
        initiateFirebase();

        unbinder = ButterKnife.bind(this, mView);
        return mView;
    }


    private void initiateFirebase() {

        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);

        mBids = FirebaseDatabase.getInstance().getReference().child("Bids");
        mBids.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                try {
                    if (firebaseAuth.getCurrentUser() == null) {
                        startActivity(new Intent(getActivity(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                } catch (NullPointerException e) {
                    e.getMessage();
                }
            }
        };

        myJobs = mJobs.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());
        Global.writeToLog("jpbd", mJobs.toString());

        myJobs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){
                    layoutEmpty.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    tvEmpty.setText("You have not posted any jobs");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setUpViews() {

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.rvMyJobs);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        FirebaseRecyclerAdapter<Jobs, PostedJobViewHolder> mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Jobs, PostedJobViewHolder>(

                Jobs.class,
                R.layout.my_jobs_row,
                PostedJobViewHolder.class,
                myJobs

        ) {
            @Override
            protected void populateViewHolder(final PostedJobViewHolder viewHolder, final Jobs model, int position) {

                mRecyclerView.setVisibility(View.VISIBLE);

                final String jobID = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setPicture(getActivity(), model.getImage());
                viewHolder.setDate(model.getDate());
                viewHolder.setLocation(model.getLocation());
                viewHolder.viewBidders.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(getActivity(), BiddedJobActivity.class);
                        intent.putExtra("jobID", jobID);
                        startActivity(intent);

                    }
                });

                DatabaseReference db = mBids.child(jobID);

                db.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        viewHolder.setNumber(dataSnapshot.getChildrenCount());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };

        mFirebaseRecyclerAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mFirebaseRecyclerAdapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public static class PostedJobViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        private ProgressBar mProgressBar;
        public Button viewBidders;
        private ImageView imageView;

        private DatabaseReference mBids;

        public PostedJobViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mBids = FirebaseDatabase.getInstance().getReference().child("Bids");
            mBids.keepSynced(true);

            imageView = (ImageView) mView.findViewById(R.id.myJobImage);
            imageView.setVisibility(View.INVISIBLE);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar_myjobs);
            viewBidders = (Button) mView.findViewById(R.id.myJobViewBids);
        }


        public void setTitle(String title) {

            TextView textView = (TextView) mView.findViewById(R.id.myJobTitle);
            textView.setText(title);

        }

        public void setDate(String date) {

            TextView textView = (TextView) mView.findViewById(R.id.myJobDatePosted);
            textView.setText(date);

        }

        public void setLocation(String location) {
            TextView textView = (TextView) mView.findViewById(R.id.myJobLocationPosted);
            textView.setText(location);
        }

        public void setPicture(Context ctx, String url) {

            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            imageView.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            imageView.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);

                            return false;
                        }
                    })
                    .error(R.mipmap.error_network)
                    .into(imageView);

        }

        public void setNumber(long no) {

            TextView textView = (TextView) mView.findViewById(R.id.myJobBids);
            textView.setText(String.valueOf(no));

        }


    }

}
