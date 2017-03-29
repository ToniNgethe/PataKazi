package com.example.toni.patakazi.Dialogs;

import android.app.Activity;
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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by toni on 2/25/17.
 */

public class ConfirmedAssigned extends Dialog {

    private Context a;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ImageButton cancel;

    private DatabaseReference mAssigns;
    private DatabaseReference mUsers;
    private DatabaseReference mConfirms;
    private FirebaseAuth mAuth;

    private String jobId;

    public ConfirmedAssigned(Context context, String jobId) {
        super(context);
        a = context;
        this.jobId = jobId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmed_assigned_dialog);

        //views
        mProgressBar = (ProgressBar) findViewById(R.id.progress_dialog_assigned);
        mRecyclerView = (RecyclerView) findViewById(R.id.confirmed_pogressAssigned);
        cancel = (ImageButton) findViewById(R.id.confirmedCancelBtn);

        LinearLayoutManager lm = new LinearLayoutManager(a);
        lm.setReverseLayout(true);
        lm.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(lm);

        //remove rv
        mRecyclerView.setVisibility(View.GONE);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mAssigns = FirebaseDatabase.getInstance().getReference().child("Assigns");
        mAssigns.keepSynced(true);

        mConfirms = FirebaseDatabase.getInstance().getReference().child("Confirms");

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        final List<Users> list = new ArrayList<>();

        //get users
        final DatabaseReference db = mConfirms.child(jobId);

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                list.clear();

                for (DataSnapshot singleShot : dataSnapshot.getChildren()) {
                    String key = singleShot.getKey();

                    DatabaseReference dbs = mUsers.child(key);

                    dbs.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            list.add(dataSnapshot.getValue(Users.class));

                            ConfirmedAdapter confirmedAdapter = new ConfirmedAdapter(a, list);

                            mRecyclerView.setAdapter(confirmedAdapter);
                            confirmedAdapter.notifyDataSetChanged();
                            mProgressBar.setVisibility(View.GONE);
                            mRecyclerView.setVisibility(View.VISIBLE);

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

    public class ConfirmedAdapter extends RecyclerView.Adapter<ConfirmedViewHolder> {

        private Context ctx;
        private List<Users> list;

        public ConfirmedAdapter(Context a, List<Users> list) {

            ctx = a;
            this.list = list;

        }

        @Override
        public ConfirmedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ConfirmedViewHolder(ctx, LayoutInflater.from(parent.getContext()).inflate(R.layout.confirmed_assigned_dialog_row, parent, false));
        }

        @Override
        public void onBindViewHolder(ConfirmedViewHolder holder, int position) {

            if (holder != null) {

                Users users = list.get(position);

                holder.setConfirmDialogTitle(users.getUser());
                holder.setConfirmDialogNumber(users.getNumber());
                holder.setConfirmedDialogImageView(users.getImage());

            }

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    public static class ConfirmedViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private Context ctx;
        private ProgressBar mProgressBar;

        public ConfirmedViewHolder(Context ctx, View itemView) {
            super(itemView);

            this.ctx = ctx;
            this.mView = itemView;

            mProgressBar = (ProgressBar) mView.findViewById(R.id.confirmedDialogProgress);
        }

        public void setConfirmDialogTitle(String title) {
            TextView textView = (TextView) mView.findViewById(R.id.postAssignedDialogUsername);
            textView.setText(title);
        }

        public void setConfirmDialogNumber(String no) {
            TextView textView = (TextView) mView.findViewById(R.id.postAssignedDialogNumber);
            textView.setText(no);
        }

        public void setConfirmedDialogImageView(String url) {
            final ImageView imageView = (ImageView) mView.findViewById(R.id.postAssignedDialogImage);

            Glide.with(ctx).load(url)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .error(R.mipmap.loading)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            imageView.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            imageView.setVisibility(View.VISIBLE);
                            mProgressBar.setVisibility(View.GONE);

                            return false;
                        }
                    })
                    .crossFade()
                    .into(imageView);
        }
    }
}


