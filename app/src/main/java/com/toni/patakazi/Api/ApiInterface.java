package com.toni.patakazi.Api;

import com.google.android.gms.location.LocationRequest;
import com.toni.patakazi.model.requests.LoginRequest;
import com.toni.patakazi.model.requests.PostJobRequest;
import com.toni.patakazi.model.requests.RegisterRequest;
import com.toni.patakazi.model.responses.JobsResponse;
import com.toni.patakazi.model.responses.LocationResponse;
import com.toni.patakazi.model.responses.LoginResponse;
import com.toni.patakazi.model.responses.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by toni on 12/22/17.
 */

public interface ApiInterface {

    @POST("users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @POST("location/save")
    Call<LocationResponse> userLocation(@Body com.toni.patakazi.model.requests.LocationRequest request);

    @POST("jobs/add_job")
    Call<LocationResponse> postJob(@Body PostJobRequest postJobRequest);

    @GET("jobs/all")
    Call<JobsResponse> getAllJobs();

    @POST("users/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest registerRequest);
}
