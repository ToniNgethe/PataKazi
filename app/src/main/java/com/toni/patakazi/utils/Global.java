package com.toni.patakazi.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.toni.patakazi.R;

/**
 * Created by toni on 2/9/17.
 */

public class Global {

    public static void showDialog(String title, String message, Context a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(a, R.style.MyDialogTheme);
        builder.setTitle(title);
        builder.setMessage(message);

        String positiveText = "Okay";
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                    }
                });

//        String negativeText = String.valueOf(android.R.string.cancel);
//        builder.setNegativeButton(negativeText,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        // negative button logic
//                    }
//                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    //check connection...
    public static boolean isConnected(Context context) {

        boolean connected = false;

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnected()) {
            connected = true;
        }

        return connected;
    }

    public static void writeToLog(String name, String msg){
        Log.e(name, "==========================================================================================");
        Log.e(name, msg);
        Log.e(name, "==========================================================================================");
    }

}
