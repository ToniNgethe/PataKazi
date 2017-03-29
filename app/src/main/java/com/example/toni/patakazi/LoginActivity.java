package com.example.toni.patakazi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private Button loginBn, signUpBtn, forgotPassBtn;
    private EditText email,pass;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setUpViews();
        signUp();
        setUpFb();
        loginUser();
    }

    private void loginUser() {

        loginBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Authenticating user...");
                progressDialog.show();

                if (!TextUtils.isEmpty(email.getText().toString().trim()) && !TextUtils.isEmpty(pass.getText().toString().trim()) ){

                        //sigin in user

                    mAuth.signInWithEmailAndPassword(email.getText().toString().trim(),pass.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                checkUser();

                            }else {

                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    });

                }else {
                    progressDialog.dismiss();

                    Toast.makeText(getApplicationContext(),"Fields cannot be empty",Toast.LENGTH_SHORT).show();

                }


            }
        });

        //String email_val = email.getText().toString().trim();

    }

    private void setUpFb() {

        mUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsers.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

    }

    private void signUp() {

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

            }
        });

    }

    private void setUpViews() {

        loginBn = (Button) findViewById(R.id.loginBtn);
        signUpBtn = (Button) findViewById(R.id.signUp_btn);
        forgotPassBtn = (Button) findViewById(R.id.forgotPass);

        email = (EditText)findViewById(R.id.loginEmail_tf);
        pass = (EditText) findViewById(R.id.loginPassword_tf);

        progressDialog = new ProgressDialog(this);
    }

    public void checkUser(){

        if (mAuth != null){

            final String userId = mAuth.getCurrentUser().getUid();

            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(userId)){

                        progressDialog.dismiss();

                        startActivity(new Intent(LoginActivity.this,SetupActivity.class));
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    }else {

                        progressDialog.dismiss();

                        startActivity(new Intent(LoginActivity.this,MainPanel.class));
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }
}
