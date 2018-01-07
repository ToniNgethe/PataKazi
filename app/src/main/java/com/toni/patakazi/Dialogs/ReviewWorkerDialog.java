package com.toni.patakazi.Dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class ReviewWorkerDialog extends Dialog {

    private Context context;
    private String userId, jobKey;
    private EditText review;
    private ImageButton cancel;
    private Button submit;

    public ReviewWorkerDialog(Context context, String userId, String jobKey) {
        super(context);

        this.context = context;
        this.userId = userId;
        this.jobKey = jobKey;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_worker_dialog);

        //views
        cancel = (ImageButton) findViewById(R.id.imagebtn_reviewworker_cancel);
        review = (EditText) findViewById(R.id.edittext_reviewworker_review);
        submit = (Button) findViewById(R.id.btn_revieworker_submit);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //action listeners...
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!TextUtils.isEmpty(review.getText())) {

                    final ProgressDialog progressDialog = new ProgressDialog(context);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setMessage("Submitting review...");
                    progressDialog.show();

                    DatabaseReference mRatings = FirebaseDatabase.getInstance().getReference().child("Feedback");

                    mRatings.child(userId).child(jobKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.child("Review").exists())
                                dataSnapshot.getRef().child("Review").setValue(review.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        progressDialog.setMessage("finalising...");

                                        if (task.isSuccessful()) {

                                            progressDialog.dismiss();
                                            Toast.makeText(context, "Submitted successfully...", Toast.LENGTH_SHORT).show();
                                        }else {
                                            progressDialog.dismiss();
                                            Toast.makeText(context,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    });
                } else {
                    Toast.makeText(context, "Field cannot be empty..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
