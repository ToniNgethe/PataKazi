package com.toni.patakazi.Fragments.myjobs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.toni.patakazi.Adapters.PostedAssignedJobsAdapter;
import com.toni.patakazi.R;
import com.toni.patakazi.model.Jobs;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by toni on 2/21/17.
 */

public class PostedAssignedJobs extends Fragment {

    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.layout_empty)
    ConstraintLayout layoutEmpty;
    Unbinder unbinder;
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

        unbinder = ButterKnife.bind(this, mView);
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
                                layoutEmpty.setVisibility(View.GONE);

                            }else {

                                mRecyclerView.setVisibility(View.GONE);
                                mProgressBar.setVisibility(View.GONE);
                                layoutEmpty.setVisibility(View.VISIBLE);
                                tvEmpty.setText("You have not assigned any jobs");

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
