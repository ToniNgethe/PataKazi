package com.example.toni.patakazi.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.toni.patakazi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by toni on 2/15/17.
 */

public class ChangeUserNameDialog extends Dialog {

    private Activity mActivity;

    private ImageButton cancelBtn;
    private Button submit;
    private EditText editText;
    private ProgressBar mProgressBar;
    private TextView successText;

    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;

    public ChangeUserNameDialog(Activity context) {

        super(context);
        this.mActivity = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_usernae);

        //views......
        cancelBtn = (ImageButton) findViewById(R.id.cancelUsernameBtn);
        submit = (Button) findViewById(R.id.changeUsernameBtn);
        editText = (EditText) findViewById(R.id.changeUsername);
        mProgressBar = (ProgressBar) findViewById(R.id.userNameProgressBar);
        successText = (TextView) findViewById(R.id.successTextUsername);

        //firebase....
        mAuth = FirebaseAuth.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        //clicks....
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgressBar.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(editText.getText())){

                    if(mAuth.getCurrentUser() != null){

                        mUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                mUsers.child(mAuth.getCurrentUser().getUid()).child("user").setValue(editText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        //check if it was a success....
                                        if (task.isSuccessful()){

                                            mProgressBar.setVisibility(View.GONE);
                                            successText.setVisibility(View.VISIBLE);
                                            successText.setTextColor(Color.WHITE);
                                            successText.setText("Username successfully updated");

                                        }else {
                                            showError(task.getException().getMessage());
                                        }

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        showError(e.getMessage());
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                                showError(databaseError.getMessage());
                            }
                        });

                    }

                }else {
                    showError("Field cannot be empty...");
                }

            }
        });

    }

    private void showError(String message) {

        mProgressBar.setVisibility(View.GONE);
        successText.setVisibility(View.VISIBLE);
        successText.setTextColor(Color.RED);
        successText.setText(message);
    }


}
