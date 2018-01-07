package com.toni.patakazi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.toni.patakazi.ui.intro_ui.LoginActivity;
import com.toni.patakazi.Fragments.myjobs.OpenJobsFragment;
import com.toni.patakazi.Fragments.myjobs.PostedAssignedJobs;
import com.toni.patakazi.Fragments.myjobs.PostedCompletedJobs;
import com.toni.patakazi.ViewPagerHelper.MyPostedJObsViewPAdapter;
import com.google.firebase.auth.FirebaseAuth;

public class MyPostedJobsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posted_jobs);
        setUpViews();
    }

    private void setUpViews() {

        mToolbar = (Toolbar) findViewById(R.id.MyPostedJobsToolbar);

        //set toolbar...
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My posted jobs");

        mViewPager = (ViewPager) findViewById(R.id.MyPostedJobsViewPager);
        //setup viewpager
        setUpViewPager();

        mTabLayout = (TabLayout) findViewById(R.id.MyPostedJobsTab);
        //setupTab with viewPager
        mTabLayout.setupWithViewPager(mViewPager);


        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if (firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(MyPostedJobsActivity.this,LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

            }
        };

    }

    private void setUpViewPager() {
        mHandler = new Handler();


        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                MyPostedJObsViewPAdapter myPostedJObsViewPAdapter = new MyPostedJObsViewPAdapter(getSupportFragmentManager());
                myPostedJObsViewPAdapter.addFragment(new OpenJobsFragment(),"OPEN");
                myPostedJObsViewPAdapter.addFragment(new PostedAssignedJobs(),"ASSIGNED");
                myPostedJObsViewPAdapter.addFragment(new PostedCompletedJobs(),"COMPLETED");

                mViewPager.setAdapter(myPostedJObsViewPAdapter);

            }
        };

        if (runnable != null)
            mHandler.post(runnable);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth != null){
            mAuth.addAuthStateListener(mAuthStateListener);
        }
    }
}
