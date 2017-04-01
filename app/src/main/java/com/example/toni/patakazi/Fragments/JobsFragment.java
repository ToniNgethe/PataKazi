package com.example.toni.patakazi.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.SingleJobActiivity;
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
 * Created by toni on 2/8/17.
 */

public class JobsFragment extends Fragment {

    private static final String SINGLE_POST = "jobiD";
    private static final String TAG = JobsFragment.class.getSimpleName();
    private View myView;
    private RecyclerView rv;

    private DatabaseReference mJobs;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private String userLocation;

    private TextView indicator;

    Query query = null;

    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.jobs_layout, container, false);

        //setupViews
        rv = (RecyclerView) myView.findViewById(R.id.jobs_rv);
        indicator = (TextView) myView.findViewById(R.id.tv_jobsFragment);
        indicator.setVisibility(View.GONE);
        progressBar = (ProgressBar) myView.findViewById(R.id.progressBar_jobFragment);

        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 2);

        rv.setLayoutManager(lm);
        rv.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(6), true));
        rv.setItemAnimator(new DefaultItemAnimator());

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        mJobs.keepSynced(true);

        return myView;
    }


    @Override
    public void onStart() {
        super.onStart();

        //get user location
        if (mAuth.getCurrentUser() != null) {
            mUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //  Log.d(TAG, dataSnapshot.getValue().toString());

                    if (dataSnapshot.child("location").exists()) {

                        userLocation = dataSnapshot.child("location").getValue().toString();
                        indicator.setVisibility(View.GONE);

                        loadData(userLocation);

                    } else {

                        //  Toast.makeText(getActivity(),"From jobs : User "+ mAuth.getCurrentUser().getUid()+"not found", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "From jobs : User" + mAuth.getCurrentUser().getUid() + "not found");

                        rv.setVisibility(View.GONE);
                        indicator.setVisibility(View.GONE);
                        indicator.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Log.d(TAG, databaseError.getMessage());

                }
            });
        }
    }

    private void loadData(String userLocation) {


        query = mJobs.orderByChild("location").equalTo(userLocation);


        FirebaseRecyclerAdapter<Jobs, JobsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Jobs, JobsViewHolder>(

                Jobs.class,
                R.layout.job_row,
                JobsViewHolder.class,
                query

        ) {
            @Override
            protected void populateViewHolder(JobsViewHolder viewHolder, Jobs model, int position) {

                progressBar.setVisibility(View.GONE);

                if (model != null) {

                    indicator.setVisibility(View.GONE);

                    final String jobID = getRef(position).getKey();

                    viewHolder.setImage(getActivity(), model.getImage());
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setCharge(model.getCharges());

                    viewHolder.checkJobIfClosed(jobID);

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent singlePost = new Intent(getActivity(), SingleJobActiivity.class);
                            singlePost.putExtra("jobiD", jobID);
                            startActivity(singlePost);

                        }
                    });
                }else {

                    indicator.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }

            }
        };

        rv.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();

    }


    public static class JobsViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public ImageView checkedOrNot;
        private ProgressBar mProgressBar;
        private DatabaseReference mJobs;

        public JobsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            checkedOrNot = (ImageView) mView.findViewById(R.id.overflow);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.jobsProgressBar);

            mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");

        }

        public void setImage(final Context ctx, final String url) {

            mProgressBar.setVisibility(View.VISIBLE);
            final ImageView imageView = (ImageView) mView.findViewById(R.id.thumbnail);

            Glide.with(ctx).load(url)
                    .crossFade()
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            mProgressBar.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .error(R.mipmap.loading)
                    .into(imageView);

        }

        public void setTitle(String title) {
            TextView textView = (TextView) mView.findViewById(R.id.title);
            textView.setText(title);
        }

        public void setCharge(long amount) {

            TextView textView1 = (TextView) mView.findViewById(R.id.count);
            textView1.setText(String.valueOf(amount) + " Ksh/hr");

        }

        public void checkJobIfClosed(String jobId) {

            mJobs.child(jobId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child("status").getValue().toString() == String.valueOf(1)) {

                        checkedOrNot.setImageResource(R.mipmap.ic_close_black_24dp);

                    } else {

                        checkedOrNot.setImageResource(R.mipmap.chat_icon_grid);
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
