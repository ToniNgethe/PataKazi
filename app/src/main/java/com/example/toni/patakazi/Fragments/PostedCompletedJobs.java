package com.example.toni.patakazi.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.toni.patakazi.Adapters.PostedCompletedJobsAdapter;
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

public class PostedCompletedJobs extends Fragment {

    private View mView;

    private RecyclerView mRv;
    private ProgressBar mProgressBar;

    private DatabaseReference mCompleted,mJobs;
    private FirebaseAuth mAuth;

    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.completed_jobd_fragment,container,false);

        //setup views.....
        mRv = (RecyclerView) mView.findViewById(R.id.postedCompletedJobsRv);
        mProgressBar = (ProgressBar) mView.findViewById(R.id.postedCompletedJobsProgressBar);

        //setup linear manager for rv....
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        //add to rv
        mRv.setLayoutManager(linearLayoutManager);

        //setup firebase....
        mCompleted = FirebaseDatabase.getInstance().getReference().child("Completed");
        mCompleted.keepSynced(true);

        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJobs.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        //get my posted Job id

        Query query = mJobs.orderByChild("uid").equalTo(userId);

        final List<Jobs> list = new ArrayList<>();



        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (final DataSnapshot singleShot : dataSnapshot.getChildren()){

                    list.clear();

                    String jobId = singleShot.getKey();

                    mCompleted.child(jobId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                list.add(singleShot.getValue(Jobs.class));

                                PostedCompletedJobsAdapter postedCompletedJobsAdapter
                                        = new PostedCompletedJobsAdapter(getActivity(),list);

                                mRv.setAdapter(postedCompletedJobsAdapter);
                                mProgressBar.setVisibility(View.GONE);
                                postedCompletedJobsAdapter.notifyDataSetChanged();

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

