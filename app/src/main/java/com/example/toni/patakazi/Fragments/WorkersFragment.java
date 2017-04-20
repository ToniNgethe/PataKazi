package com.example.toni.patakazi.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.Helpers.Global;
import com.example.toni.patakazi.Helpers.GpsTracker;
import com.example.toni.patakazi.Helpers.SingleShotLocationProvider;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.SingleWorkerActivity;
import com.example.toni.patakazi.model.Skills;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by toni on 2/8/17.
 */

public class WorkersFragment extends Fragment {

    public static final String SKILL_KEY = "SKILL_KEY";
    private static final String TAG = WorkersFragment.class.getSimpleName();
    private View mView;
    private RecyclerView mRecycleView;

    private FirebaseAuth mAuth;
    private DatabaseReference mSkills;
    private LinearLayout linearLayout;
    private Button more;
    private String loc;
    private Query query;

    private ImageView network;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.workers_fragment, container, false);

        //views
        linearLayout = (LinearLayout) mView.findViewById(R.id.holder_workersFragment);
        more = (Button) mView.findViewById(R.id.button_workersFragment);
        linearLayout.setVisibility(View.GONE);
        network = (ImageView) mView.findViewById(R.id.iv_workersFragment_net);
        //setting up recycleview
        mRecycleView = (RecyclerView) mView.findViewById(R.id.workersRecycleView);
        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 2);

        if (Global.isConnected(getActivity())) {
            network.setVisibility(View.GONE);
            getLocation();

            mRecycleView.setLayoutManager(lm);
            mRecycleView.addItemDecoration(new WorkersFragment.GridSpacingItemDecoration(3, dpToPx(6), true));
            mRecycleView.setItemAnimator(new DefaultItemAnimator());

            //setting up firebase
            mAuth = FirebaseAuth.getInstance();
            mSkills = FirebaseDatabase.getInstance().getReference().child("Skills");

        } else {

            linearLayout.setVisibility(View.GONE);
            mRecycleView.setVisibility(View.GONE);

        }

        return mView;
    }

    private void getLocation() {


        GpsTracker gps = new GpsTracker(getActivity());

        if (gps.canGetLocation()) {

            if (Geocoder.isPresent()) {

                Geocoder geocoder;
                List<Address> addresses;

                geocoder = new Geocoder(getActivity(), Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    // address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


                    loc = addresses.get(0).getLocality();

                    Log.d(TAG, addresses.get(0).getLocality());


                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (Global.isConnected(getActivity())) {

            //queery
            query = mSkills.orderByChild("city").equalTo(loc);

            FirebaseRecyclerAdapter<Skills, WorkersFragmentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Skills, WorkersFragmentViewHolder>(

                    Skills.class,
                    R.layout.workers_fragment_row,
                    WorkersFragmentViewHolder.class,
                    query

            ) {
                @Override
                protected void populateViewHolder(WorkersFragmentViewHolder viewHolder, Skills model, int position) {


                    if (model != null) {

                        linearLayout.setVisibility(View.GONE);

                        final String skillKey = getRef(position).getKey();

                        viewHolder.setWorkerImage(getActivity(), model.getImage());
                        viewHolder.setWorkerTitle(model.getTitle());
                        viewHolder.setWorkerLocation(model.getLocation());
                        viewHolder.setPrice(model.getCharges());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent skillSingleView = new Intent(getActivity(), SingleWorkerActivity.class);
                                skillSingleView.putExtra(SKILL_KEY, skillKey);
                                startActivity(skillSingleView);

                            }
                        });
                    } else {

                        mRecycleView.setVisibility(View.GONE);
                        linearLayout.setVisibility(View.VISIBLE);

                    }
                }
            };

            mRecycleView.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public static class WorkersFragmentViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        public WorkersFragmentViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setWorkerTitle(String title) {

            TextView textView = (TextView) mView.findViewById(R.id.skillTitle);
            textView.setText(title);
        }

        public void setPrice(long price) {

            TextView textView = (TextView) mView.findViewById(R.id.workerCharges);
            textView.setText(String.valueOf(price) + "" + " ksh/hr");
        }

        public void setWorkerLocation(String location) {

            TextView textView = (TextView) mView.findViewById(R.id.workerLocation);
            textView.setText(location);

        }

        public void setWorkerImage(final Context ctx, final String url) {

            final ImageView imageView = (ImageView) mView.findViewById(R.id.skillImageView);

            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .into(imageView);

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
