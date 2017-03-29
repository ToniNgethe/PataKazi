package com.example.toni.patakazi.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toni on 2/20/17.
 */

public class BiddedJobsAdapter extends RecyclerView.Adapter<BidededJobsViewHolder> {

    private Context ctx;
    private List<Jobs> biddedJobs = new ArrayList<>();

    public BiddedJobsAdapter(Context ctx, List<Jobs> biddedJobs){

        this.ctx = ctx;
        this.biddedJobs = biddedJobs;

    }

    @Override
    public BidededJobsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BidededJobsViewHolder(ctx, LayoutInflater.from(parent.getContext()).inflate(R.layout.biddedjobs_row,parent,false));
    }

    @Override
    public void onBindViewHolder(final BidededJobsViewHolder holder, int position) {

         final Jobs job = biddedJobs.get(position);

        holder.setBdImage(job.getImage());
        holder.setBdTitle(job.getTitle());
        holder.setBdDesc(job.getDesc());
        holder.setBdDate(job.getDate());
        holder.bdImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final FirebaseAuth mAuth = FirebaseAuth.getInstance();

                //get job id using image
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Jobs");
                Query  query = databaseReference.orderByChild("image").equalTo(job.getImage());

                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String key = null;

                        for (DataSnapshot ds : dataSnapshot.getChildren()){

                           key = ds.getKey();

                        }


                        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Bids");
                        db.child(key).child(mAuth.getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){

                                }
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    public int getItemCount() {
        return biddedJobs.size();
    }
}

class BidededJobsViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    private Context ctx;
    public ImageButton bdImageButton;

    public BidededJobsViewHolder(Context ctx, View itemView) {
        super(itemView);

        mView = itemView;
        this.ctx = ctx;

        bdImageButton = (ImageButton) mView.findViewById(R.id.bdDelete);
    }

    public void setBdTitle(String title){
        TextView textView = (TextView) mView.findViewById(R.id.bdTitle);
        textView.setText(title);
    }

    public void setBdDate(String date){
        TextView textView = (TextView) mView.findViewById(R.id.bdDate);
        textView.setText(date);
    }
    public void setBdDesc(String desc){
        TextView textView = (TextView) mView.findViewById(R.id.bdDesc);
        textView.setText(desc);
    }

    public void setBdImage(String url){

        ImageView imageView = (ImageView) mView.findViewById(R.id.bjImageView);

        Glide.with(ctx).load(url)
                .crossFade()
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.loading)
                .into(imageView);

    }

}