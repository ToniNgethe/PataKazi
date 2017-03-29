package com.example.toni.patakazi.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.toni.patakazi.Adapters.AssignedJobsAdapter;
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
 * Created by toni on 2/20/17.
 */

public class AssignedJobFragment extends Fragment {

    private View mView;
    private RecyclerView mRecyclerView;
    private DatabaseReference mAssigns ;
    private DatabaseReference mJobs;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.assigned_jobs,container,false);

        //setup views......
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.ajRecycleView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        //setUp firebase
        mAssigns = FirebaseDatabase.getInstance().getReference().child("Assigns");
        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mAuth = FirebaseAuth.getInstance();

        return mView;

    }

    @Override
    public void onStart() {
        super.onStart();

        final List<Jobs> list = new ArrayList<>();

        Query query = mAssigns.orderByChild(mAuth.getCurrentUser().getUid()).equalTo(1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String key;
                list.clear();

                for (DataSnapshot sinSnapshot : dataSnapshot.getChildren()){

                    key = sinSnapshot.getKey();

                    DatabaseReference databaseReference = mJobs.child(key);

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){
                                try {

                                    list.add(dataSnapshot.getValue(Jobs.class));

                                    AssignedJobsAdapter  assignedJobsAdapter  = new AssignedJobsAdapter(getActivity().getApplicationContext(),list);
                                    mRecyclerView.setAdapter(assignedJobsAdapter);
                                    assignedJobsAdapter.notifyDataSetChanged();

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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
