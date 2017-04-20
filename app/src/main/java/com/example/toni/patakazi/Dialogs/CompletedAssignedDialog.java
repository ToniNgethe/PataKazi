package com.example.toni.patakazi.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
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
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toni on 3/2/17.
 */

public class CompletedAssignedDialog extends Dialog {

    private static final String TAG = CompletedAssignedDialog.class.getSimpleName();
    private Context ctx;
    private String jobKey;
    private RecyclerView mRv;
    private ProgressBar progressBar;
    private DatabaseReference mJobs;
    private DatabaseReference mConfirms;
    private DatabaseReference mUsers;
    private FirebaseAuth mAurth;

    public CompletedAssignedDialog(Context context, String jobKey) {
        super(context);

        this.ctx = context;
        this.jobKey = jobKey;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.completed_assigned_dialog);

        //set up views....
        mRv = (RecyclerView) findViewById(R.id.completedAssignedRv);
        progressBar = (ProgressBar) findViewById(R.id.completedAssignedDialogProgress);

        LinearLayoutManager lm = new LinearLayoutManager(ctx);
        lm.setStackFromEnd(true);
        lm.setReverseLayout(true);

        mRv.setLayoutManager(lm);

        ImageButton imageButton = (ImageButton) findViewById(R.id.completed_cancel_btn);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //setup firebase....
        mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");
        mConfirms = FirebaseDatabase.getInstance().getReference().child("Confirms");
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAurth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        final List<Users> jobs = new ArrayList<>();

        //get confirmed people....
        mConfirms.child(jobKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                jobs.clear();

                for (DataSnapshot singleShot : dataSnapshot.getChildren()){



                    final String userId = singleShot.getKey();

                    mUsers.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Log.d(TAG,dataSnapshot.getKey() + "user " + userId);

                            jobs.add(dataSnapshot.getValue(Users.class));

                            CompletedAssignedAdapter completedAssignedAdapter = new CompletedAssignedAdapter(ctx,jobs);
                            mRv.setAdapter(completedAssignedAdapter);
                            completedAssignedAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);

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
    }

    public static class CompletedAssignedAdapter extends RecyclerView.Adapter<CompletedAssignedViewHolder>{

        private Context ctx;
        private List<Users> userList = new ArrayList<>();

        public CompletedAssignedAdapter(Context ctx, List<Users> jobs) {

            this.ctx = ctx;
            this.userList = jobs;

        }

        @Override
        public CompletedAssignedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.completed_assigned_dialog_row,parent,false);

            return new CompletedAssignedViewHolder(ctx,v);

        }

        @Override
        public void onBindViewHolder(CompletedAssignedViewHolder holder, int position) {

            if (holder != null){

                Users users = userList.get(position);

                holder.setCompletedImageView(users.getImage());
                holder.setCompletedAssTitle(users.getUser());
                holder.setCompletedAssNumber(users.getNumber());

            }
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }
    }

    public static  class CompletedAssignedViewHolder extends RecyclerView.ViewHolder{

        private View mView ;
        private Context ctx;
        private ProgressBar progressBar;

        public CompletedAssignedViewHolder(Context ctx, View itemView) {
            super(itemView);

            mView = itemView;
            this.ctx = ctx;
            progressBar = (ProgressBar) mView.findViewById(R.id.completedAssDialogProgress);
        }

        public void setCompletedImageView(String url){
            final ImageView imageView = (ImageView) mView.findViewById(R.id.completedAssignedDialogImage);

            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            imageView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            imageView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                            return false;
                        }
                    })
                    .crossFade()
                    .into(imageView);
        }

        public void setCompletedAssTitle(String title){

            TextView textView = (TextView) mView.findViewById(R.id.completedAssDialogUsername);
            textView.setText(title);

        }

        public void setCompletedAssNumber(String title){
            TextView textView = (TextView) mView.findViewById(R.id.completedAssDialognumber);
            textView.setText(title);
        }

    }

}

