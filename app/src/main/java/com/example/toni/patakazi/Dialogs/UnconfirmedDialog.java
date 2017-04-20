package com.example.toni.patakazi.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * Created by toni on 2/28/17.
 */
public class UnconfirmedDialog extends Dialog {

    private Context a;
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ImageButton cancel;

    private DatabaseReference mAssigns;
    private DatabaseReference mUsers;
    private DatabaseReference mConfirms;
    private FirebaseAuth mAuth;

    private String jobId;

    public UnconfirmedDialog(Context context, String jobId) {
        super(context);
        a = context;
        this.jobId = jobId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uncornfirmed_assigned_dialog);

        //views
        mProgressBar = (ProgressBar) findViewById(R.id.progress_dialog_unConfirmed_assigned);
        mRecyclerView = (RecyclerView) findViewById(R.id.unconfirmed_pogressAssigned);
        cancel = (ImageButton) findViewById(R.id.unconfirmedCancelBtn);

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

        list.clear();
        //get users
        final DatabaseReference db = mAssigns.child(jobId);

        //confirms
        final DatabaseReference mc = mConfirms.child(jobId);

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleShot : dataSnapshot.getChildren()) {

                    final String key = singleShot.getKey();

                    //check if user exists in Confirms

                    mc.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //only execute if user has not confirmed job

                            if (!dataSnapshot.exists()) {

                                DatabaseReference dbs = mUsers.child(key);

                                dbs.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        list.add(dataSnapshot.getValue(Users.class));

                                        UnConfirmedAdapter confirmedAdapter = new UnConfirmedAdapter(a, list);

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

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public class UnConfirmedAdapter extends RecyclerView.Adapter<UnConfirmedViewHolder> {

        private Context ctx;
        private List<Users> list;

        public UnConfirmedAdapter(Context a, List<Users> list) {

            ctx = a;
            this.list = list;

        }

        @Override
        public UnConfirmedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UnConfirmedViewHolder(ctx, LayoutInflater.from(parent.getContext()).inflate(R.layout.unconfirmed_assigned_dialog_row, parent, false));
        }

        @Override
        public void onBindViewHolder(UnConfirmedViewHolder holder, int position) {

            if (holder != null) {

                Users users = list.get(position);

                holder.setConfirmDialogTitle(users.getUser());

                if (users.getNumber() != null)
                    holder.setConfirmDialogNumber(users.getNumber());


                holder.setConfirmedDialogImageView(users.getImage());

            }

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }


    public static class UnConfirmedViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private Context ctx;
        private ProgressBar mProgressBar;

        public UnConfirmedViewHolder(Context ctx, View itemView) {
            super(itemView);

            this.ctx = ctx;
            this.mView = itemView;

            mProgressBar = (ProgressBar) mView.findViewById(R.id.unconfirmedDialogProgress);
        }

        public void setConfirmDialogTitle(String title) {
            TextView textView = (TextView) mView.findViewById(R.id.unpostAssignedDialogUsername);
            textView.setText(title);
        }

        public void setConfirmDialogNumber(String no) {
            TextView textView = (TextView) mView.findViewById(R.id.unpostAssignedDialogNumber);
            textView.setText(no);
        }

        public void setConfirmedDialogImageView(String url) {
            final ImageView imageView = (ImageView) mView.findViewById(R.id.unpostAssignedDialogImage);

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