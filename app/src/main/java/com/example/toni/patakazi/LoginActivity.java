package com.example.toni.patakazi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 20;
    private Button loginBn, signUpBtn, forgotPassBtn;
    private EditText email, pass;
    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog, googleProgress;

    private SignInButton signInButton;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        setUpViews();
        signUp();
        setUpFb();
        googlesignin();
        loginUser();
    }

    private void googlesignin() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    private void loginUser() {

        loginBn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.setMessage("Authenticating user...");
                progressDialog.show();

                if (!TextUtils.isEmpty(email.getText().toString().trim()) && !TextUtils.isEmpty(pass.getText().toString().trim())) {

                    //sigin in user

                    mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), pass.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                checkUser();

                            } else {

                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();

                        }
                    });

                } else {
                    progressDialog.dismiss();

                    Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();

                }


            }
        });

        //String email_val = email.getText().toString().trim();


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                googleProgress = new ProgressDialog(LoginActivity.this);
                googleProgress.setMessage("Authenticating user..");
                googleProgress.setCanceledOnTouchOutside(false);
                googleProgress.show();

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                // mPrpgress.setVisibility(View.GONE);
                googleProgress.dismiss();
                Toast.makeText(LoginActivity.this, "Error..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            googleProgress.dismiss();
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        } else {

                            checkUser();

                        }
                        // ...
                    }
                });
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

        email = (EditText) findViewById(R.id.loginEmail_tf);
        pass = (EditText) findViewById(R.id.loginPassword_tf);

        signInButton = (SignInButton) findViewById(R.id.google_signin);

        progressDialog = new ProgressDialog(this);
    }

    public void checkUser() {

        if (mAuth != null) {

            final String userId = mAuth.getCurrentUser().getUid();

            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(userId)) {

                        googleProgress.dismiss();
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    } else {

                        googleProgress.dismiss();
                        progressDialog.dismiss();

                        startActivity(new Intent(LoginActivity.this, MainPanel.class));
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
