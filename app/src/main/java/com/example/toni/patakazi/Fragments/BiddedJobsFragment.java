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
import android.widget.TextView;

import com.example.toni.patakazi.Adapters.BiddedJobsAdapter;
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

public class BiddedJobsFragment extends Fragment {

    private View mView;
    private RecyclerView recyclerView;

    private DatabaseReference mUsers;
    private DatabaseReference mBids;
    private DatabaseReference mJObs;
    private DatabaseReference mAssigns;
    private FirebaseAuth mAuth;
    private String userID= null;

    private TextView indicator;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.biddedjobs,container,false);
        indicator = (TextView) mView.findViewById(R.id.tv_biddedjobs_indicator);
        //setup rv

        recyclerView = (RecyclerView) mView.findViewById(R.id.biddedJobsRv);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);


        //setUpFireBase
        mAssigns = FirebaseDatabase.getInstance().getReference().child("Assigns");
        mAssigns.keepSynced(true);

        mJObs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mJObs.keepSynced(true);

        mBids = FirebaseDatabase.getInstance().getReference().child("Bids");
        mBids.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();



        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        userID = mAuth.getCurrentUser().getUid();
        final List<Jobs> lists = new ArrayList<Jobs>();
        Query getjobkeys = mBids.orderByChild(userID).equalTo(1);

        getjobkeys.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("UserKeyss",userID);
                lists.clear();

                for (DataSnapshot datChild : dataSnapshot.getChildren()){

                    String key = datChild.getKey();

                    DatabaseReference db = mJObs.child(key);

                    db.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()){

                                try {

                                    lists.add(dataSnapshot.getValue(Jobs.class));

                                    if (lists.size() != 0) {

                                        indicator.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);

                                        BiddedJobsAdapter biddedJobsAdapter = new BiddedJobsAdapter(getActivity().getApplicationContext(), lists);
                                        recyclerView.setAdapter(biddedJobsAdapter);
                                        biddedJobsAdapter.notifyDataSetChanged();

                                    }else {
                                        indicator.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                    }

                                }catch (NullPointerException e){
                                    Log.d("sonmedfd",e.getMessage());
                                }

                            }else {
                                indicator.setVisibility(View.VISIBLE);
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
