package com.example.toni.patakazi.Fragments;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.model.Skills;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by toni on 2/8/17.
 */

public class WorkersFragment extends Fragment {

    private View mView;
    private RecyclerView mRecycleView;

    private FirebaseAuth mAuth;
    private DatabaseReference mSkills;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.workers_fragment,container,false);

        //setting up recycleview
        mRecycleView = (RecyclerView) mView.findViewById(R.id.workersRecycleView);
        RecyclerView.LayoutManager lm = new GridLayoutManager(getActivity(),2);

        mRecycleView.setLayoutManager(lm);
        mRecycleView.addItemDecoration(new WorkersFragment.GridSpacingItemDecoration(3, dpToPx(6), true));
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        //setting up firebase
        mAuth = FirebaseAuth.getInstance();

        mSkills = FirebaseDatabase.getInstance().getReference().child("Skills");

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<Skills,WorkersFragmentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Skills, WorkersFragmentViewHolder>(

                Skills.class,
                R.layout.workers_fragment_row,
                WorkersFragmentViewHolder.class,
                mSkills

        ) {
            @Override
            protected void populateViewHolder(WorkersFragmentViewHolder viewHolder, Skills model, int position) {

                viewHolder.setWorkerImage(getActivity(),model.getImage());
                viewHolder.setWorkerTitle(model.getTitle());
                viewHolder.setWorkerLocation(model.getLocation());
                viewHolder.setPrice(model.getCharges());

            }
        };

        mRecycleView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    public static class WorkersFragmentViewHolder extends RecyclerView.ViewHolder{

        private View mView;

        public WorkersFragmentViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setWorkerTitle(String title){

            TextView textView = (TextView) mView.findViewById(R.id.skillTitle);
            textView.setText(title);
        }

        public void setPrice(long price){

            TextView textView = (TextView) mView.findViewById(R.id.workerCharges);
            textView.setText(String.valueOf(price) +"" + " ksh/hr");
        }

        public void setWorkerLocation(String location){

            TextView textView = (TextView) mView.findViewById(R.id.workerLocation);
            textView.setText(location);

        }

        public void setWorkerImage(final Context ctx, final String url){

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
