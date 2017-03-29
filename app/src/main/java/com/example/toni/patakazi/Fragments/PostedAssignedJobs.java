package com.example.toni.patakazi.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.toni.patakazi.Adapters.PostedAssignedJobsAdapter;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.model.Jobs;
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
 * Created by toni on 2/21/17.
 */

public class PostedAssignedJobs extends Fragment {

    private View mView;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mJobs;
    private DatabaseReference mAssigns;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.posted_assigned_jobs, container, false);

        mRecyclerView = (RecyclerView) mView.findViewById(R.id.postedAssignedRv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.postedAssingnedRvIndicator);


        //FIREBASE INIT

        mAuth = FirebaseAuth.getInstance();
        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);

        mAssigns = FirebaseDatabase.getInstance().getReference().child("Assigns");
        mAssigns.keepSynced(true);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        String userId = mAuth.getCurrentUser().getUid();
        final List<Jobs> list = new ArrayList<>();

        //get job key
        Query getJobKey = mJobs.orderByChild("uid").equalTo(userId);
        getJobKey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();
                String key = null;

                for (final DataSnapshot singleShot : dataSnapshot.getChildren()) {
                    key = singleShot.getKey();

                    DatabaseReference query = mAssigns;

                    final String finalKey = key;
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot1) {

                            //check if job assigned{
                            if (dataSnapshot1.hasChild(finalKey)) {

                                Jobs h = singleShot.getValue(Jobs.class);
                                list.add(h);
                                PostedAssignedJobsAdapter postedAssignedJobsAdapter = new PostedAssignedJobsAdapter(getActivity(), list);
                                mRecyclerView.setAdapter(postedAssignedJobsAdapter);
                                postedAssignedJobsAdapter.notifyDataSetChanged();

                                mRecyclerView.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.GONE);

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
}
