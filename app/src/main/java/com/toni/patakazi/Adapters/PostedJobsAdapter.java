package com.toni.patakazi.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.toni.patakazi.R;
import com.toni.patakazi.model.responses.JobsResponse;
import com.toni.patakazi.utils.Utills;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toni on 12/22/17.
 */

public class PostedJobsAdapter extends RecyclerView.Adapter<PostedJobsAdapter.JobsViewHolder> {

    private Context ctx;
    private List<JobsResponse.Data> list ;

    public PostedJobsAdapter(Context ctx, List<JobsResponse.Data> list) {
        this.list = list;
        this.ctx = ctx;
        Utills.writeLog(PostedJobsAdapter.class, new Gson().toJson(list));
    }

    @Override
    public JobsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new JobsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.job_row, parent, false));
    }

    @Override
    public void onBindViewHolder(JobsViewHolder viewHolder, int position) {

        JobsResponse.Data model = list.get(position);
        viewHolder.setImage(ctx, model.getImage_url());
        viewHolder.setTitle(model.getTitle());
        viewHolder.setCharge(model.getCharges());

      //  viewHolder.checkJobIfClosed(jobID);

        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Intent singlePost = new Intent(getActivity(), SingleJobActiivity.class);
//                singlePost.putExtra("jobiD", jobID);
//                startActivity(singlePost);

            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
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

        public void setCharge(double amount) {

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
}
