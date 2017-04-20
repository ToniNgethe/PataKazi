package com.example.toni.patakazi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.Dialogs.AddChoiceDialog;
import com.example.toni.patakazi.Fragments.AccountFragment;
import com.example.toni.patakazi.Fragments.JobsFragment;
import com.example.toni.patakazi.Fragments.WorkersFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class MainPanel extends AppCompatActivity {

    private static final String TAG = MainPanel.class.getSimpleName();
    private static final int MY_PERMISSIONS_FINE_LOCATION = 100;
    private static final int MY_PERMISSIONS_COURSE_LOCATION = 200;
    // index to identify current nav menu item
    public static int navItemIndex = 0;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private BottomBar bottomBar;
    private FrameLayout frameLayout;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtWebsite;
    private DatabaseReference mUsers;
    private Handler mHandler;
    private Boolean exit = false;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;


    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overall);
//        buildGoogleApiClient();
//        createLocationRequest();

        initiLizeFirebase();
        displayLocation();
        checkUsre();
        setUpViews();
        // load nav menu header data
        loadNavHeader();
        // initializing navigation menu
        setUpNavigationView();
        initiateTabListener();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            loadHomeFragment();
        }

    }


    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }


    private void checkUsre() {

        if (mAuth.getCurrentUser() != null) {


            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                        startActivity(new Intent(MainPanel.this, SetupActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    private void initiateTabListener() {

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {

                switch (tabId) {

                    case R.id.tab_jobs:

                        navItemIndex = 0;
                        loadHomeFragment();

                        break;
                    case R.id.tab_workers:

                        navItemIndex = 1;
                        loadHomeFragment();

                        break;
                    case R.id.tab_account:

                        navItemIndex = 2;
                        loadHomeFragment();

                        break;

                }

            }
        });

    }

    private void loadNavHeader() {

        if (mAuth.getCurrentUser() != null) {

            mUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        txtName.setText(dataSnapshot.child("user").getValue().toString());
                        txtWebsite.setText(mAuth.getCurrentUser().getEmail());

                        // Loading profile image
                        Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue())
                                .crossFade()
                                .thumbnail(0.5f)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .error(R.mipmap.error_network)
                                .into(imgProfile);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    private void setUpNavigationView() {

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_postedJobs:

                        startActivity(new Intent(MainPanel.this, MyPostedJobsActivity.class));
                        drawer.closeDrawers();

                        break;
                    case R.id.nav_photos:

                        startActivity(new Intent(MainPanel.this, JobsDue.class));
                        drawer.closeDrawers();

                        break;

                    case R.id.nav_about_us:

                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_privacy_policy:

                        drawer.closeDrawers();
                        return true;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);


                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }


    private void setUpViews() {

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        frameLayout = (FrameLayout) findViewById(R.id.contentContainer);
        toolbar = (Toolbar) findViewById(R.id.toolbar2);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtWebsite = (TextView) navHeader.findViewById(R.id.website);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);
        mHandler = new Handler();

    }

    private void initiLizeFirebase() {

        mAuth = FirebaseAuth.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference("Users");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //user is not logged in
                if (firebaseAuth.getCurrentUser() == null) {

                    startActivity(new Intent(MainPanel.this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                }

            }
        };


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add:

                AddChoiceDialog addChoiceDialog = new AddChoiceDialog(this);
                addChoiceDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                addChoiceDialog.show();

                break;

            case R.id.action_logout:

                if (mAuth != null) {
                    mAuth.signOut();

                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public void onBackPressed() {

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        if (navItemIndex != 0) {
            navItemIndex = 0;
            loadHomeFragment();
        }

        if (exit) {

            finish(); // finish activity

        } else {
            Toast.makeText(this, "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();

            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkPlayServices();

    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    private void loadHomeFragment() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments

                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.contentContainer, fragment);
                fragmentTransaction.commitAllowingStateLoss();

            }
        };

        if (runnable != null) {

            mHandler.post(runnable);

        }

    }

    private Fragment getHomeFragment() {

        switch (navItemIndex) {
            case 0:

                JobsFragment jobsFragment = new JobsFragment();
                return jobsFragment;

            case 1:

                WorkersFragment workersFragment = new WorkersFragment();
                return workersFragment;

            case 2:

                AccountFragment accountFragment = new AccountFragment();
                return accountFragment;
            default:
                return new JobsFragment();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        AccountFragment accountFragment = new AccountFragment();
//
//        accountFragment.onActivityResult(requestCode,resultCode,data);

    }


    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

    }

    /**
     * Method to display the location on UI
     */
    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

//            mLastLocation = LocationServices.FusedLocationApi
//                    .getLastLocation(mGoogleApiClient);
//
//            if (mLastLocation != null) {
//                latitude = mLastLocation.getLatitude();
//                longitude = mLastLocation.getLongitude();


            Log.d(TAG, "Location services allowed");


        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                }, MY_PERMISSIONS_FINE_LOCATION);
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                }, MY_PERMISSIONS_COURSE_LOCATION);
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "Permission requested was a success");

//                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        mLastLocation = LocationServices.FusedLocationApi
//                                .getLastLocation(mGoogleApiClient);
//
//                        if (mLastLocation != null) {
//                            latitude = mLastLocation.getLatitude();
//                            longitude = mLastLocation.getLongitude();
//
//
//                        } else {
//
//                            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_LONG).show();
//                        }
//                    }

                } else {

                    Toast.makeText(MainPanel.this, "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();

                }

                break;

            case MY_PERMISSIONS_COURSE_LOCATION:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    Log.d(TAG, "Permission requested was a success");
//                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        mLastLocation = LocationServices.FusedLocationApi
//                                .getLastLocation(mGoogleApiClient);
//
//                        if (mLastLocation != null) {
//
//                            latitude = mLastLocation.getLatitude();
//                            longitude = mLastLocation.getLongitude();
//
//
//                        } else {
//
//                            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled on the device", Toast.LENGTH_LONG).show();
//                        }
//                    }

                } else {

                    Toast.makeText(MainPanel.this, "This app requires location permissions to be granted", Toast.LENGTH_SHORT).show();
                    finish();

                }

                break;

        }
    }

}
