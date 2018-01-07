package com.toni.patakazi.Api;

import com.google.android.gms.location.LocationRequest;
import com.google.gson.Gson;
import com.toni.patakazi.model.responses.LocationResponse;
import com.toni.patakazi.utils.Utills;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by toni on 12/22/17.
 */

public class BackGroundCalls {

    public static void writeLogs(String msg) {
        Utills.writeLog(BackGroundCalls.class, msg);
    }

    public static void storeUserLocation(final com.toni.patakazi.model.requests.LocationRequest request) {

        Call<LocationResponse> call = ApiClient.apiInterface().userLocation(request);
        call.enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {

                if (response.code() == 200) {
                    // all good..
                    writeLogs(new Gson().toJson(response.body()));
                }

            }

            @Override
            public void onFailure(Call<LocationResponse> call, Throwable t) {
                writeLogs(new ErrorUtils().parseOnFailure(t));
            }
        });


    }

}
