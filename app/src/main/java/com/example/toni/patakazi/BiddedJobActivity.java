package com.example.toni.patakazi;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.Adapters.BidderJobAdapter;
import com.example.toni.patakazi.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BiddedJobActivity extends AppCompatActivity {

    private RecyclerView bidRecycleView;
    private Toolbar mToolBar;
    private DatabaseReference mBidders;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private List<Users> users ;
    private BidderJobAdapter bidderJobAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidded_job);

        setUpViews();
        intiateireBase();

    }

    private void intiateireBase() {

        mBidders = FirebaseDatabase.getInstance().getReference().child("Bids");
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(BiddedJobActivity.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

            }
        };

    }

    private void setUpViews() {

        bidRecycleView = (RecyclerView) findViewById(R.id.biddersRV);
        mToolBar = (Toolbar) findViewById(R.id.toolbarBidders);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView.LayoutManager lm = new GridLayoutManager(BiddedJobActivity.this,3);

        bidRecycleView.setLayoutManager(lm);
        bidRecycleView.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(3), true));
        bidRecycleView.setItemAnimator(new DefaultItemAnimator());

        users = new ArrayList<Users>();
    }


    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        final DatabaseReference m= mBidders.child(getIntent().getExtras().getString("jobID"));

        m.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                users.clear();

                for (DataSnapshot mData : dataSnapshot.getChildren()){

                    Log.d("sjfndsjf",mData.getKey());

                    final DatabaseReference  databaseReference = mUsers.child(mData.getKey());

                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                             Users u = dataSnapshot.getValue(Users.class);

                           users.add(u);
                            Log.d(u.getUser(),dataSnapshot.getKey());

                            bidderJobAdapter = new BidderJobAdapter(getApplicationContext(),users,getIntent().getExtras().getString("jobID"));
                            bidderJobAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                            });


                            bidderJobAdapter.notifyDataSetChanged();
                            bidRecycleView.setAdapter(bidderJobAdapter);

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

    public static class BiddedJobViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public Button mButton;

        private DatabaseReference mAssigns;


        public BiddedJobViewHolder(Context mContext, View itemView) {
            super(itemView);

            mView = itemView;
            mButton = (Button) itemView.findViewById(R.id.assignBidder);

            mAssigns = FirebaseDatabase.getInstance().getReference().child("Assigns");
            mAssigns.keepSynced(true);
        }

        public void setImge(Context ctx,String url){

            ImageView imageView = (ImageView) mView.findViewById(R.id.bidder_profile);

            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .into(imageView);
        }

        public void setTitle(String title){
            TextView textView = (TextView) mView.findViewById(R.id.bidder_username);

            textView.setText(title);
        }

        public void checIfAssigned(String jobID, final String key){

            DatabaseReference db = mAssigns.child(jobID);

            db.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild(key)){

                        mButton.setText("Assigned");
                        mButton.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_done_black_24dp,0,0,0);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }

                outRect.bottom = spacing; // item bottom

            } else {

                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)

                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
