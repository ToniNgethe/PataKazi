package com.toni.patakazi.Fragments;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.toni.patakazi.Adapters.PostedJobsAdapter;
import com.toni.patakazi.Api.ApiClient;
import com.toni.patakazi.Api.ErrorUtils;
import com.toni.patakazi.model.responses.JobsResponse;
import com.toni.patakazi.ui.intro_ui.GetLocationActivity;
import com.toni.patakazi.utils.Global;
import com.toni.patakazi.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.toni.patakazi.utils.Utills;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by toni on 2/8/17.
 */

public class JobsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String SINGLE_POST = "jobiD";
    private static final String TAG = JobsFragment.class.getSimpleName();
    private View myView;
    private RecyclerView rv;
    private LinearLayout lm;

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

    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        myView = inflater.inflate(R.layout.jobs_layout, container, false);
        //setupViews
        network_indicator = (ImageView) myView.findViewById(R.id.iv_jobs_indicator);
        rv = (RecyclerView) myView.findViewById(R.id.jobs_rv);
        indicator = (TextView) myView.findViewById(R.id.tv_jobsFragment);
        lm = (LinearLayout) myView.findViewById(R.id.linearlayout_jobs);
        swipeRefreshLayout = myView.findViewById(R.id.swipe);
        lm.setVisibility(View.GONE);
        // Building the GoogleApi client
        buildGoogleApiClient();


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        if (Global.isConnected(getActivity())) {
            Utills.writeLog(JobsFragment.class, "Connected.....");
            displayLocation();
            network_indicator.setVisibility(View.GONE);
            getLocation();

            RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 2);
            rv.setLayoutManager(lm);
            rv.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(4), true));
            rv.setItemAnimator(new DefaultItemAnimator());

            loadData();

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
    }

    private void getLocation() {

        SharedPreferences prefs = getActivity().getSharedPreferences(GetLocationActivity.MY_PREFS_NAME, getActivity().MODE_PRIVATE);
        String restoredText = prefs.getString("location", null);
        if (restoredText != null) {
            loc = prefs.getString("location", "location not found");//"No name defined" is the default value.
            //  Toast.makeText(getActivity(), "Current Location :" + loc, Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(getActivity(), "Current Location :" + loc, Toast.LENGTH_SHORT).show();
        }

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

        Utills.writeLog(JobsFragment.class,"Loading data.........");

        Call<JobsResponse> jobs = ApiClient.apiInterface().getAllJobs();
        jobs.enqueue(new Callback<JobsResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobsResponse> call, @NonNull Response<JobsResponse> response) {

                swipeRefreshLayout.setRefreshing(false);
                if (response.code() == 200) {

                    if (response.body().getStatus().equals("00")) {

                        if (response.body().getData().size() > 0) {

                            Utills.writeLog(JobsFragment.class, "entered into adapter");
                            lm.setVisibility(View.GONE);
                            rv.setVisibility(View.VISIBLE);

                            PostedJobsAdapter postedJobsAdapter = new PostedJobsAdapter(getActivity(), response.body().getData());
                            rv.setAdapter(postedJobsAdapter);
                            postedJobsAdapter.notifyDataSetChanged();


                        } else {
                            lm.setVisibility(View.VISIBLE);
                        }

                    } else {
                        Utills.showErrorToast(getString(R.string.global_error));
                    }

                } else {
                    Utills.showErrorToast(getString(R.string.global_error));
                }

            }

            @Override
            public void onFailure(Call<JobsResponse> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Utills.showErrorToast(new ErrorUtils().parseOnFailure(t));
            }
        });

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
