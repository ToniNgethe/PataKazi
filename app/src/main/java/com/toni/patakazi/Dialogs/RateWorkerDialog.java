package com.toni.patakazi.Dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.toni.patakazi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by toni on 3/2/17.
 */

public class RateWorkerDialog extends Dialog {

    private Context ctx;
    private String userId, jobKey;
    private RatingBar ratingBar;
    private Button submit;
    private ImageButton cancel;
    private TextView number;

    public RateWorkerDialog(Context context, String userId, String jobKey) {
        super(context);

        ctx = context;
        this.userId = userId;
        this.jobKey = jobKey;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_worker_dialog);

        //views
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        submit = (Button) findViewById(R.id.submitRatings);
        cancel = (ImageButton) findViewById(R.id.ratingCancelBtn);
        number = (TextView) findViewById(R.id.ratingTextView);

        //close
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //getrating
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                number.setText(String.valueOf(rating));

            }
        });

        //save ratings

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog progressDialog = new ProgressDialog(ctx);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setMessage("Saving ratings...");
                progressDialog.show();

                DatabaseReference mRatings = FirebaseDatabase.getInstance().getReference().child("Feedback");
                mRatings.child(userId).child(jobKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (!dataSnapshot.child("Rating").exists())
                            dataSnapshot.getRef().child("Rating").setValue(number.getText()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    progressDialog.setMessage("finalising...");
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(ctx, "Saved successfully", Toast.LENGTH_SHORT).show();
                                    }else {
                                        progressDialog.dismiss();
                                        Toast.makeText(ctx,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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
}
