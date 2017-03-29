package com.example.toni.patakazi.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.toni.patakazi.BiddedJobActivity;
import com.example.toni.patakazi.BidderProfileActivity;
import com.example.toni.patakazi.R;
import com.example.toni.patakazi.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by toni on 2/10/17.
 */

public class BidderJobAdapter extends RecyclerView.Adapter<BiddedJobActivity.BiddedJobViewHolder>{

    private List<Users> posts;
    private Context mContext;
    private String job;

    public BidderJobAdapter(Context context, List<Users> fireChatUsers, String jobID) {
        posts = fireChatUsers;
        this.mContext = context;
        this.job = jobID;
    }

    @Override
    public BiddedJobActivity.BiddedJobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new BiddedJobActivity.BiddedJobViewHolder(mContext, LayoutInflater.from(parent.getContext()).inflate(R.layout.bidder_profile_row,parent,false));
    }

    @Override
    public void onBindViewHolder(BiddedJobActivity.BiddedJobViewHolder holder, final int position) {

        final Users blog = posts.get(position);

        holder.setTitle(blog.getUser());
        holder.setImge(mContext,blog.getImage());

        getKey(blog,holder);

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users");

                Query getKey = db.orderByChild("image").equalTo(blog.getImage());

                getKey.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String key = null;

                        for (DataSnapshot childs : dataSnapshot.getChildren()){
                            key = childs.getKey();
                            Log.d("Keyusss",key);
                        }


                        Intent intent = new Intent(mContext, BidderProfileActivity.class);
                        intent.putExtra("key",key);
                        intent.putExtra("job",job);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);


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

        return posts.size();
    }
    public void getKey(Users blog, final BiddedJobActivity.BiddedJobViewHolder holder){


        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users");

        Query getKey = db.orderByChild("image").equalTo(blog.getImage());

        getKey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String key = null;

                for (DataSnapshot childs : dataSnapshot.getChildren()){
                    key = childs.getKey();
                    Log.d("Keyusss", key);
                }

                holder.checIfAssigned(job,key);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
