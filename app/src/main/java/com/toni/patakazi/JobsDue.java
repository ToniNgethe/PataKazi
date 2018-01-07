package com.toni.patakazi;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.toni.patakazi.Fragments.AssignedJobFragment;
import com.toni.patakazi.Fragments.BiddedJobsFragment;
import com.toni.patakazi.Fragments.CompletedJobsFragment;
import com.toni.patakazi.ViewPagerHelper.JobsDueViewPagerAdapter;

public class JobsDue extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        setContentView(R.layout.activity_jobs_due);

        setUpViews();
    }

    private void setUpViews() {

        mToolbar = (Toolbar) findViewById(R.id.jobsDueToolbar);

        //set toolbar...
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Jobs due");

        mViewPager = (ViewPager) findViewById(R.id.jobsDueViewPager);
        //setup viewpager
        setUpViewPager();

        mTabLayout = (TabLayout) findViewById(R.id.jobsDueTabLayout);
        //setupTab with viewPager
        mTabLayout.setupWithViewPager(mViewPager);

    }

    private void setUpViewPager() {

        JobsDueViewPagerAdapter jobsDueViewPagerAdapter = new JobsDueViewPagerAdapter(getSupportFragmentManager());
        jobsDueViewPagerAdapter.addFragment(new BiddedJobsFragment(),"Bidded jobs");
        jobsDueViewPagerAdapter.addFragment(new AssignedJobFragment(),"Assigned Jobs");
        jobsDueViewPagerAdapter.addFragment(new CompletedJobsFragment(),"Completed Jobs");

        mViewPager.setAdapter(jobsDueViewPagerAdapter);

    }
}
