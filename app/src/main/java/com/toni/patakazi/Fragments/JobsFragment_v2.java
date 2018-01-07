package com.toni.patakazi.Fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.toni.patakazi.Adapters.PostedJobsAdapter;
import com.toni.patakazi.Api.ApiClient;
import com.toni.patakazi.Api.ErrorUtils;
import com.toni.patakazi.R;
import com.toni.patakazi.model.responses.JobsResponse;
import com.toni.patakazi.ui.intro_ui.GetLocationActivity;
import com.toni.patakazi.utils.Global;
import com.toni.patakazi.utils.Utills;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by toni on 1/7/18.
 */

public class JobsFragment_v2 extends Fragment {


    @BindView(R.id.iv_jobs_indicator2)
    ImageView ivJobsIndicator2;
    @BindView(R.id.imageView8)
    ImageView imageView8;
    @BindView(R.id.tv_jobsFragment)
    TextView tvJobsFragment;
    @BindView(R.id.linearlayout_jobs)
    LinearLayout linearlayoutJobs;
    Unbinder unbinder;
    @BindView(R.id.swipe)
    SwipeRefreshLayout swipe;
    @BindView(R.id.rv)
    RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jobs_v2_layout, container, false);
        unbinder = ButterKnife.bind(this, view);

        // check network
        if (Global.isConnected(getActivity())) {

            setUpSwipe();
            setUpRv();
            loadData();

        } else {
            swipe.setVisibility(View.GONE);
            ivJobsIndicator2.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private String userLocation() {

        SharedPreferences prefs = getActivity().getSharedPreferences(GetLocationActivity.MY_PREFS_NAME, getActivity().MODE_PRIVATE);
        String restoredText = prefs.getString("location", null);
        if (restoredText != null) {
            return prefs.getString("location", "location not found");//"No name defined" is the default value.
            //  Toast.makeText(getActivity(), "Current Location :" + loc, Toast.LENGTH_SHORT).show();
        } else {
            // Toast.makeText(getActivity(), "Current Location :" + loc, Toast.LENGTH_SHORT).show();
            return "Nairobi";
        }

    }

    private void loadData() {

        Utills.writeLog(JobsFragment_v2.class, "Location: " + userLocation());

        Call<JobsResponse> jobs = ApiClient.apiInterface().getAllJobs();
        jobs.enqueue(new Callback<JobsResponse>() {
            @Override
            public void onResponse(@NonNull Call<JobsResponse> call, @NonNull Response<JobsResponse> response) {

                swipe.setRefreshing(false);
                if (response.code() == 200) {

                    if (response.body().getStatus().equals("00")) {

                        if (response.body().getData().size() > 0) {

                            PostedJobsAdapter postedJobsAdapter = new PostedJobsAdapter(getActivity(), response.body().getData());
                            rv.setAdapter(postedJobsAdapter);
                            postedJobsAdapter.notifyDataSetChanged();


                        } else {
                            linearlayoutJobs.setVisibility(View.VISIBLE);
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
                swipe.setRefreshing(false);
                Utills.showErrorToast(new ErrorUtils().parseOnFailure(t));
            }
        });

    }


    private void setUpRv() {

        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(), 2);
        rv.setLayoutManager(lm);
        rv.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(4), true));
        rv.setItemAnimator(new DefaultItemAnimator());

    }

    private void setUpSwipe() {

        swipe.setRefreshing(true);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                // reload data...
                loadData();

            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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
