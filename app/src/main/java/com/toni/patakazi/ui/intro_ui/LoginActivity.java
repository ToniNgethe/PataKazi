package com.toni.patakazi.ui.intro_ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.toni.patakazi.Api.ApiClient;
import com.toni.patakazi.R;
import com.toni.patakazi.SetupActivity;
import com.toni.patakazi.model.requests.LoginRequest;
import com.toni.patakazi.model.responses.LoginResponse;
import com.toni.patakazi.utils.PrefManager;
import com.toni.patakazi.utils.Utills;
import com.toni.patakazi.utils.Validator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 20;
    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.buttonsignin)
    TextView buttonsignin;
    @BindView(R.id.fb)
    Button fb;
    @BindView(R.id.account)
    TextView account;
    @BindView(R.id.linear)
    LinearLayout linear;
    @BindView(R.id.forgotPass)
    TextView forgotPass;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.signin)
    TextView signin;
    @BindView(R.id.signup)
    TextView signup;

    private DatabaseReference mUsers;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog, googleProgress;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_v2);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);

        signUp();
        setUpFb();
        googlesignin();
    }

    private void googlesignin() {


        fb.setOnClickListener(new View.OnClickListener() {
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

        // display progress....
        final SweetAlertDialog progress = Utills.showProgress(LoginActivity.this, "authenticating user..");
        progress.show();

        // package request...
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email.getText().toString());
        loginRequest.setPassword(password.getText().toString());

        // make call to servre..
        Call<LoginResponse> call = ApiClient.apiInterface().loginUser(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                progress.dismissWithAnimation();

                if (response.code() == 200){

                    if (response.body().getStatus().equals("00")){

                        // was a success.....store user info to preferences
                        PrefManager.storeUser(response.body());

                        // set session...
                        PrefManager.setLoggedIn();

                        // redirect to location area....
                        startActivity(new Intent(LoginActivity.this, GetLocationActivity.class));
                        finish();

                    }else {
                        Utills.showErrorToast(response.body().getMsg());
                    }

                }else {
                    Utills.showErrorToast(getString(R.string.global_error));
                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

                progress.dismissWithAnimation();

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
                Toast.makeText(LoginActivity.this, "Error: " + result.getStatus().getStatusMessage(), Toast.LENGTH_SHORT).show();
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

                        googleProgress.dismiss();

                        if (!task.isSuccessful()) {

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

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

            }
        });

    }


    public void checkUser() {

        if (mAuth != null) {

            final String userId = mAuth.getCurrentUser().getUid();

            mUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(userId)) {


                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        finish();

                    } else {

                        progressDialog.dismiss();

                        Intent launchNextActivity;
                        launchNextActivity = new Intent(LoginActivity.this, GetLocationActivity.class);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(launchNextActivity);

                        // startActivity(new Intent(LoginActivity.this, MainPanel.class));
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                        finish();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    @OnClick(R.id.signup)
    public void onViewClicked() {
        // redirect to signup
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }


    @OnClick({R.id.buttonsignin, R.id.signup})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.buttonsignin:

                if (!Utills.validate(new EditText[]{email, password})) {
                    return;
                }

                if (!Validator.isEmalValid(email.getText().toString())) {
                    email.setError(getString(R.string.invalid_email_address));
                    return;
                }

                loginUser();

                break;
            case R.id.signup:
                break;
        }
    }

}
