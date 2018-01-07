package com.toni.patakazi.Fragments.myjobs;

import android.os.Bundle;
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
import com.toni.patakazi.Adapters.PostedCompletedJobsAdapter;
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

public class PostedCompletedJobs extends Fragment {

    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.layout_empty)
    ConstraintLayout layoutEmpty;
    Unbinder unbinder;
    private View mView;

    private RecyclerView mRv;
    private ProgressBar mProgressBar;

    private DatabaseReference mCompleted, mJobs;
    private FirebaseAuth mAuth;

    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.completed_jobd_fragment, container, false);

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

        unbinder = ButterKnife.bind(this, mView);
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

                for (final DataSnapshot singleShot : dataSnapshot.getChildren()) {

                    list.clear();

                    String jobId = singleShot.getKey();

                    mCompleted.child(jobId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                list.add(singleShot.getValue(Jobs.class));

                                PostedCompletedJobsAdapter postedCompletedJobsAdapter
                                        = new PostedCompletedJobsAdapter(getActivity(), list);

                                mRv.setAdapter(postedCompletedJobsAdapter);
                                mProgressBar.setVisibility(View.GONE);
                                postedCompletedJobsAdapter.notifyDataSetChanged();

                            }else {

                                mProgressBar.setVisibility(View.GONE);
                                mRv.setVisibility(View.GONE);
                                layoutEmpty.setVisibility(View.VISIBLE);
                                tvEmpty.setText("None of your jobs are completed");

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

