package com.example.toni.patakazi.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.model.Jobs;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by toni on 2/20/17.
 */

public class AssignedJobsAdapter extends RecyclerView.Adapter<AssignedJobsViewHolder> {

    private Context ctx;
    private List<Jobs> list = new ArrayList<>();
    private Handler handler;

    public AssignedJobsAdapter(Context activity, List<Jobs> list) {
        this.ctx = activity;
        this.list = list;
    }

    @Override
    public AssignedJobsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AssignedJobsViewHolder(ctx, LayoutInflater.from(parent.getContext()).inflate(R.layout.assigned_jobs_row,parent,false));
    }

    @Override
    public void onBindViewHolder(final AssignedJobsViewHolder holder, int position) {

        final Jobs jobs = list.get(position);

        holder.setAssignedJobTitle(jobs.getTitle());
        holder.setAssignJobDesc(jobs.getDesc());
        holder.setAssignJobIImage(jobs.getImage());
        getJobId(holder,jobs);
        holder.confirmJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final DatabaseReference mConfirm = FirebaseDatabase.getInstance().getReference().child("Confirms");
                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                final String userId = mAuth.getCurrentUser().getUid();

                //getJob id
                DatabaseReference mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");

                Query query = mJobs.orderByChild("image").equalTo(jobs.getImage());
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String key = null;

                        final boolean[] process = {true};

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            key = ds.getKey();
                            }
                            //record confirmed......
                            DatabaseReference cf = mConfirm.child(key);

                            cf.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (process[0]) {

                                        if (dataSnapshot.hasChild(userId)) {

                                            dataSnapshot.getRef().removeValue();
                                            process[0] = false;

                                        } else {

                                            dataSnapshot.getRef().child(userId).setValue(1);

                                            process[0] = false;

                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        holder.confrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                handler = new Handler();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        final DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("Completed");
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        final String userID = mAuth.getCurrentUser().getUid();

                        //getJob id
                        DatabaseReference mJobs = FirebaseDatabase.getInstance().getReference().child("Jobs");

                        Query query = mJobs.orderByChild("image").equalTo(jobs.getImage());
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String key = null;

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    key = ds.getKey();
                                }

                                final DatabaseReference db = databaseReference.child(key);

                                final String finalKey = key;

                                db.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        try{

                                            Date date = new Date();
                                            //Date newDate = new Date(date.getTime() + (604800000L * 2) + (24 * 60 * 60));

                                            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
                                            String stringdate = dt.format(date);

                                            System.out.println("Submission Date: " + stringdate);

                                            dataSnapshot.getRef().child("Date").setValue(stringdate);

                                            dataSnapshot.getRef().child("rate").setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()){

                                                        //remove from assigned
                                                        DatabaseReference removeAssigned = FirebaseDatabase.getInstance().getReference().child("Assigns");
                                                        removeAssigned.child(finalKey).child(userID).removeValue();
                                                        holder.confrim.setEnabled(false);
                                                        Toast.makeText(ctx,"Marked as completed", Toast.LENGTH_SHORT).show();


                                                        //remove from confirmed;
//                                                        DatabaseReference removeConfirmed = FirebaseDatabase.getInstance().getReference().child("Confirms");
//                                                        removeConfirmed.child(finalKey).child(userID).removeValue();

                                                    }

                                                }
                                            });


                                        }catch (Exception e){
                                            Log.d("AssignedJobsAdapter", e.getMessage());
                                        }

                                        // db.child(userID).child();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };

                if (runnable != null){
                    handler.post(runnable);
                }

            }
        });

        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void getJobId(final AssignedJobsViewHolder holder, Jobs jobs){


        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Jobs");
        Query query = mDatabaseReference.orderByChild("image").equalTo(jobs.getImage());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String key = null;
                for (DataSnapshot ss: dataSnapshot.getChildren()){
                 key = ss.getKey();
                }

                holder.checkConfirmation(key);
                holder.checkIfComplete(key);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}

class AssignedJobsViewHolder extends RecyclerView.ViewHolder{

    private View mView;
    private Context ctx;
    public ImageButton confirmJob;
    private DatabaseReference mConfirms,mCompleted;
    private FirebaseAuth mAuth;
    private TextView text;
    private View view;
    public Button confrim;
    public Button rejectButton;

    public AssignedJobsViewHolder(Context ctx, View itemView) {
        super(itemView);

        mView = itemView;
        this.ctx = ctx;

        text = (TextView) mView.findViewById(R.id.confirmationTxt);

        confirmJob = (ImageButton) mView.findViewById(R.id.assignedJobConfirm);

        mConfirms = FirebaseDatabase.getInstance().getReference().child("Confirms");
        mConfirms.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        confrim = (Button) mView.findViewById(R.id.markAsComplete);
        mCompleted = FirebaseDatabase.getInstance().getReference().child("Completed");
        view = mView.findViewById(R.id.mss);

        rejectButton = (Button) mView.findViewById(R.id.assignedRejectButton);
    }

    public void setAssignedJobTitle(String title){
        TextView textView = (TextView) mView.findViewById(R.id.assignedJobTitle);
        textView.setText(title);
    }

    public void setAssignJobDesc(String desc){
        TextView textView = (TextView) mView.findViewById(R.id.assignedJobDesc);
        textView.setText(desc);
    }

    public void setAssignJobIImage(String url){
        ImageView imageView = (ImageView) mView.findViewById(R.id.assignedJobsImage);

        Glide.with(ctx).load(url)
                .crossFade()
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.loading)
                .into(imageView);
    }

    public void checkIfComplete(final String jobId){

        mCompleted.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(jobId).child(mAuth.getCurrentUser().getUid()).exists()){

                    DatabaseReference removeAssigned = FirebaseDatabase.getInstance().getReference().child("Assigns");

                    removeAssigned.child(jobId).child(mAuth.getCurrentUser().getUid()).removeValue();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void checkConfirmation(String jobID){

        DatabaseReference query = mConfirms.child(jobID);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())){

                    confirmJob.setImageResource(R.mipmap.ic_done_all_black_24dp);
                    text.setTextColor(Color.parseColor("#4caf50"));
                    text.setText("Confirmed");

                    view.setVisibility(View.VISIBLE);
                    confrim.setVisibility(View.VISIBLE);

                    rejectButton.setEnabled(false);

                }else {

                    rejectButton.setEnabled(true);
                    view.setVisibility(View.GONE);
                    confrim.setVisibility(View.GONE);
                    text.setTextColor(Color.GRAY);
                    text.setText("Confirm");
                    confirmJob.setImageResource(R.mipmap.ic_done_black_24dp);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
