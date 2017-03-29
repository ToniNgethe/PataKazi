package com.example.toni.patakazi.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.BiddedJobActivity;
import com.example.toni.patakazi.LoginActivity;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.model.Jobs;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by toni on 2/21/17.
 */

public class OpenJobsFragment extends Fragment {

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
            }catch (NullPointerException e){
                e.getMessage();
            }
            }
        };

        myJobs = mJobs.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());
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

        FirebaseRecyclerAdapter<Jobs,PostedJobViewHolder> mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Jobs, PostedJobViewHolder>(

                Jobs.class,
                R.layout.my_jobs_row,
                PostedJobViewHolder.class,
                myJobs

        ) {
            @Override
            protected void populateViewHolder(final PostedJobViewHolder viewHolder, final Jobs model, int position) {

                final String jobID = getRef(position).getKey();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setPicture(getActivity(), model.getImage());
                viewHolder.setDate(model.getDate());
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

    public static class PostedJobViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public Button viewBidders;

        private DatabaseReference mBids;

        public PostedJobViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mBids = FirebaseDatabase.getInstance().getReference().child("Bids");
            mBids.keepSynced(true);

            viewBidders = (Button) mView.findViewById(R.id.myJobViewBids);
        }


        public void setTitle(String title){

            TextView textView = (TextView) mView.findViewById(R.id.myJobTitle);
            textView.setText(title);

        }

        public void setDate(String date){

            TextView textView = (TextView) mView.findViewById(R.id.myJobDatePosted);
            textView.setText(date);

        }

        public void setPicture(Context ctx, String url){

            ImageView imageView = (ImageView) mView.findViewById(R.id.myJobImage);
            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .into(imageView);

        }

        public void setNumber(long no){

            TextView textView = (TextView) mView.findViewById(R.id.myJobBids);
            textView.setText(String.valueOf(no));

        }


    }

}
