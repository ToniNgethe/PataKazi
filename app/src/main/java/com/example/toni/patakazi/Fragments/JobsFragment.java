package com.example.toni.patakazi.Fragments;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.toni.patakazi.Helpers.Global;
import com.example.toni.patakazi.Helpers.GpsTracker;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.SingleJobActiivity;
import com.example.toni.patakazi.model.Jobs;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by toni on 2/8/17.
 */

public class JobsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String SINGLE_POST = "jobiD";
    private static final String TAG = JobsFragment.class.getSimpleName();
    private View myView;
    private RecyclerView rv;

    private DatabaseReference mJobs;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;
    private TextView indicator;

    private Query query = null;

    private String loc;

    private Location mLastLocation;

    private ImageView network_indicator;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    private double longitude;
    private double latitude;

    private static final int MY_PERMISSIONS_FINE_LOCATION = 102;
    private static final int MY_PERMISSIONS_COURSE_LOCATION = 103;


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.jobs_layout, container, false);

        //setupViews
        network_indicator = (ImageView) myView.findViewById(R.id.iv_jobs_indicator);
        rv = (RecyclerView) myView.findViewById(R.id.jobs_rv);
        indicator = (TextView) myView.findViewById(R.id.tv_jobsFragment);
        // Building the GoogleApi client
        buildGoogleApiClient();

        if (Global.isConnected(getActivity())) {
            displayLocation();
            network_indicator.setVisibility(View.GONE);

            getLocation();
            RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 2);

            rv.setLayoutManager(lm);
            rv.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(6), true));
            rv.setItemAnimator(new DefaultItemAnimator());

            //FIREBASE
            mAuth = FirebaseAuth.getInstance();
            mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
            mUsers = FirebaseDatabase.getInstance().getReference().child("Users");

            mJobs.keepSynced(true);
        } else {

            rv.setVisibility(View.GONE);
            indicator.setVisibility(View.GONE);

        }

        return myView;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        if (Global.isConnected(getActivity())) {
            loadData();
        }
    }

    private void getLocation() {


        GpsTracker gps = new GpsTracker(getActivity());

        if (gps.canGetLocation()) {

            createLocationRequest();
            // displayLocation();
            if (Geocoder.isPresent()) {
                Geocoder geocoder;
                List<Address> addresses;

                // displayLocation();

                geocoder = new Geocoder(getActivity(), Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    // address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()


                    loc = addresses.get(0).getLocality();
                    // Log.d(TAG, String.valueOf(longitude) + " ," + String.valueOf(latitude));
                    Log.d(TAG, "CITY :" + loc);

                    Toast.makeText(getActivity(), "Current Location :" + loc, Toast.LENGTH_SHORT).show();


                } catch (IOException e) {

                    Log.d(TAG, e.getMessage());
                } catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Current Location : Unknown location, try restarting app", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getActivity(), "Current Location : Unknown location, try restarting app", Toast.LENGTH_SHORT).show();
            }
        } else {
            gps.showSettingsAlert();
        }

    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private void loadData() {


        query = mJobs.orderByChild("city").equalTo(loc);

        FirebaseRecyclerAdapter<Jobs, JobsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Jobs, JobsViewHolder>(

                Jobs.class,
                R.layout.job_row,
                JobsViewHolder.class,
                query

        ) {
            @Override
            protected void populateViewHolder(JobsViewHolder viewHolder, Jobs model, int position) {

                //progressBar.setVisibility(View.GONE);

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
                } else {

                    indicator.setVisibility(View.VISIBLE);
                    rv.setVisibility(View.GONE);
                }

            }
        };

        rv.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();

    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSIONS_FINE_LOCATION);
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, MY_PERMISSIONS_COURSE_LOCATION);
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            // startLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {

                    Toast.makeText(getActivity(), "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }

                break;

            case MY_PERMISSIONS_COURSE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    Toast.makeText(getActivity(), "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    getActivity().finish();

                }

                break;

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

                    if (dataSnapshot.exists()) {

                        if (dataSnapshot.child("status").getValue().toString() == String.valueOf(1)) {

                            checkedOrNot.setImageResource(R.mipmap.ic_close_black_24dp);

                        } else {

                            checkedOrNot.setImageResource(R.mipmap.chat_icon_grid);
                        }
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
