package com.toni.patakazi.Dialogs;

import android.app.Activity;
import android.app.Dialog;
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

import com.toni.patakazi.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by toni on 2/15/17.
 */

public class ChangePhoneNumber extends Dialog{

    private Activity a;

    private ImageButton cancelBtn;
    private Button submit;
    private EditText editText;
    private ProgressBar mProgressBar;
    private TextView successText;

    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;

    public ChangePhoneNumber(Activity context) {
        super(context);

        this.a = context;

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_number_dialog);

        cancelBtn = (ImageButton) findViewById(R.id.cancelNumberBtn);
        submit = (Button) findViewById(R.id.changePhoneNuberBtn);
        editText = (EditText) findViewById(R.id.changePhoneNumber);
        mProgressBar = (ProgressBar) findViewById(R.id.phoneNumberProgressBar);
        successText = (TextView) findViewById(R.id.successText);

        mAuth = FirebaseAuth.getInstance();
        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dismiss();

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                successText.setVisibility(View.VISIBLE);
                successText.setText("");
                mProgressBar.setVisibility(View.VISIBLE);

                if (!TextUtils.isEmpty(editText.getText().toString())){

                    if (checkNumber(editText.getText().toString())) {


                        mUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                mUsers.child(mAuth.getCurrentUser().getUid()).child("number").setValue(editText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {

                                            mProgressBar.setVisibility(View.GONE);
                                            successText.setTextColor(Color.WHITE);
                                            successText.setText("Phone number updated...");
                                        } else {

                                            mProgressBar.setVisibility(View.GONE);
                                            successText.setTextColor(Color.WHITE);
                                            successText.setText(task.getException().getMessage());
                                        }

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                                mProgressBar.setVisibility(View.GONE);
                                successText.setTextColor(Color.WHITE);
                                successText.setText(databaseError.getMessage());

                            }
                        });
                    }else {

                        mProgressBar.setVisibility(View.GONE);
                        successText.setTextColor(Color.WHITE);
                        successText.setText("Phone Number must be in the form 07XX XXX XXX");


                    }

                }else {

                    mProgressBar.setVisibility(View.GONE);
                    successText.setTextColor(Color.RED);
                    successText.setText("Field cannot be empty..");

                }

            }
        });

    }

    private boolean checkNumber(String sPhoneNumber){

        boolean check = false;

        Pattern pattern = Pattern.compile("07\\d{2}\\d{6}");
        Matcher matcher = pattern.matcher(sPhoneNumber);

        if (matcher.matches()) {
            check = true;
        }
//        else
//        {
//            Log.d("Status : ","Phone Number must be in the form XXX-XXXXXXX");
//        }

        return check;
    }


}
