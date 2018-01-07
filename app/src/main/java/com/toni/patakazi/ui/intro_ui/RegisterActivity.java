package com.toni.patakazi.ui.intro_ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toni.patakazi.Api.ApiClient;
import com.toni.patakazi.Api.ErrorUtils;
import com.toni.patakazi.R;
import com.toni.patakazi.model.requests.RegisterRequest;
import com.toni.patakazi.model.responses.RegisterResponse;
import com.toni.patakazi.utils.Global;
import com.toni.patakazi.utils.Utills;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.internal.Util;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;
    @BindView(R.id.signin)
    TextView signin;
    @BindView(R.id.signup)
    TextView signup;
    @BindView(R.id.email)
    EditText email;
    @BindView(R.id.user)
    EditText user;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.account)
    TextView account;

    private DatabaseReference mUsers;
    private StorageReference mProfile;
    private FirebaseAuth mAuth;

    private Uri imageUri = null;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_v2);
        ButterKnife.bind(this);

    }


    @OnClick({R.id.signin, R.id.signup, R.id.account})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.signin:

                // redirect to signin..
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();

                break;
            case R.id.signup:

                break;
            case R.id.account:

                // validate fields
                if (TextUtils.isEmpty(email.getText())){
                    email.setError("Email is required");
                    return;
                }

                // check if email is correct format...
                if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    email.setError("Invalid email format");
                    return;
                }

                if (TextUtils.isEmpty(user.getText())){
                    user.setError("Username is required");
                    return;
                }


                if (TextUtils.isEmpty(password.getText())){
                    password.setError("Password is required");
                    return;
                }


                register();

                break;
        }
    }

    private void register(){

        // show progress..
        final SweetAlertDialog progress = Utills.showProgress(this, "Registering user...");
        progress.show();

        // create request object...
        final RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email.getText().toString());
        registerRequest.setPassword(password.getText().toString());
        registerRequest.setUsername(user.getText().toString());

        // make call to server
        Call<RegisterResponse> call = ApiClient.apiInterface().registerUser(registerRequest);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {

                progress.dismissWithAnimation();

                if (response.code() == 200){

                    if (response.body().getStatus().equals("00")){
                        // redirect user to login...
                        SweetAlertDialog success = Utills.successWithButton(RegisterActivity.this, response.body().getMessage());
                        success.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {

                                sweetAlertDialog.dismissWithAnimation();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();

                            }
                        });
                        success.show();
                    }else {
                        Utills.showErrorToast(response.body().getMessage());
                    }

                }else {
                    Utills.showErrorToast(getString(R.string.global_error));
                }

            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                    progress.dismissWithAnimation();
                    Utills.showErrorToast(new ErrorUtils().parseOnFailure(t));
            }
        });


    }
}
