package com.toni.patakazi.ui.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.toni.patakazi.R;
import com.toni.patakazi.ui.intro_ui.GetLocationActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 100;
    @BindView(R.id.imageView9)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);


        // display permissions needed in the app...
        Animation myAnimation = AnimationUtils.loadAnimation(this, R.anim.intro_animation);
        imageView.setAnimation(myAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                List<String> permissions = new ArrayList<>();

                if (ActivityCompat.checkSelfPermission(SplashActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if (ActivityCompat.checkSelfPermission(SplashActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                }

                if (ActivityCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.SEND_SMS);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (!permissions.isEmpty()) {
                        ActivityCompat.requestPermissions(SplashActivity.this,
                                permissions.toArray(new String[]{}), PERMISSIONS_REQUEST);
                    }else {
                        nextActivity();
                    }

                } else {
                    nextActivity();
                }

            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        boolean allOK = true;
        if (requestCode == PERMISSIONS_REQUEST) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allOK = false;
                    break;
                }
            }
            if (allOK) {
                nextActivity();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Application permissions");
                alertDialog.setMessage("Some permissions were denied thus some features may not work properly. Continue anyway? ");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        nextActivity();

                        finish();
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                // Showing Alert Message
                alertDialog.create().show();
            }
        }
    }


    public void nextActivity() {
        startActivity(new Intent(SplashActivity.this, GetLocationActivity.class));
        finish();
    }
}
