package com.toni.patakazi.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.toni.patakazi.CompletedWorkers;
import com.toni.patakazi.Dialogs.CompletedAssignedDialog;
import com.toni.patakazi.R;
import com.toni.patakazi.model.Jobs;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toni on 3/1/17.
 */

public class PostedCompletedJobsAdapter extends RecyclerView.Adapter<PostedCompletedJobsAdapter.PostedCompletedViewHolder> {

    private Context ctx;
    private List<Jobs> listJobs = new ArrayList<>();

    public PostedCompletedJobsAdapter(Context ctx, List<Jobs> list) {

        this.ctx = ctx;
        this.listJobs = list;

    }

    @Override
    public PostedCompletedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PostedCompletedViewHolder(ctx, LayoutInflater.from(parent.getContext()).inflate(R.layout.completed_jobs_fragment_rows, parent, false));
    }

    @Override
    public void onBindViewHolder(final PostedCompletedViewHolder holder, int position) {

        if (holder != null) {

            final Jobs job = listJobs.get(position);

            holder.setImageCompleted(job.getImage());
            holder.setCompletedJobTitle(job.getTitle());

            //getJobKey
            DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Jobs");
            Query query = db.orderByChild("image").equalTo(job.getImage());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        Log.d("sadsds", ds.getKey());
                        String key = ds.getKey();

                        DatabaseReference mAssigns = FirebaseDatabase.getInstance().getReference().child("Confirms");
                        mAssigns.child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists())
                                    holder.setAssignedWorkers(String.valueOf(dataSnapshot.getChildrenCount()));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        DatabaseReference mCompleted = FirebaseDatabase.getInstance().getReference().child("Completed");
                        mCompleted.child(key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists())
                                    holder.setAssignedCompleted(String.valueOf(dataSnapshot.getChildrenCount()));


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            holder.completedAssigned.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //getJobKey
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Jobs");
                    Query query = db.orderByChild("image").equalTo(job.getImage());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                String jobKey = ds.getKey();

                                CompletedAssignedDialog completedAssignedDialog = new CompletedAssignedDialog(ctx,jobKey);
                                completedAssignedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                completedAssignedDialog.setCanceledOnTouchOutside(false);
                                completedAssignedDialog.show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

            holder.completedWorkers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //getJobKey
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Jobs");
                    Query query = db.orderByChild("image").equalTo(job.getImage());
                    query.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                String jobKey = ds.getKey();

                                Intent intent = new Intent(ctx, CompletedWorkers.class);
                                intent.putExtra("JOBKEY",jobKey);
                                ctx.startActivity(intent);

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });


        }

    }

    @Override
    public int getItemCount() {
        return listJobs.size();
    }

    public static class PostedCompletedViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private Context ctx;
        private ProgressBar mProgress;
        public ImageButton completedAssigned,completedWorkers;

        public PostedCompletedViewHolder(Context ctx, View itemView) {
            super(itemView);

            this.mView = itemView;
            this.ctx = ctx;

            mProgress = (ProgressBar) mView.findViewById(R.id.completedJobsProgress);

            completedAssigned = (ImageButton) mView.findViewById(R.id.completedWorkersAssignedbtn);
            completedWorkers = (ImageButton) mView.findViewById(R.id.completedWorkersCompletedbtn);
        }

        public void setImageCompleted(String url) {

            ImageView imageView = (ImageView) mView.findViewById(R.id.posteCompletedImageV);

            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            mProgress.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            mProgress.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .crossFade()
                    .into(imageView);

        }

        public void setCompletedJobTitle(String title) {

            TextView textView = (TextView) mView.findViewById(R.id.completedJobTitle);
            textView.setText(title);

        }

        public void setAssignedWorkers(String total) {

            TextView textView = (TextView) mView.findViewById(R.id.completedJobAssigned);
            textView.setText(total);

        }

        public void setAssignedCompleted(String total) {

            TextView textView = (TextView) mView.findViewById(R.id.completedJobComplete);
            textView.setText(total);

        }
    }
}
