package com.toni.patakazi.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.toni.patakazi.App;
import com.toni.patakazi.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;

/**
 * Created by toni on 11/2/17.
 * will contain all alerts needed by the app....
 */

public class Utills {


    private static Context ctx(){
        return App.getInstance().getApplicationContext();
    }

    public static void showErrorToast(String msg) {
        Toasty.error(ctx(), msg, Toast.LENGTH_SHORT, true).show();
    }

    public static void showSuccessToast(String msg) {
        Toasty.success(ctx(), msg, Toast.LENGTH_SHORT, true).show();
    }

    public static void showInfoToast(String msg) {
        Toasty.info(ctx(), msg, Toast.LENGTH_SHORT, true).show();
    }

    public static void showWarnignToast(String msg) {
        Toasty.warning(ctx(), msg, Toast.LENGTH_SHORT, true).show();
    }


    public static boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String pass) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{6,})"; // "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[,.@#$%*!^&()]).{8,20})";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(pass);

        return matcher.matches();
    }

    public static Dialog dialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public static void compressInputImage(Intent data, Context context, ImageView newIV) {
        Bitmap bitmap;
        Uri inputImageData = data.getData();
        try {
            Bitmap bitmapInputImage = MediaStore.Images.Media.getBitmap(context.getContentResolver(), inputImageData);
            if (bitmapInputImage.getWidth() > 2048 && bitmapInputImage.getHeight() > 2048) {
                bitmap = Bitmap.createScaledBitmap(bitmapInputImage, 1024, 1280, true);
                newIV.setImageBitmap(bitmap);
            } else if (bitmapInputImage.getWidth() > 2048 && bitmapInputImage.getHeight() < 2048) {
                bitmap = Bitmap.createScaledBitmap(bitmapInputImage, 1920, 1200, true);
                newIV.setImageBitmap(bitmap);
            } else if (bitmapInputImage.getWidth() < 2048 && bitmapInputImage.getHeight() > 2048) {
                bitmap = Bitmap.createScaledBitmap(bitmapInputImage, 1024, 1280, true);
                newIV.setImageBitmap(bitmap);
            } else if (bitmapInputImage.getWidth() < 2048 && bitmapInputImage.getHeight() < 2048) {
                bitmap = Bitmap.createScaledBitmap(bitmapInputImage, bitmapInputImage.getWidth(), bitmapInputImage.getHeight(), true);
                newIV.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void showError(Context ctx, String message) {
        new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message)
                .show();
    }

    public static SweetAlertDialog showErrorWithButton(Context ctx, String message) {
        return new SweetAlertDialog(ctx, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message);
    }


    public static void showSuccess(Context ctx, String message) {
        new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText(message)
                .show();
    }


    public static SweetAlertDialog showInformation(Context ctx, String title, String message) {

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ctx, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(title)
                .setContentText(message)
                .setCustomImage(R.mipmap.logo);

        return sweetAlertDialog;
    }

    public static SweetAlertDialog showWarning(Context ctx, String title, String msg) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ctx, SweetAlertDialog.WARNING_TYPE);
        sweetAlertDialog.setTitleText(title)
                .setContentText(msg)
                .setCancelText("No")
                .setConfirmText("Yes");

        return sweetAlertDialog;
    }

    public static SweetAlertDialog successWithButton(Context ctx, String msg) {
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(ctx, SweetAlertDialog.SUCCESS_TYPE);
        sweetAlertDialog.setTitleText("Success")
                .setContentText(msg);

        return sweetAlertDialog;
    }

    public static SweetAlertDialog showProgress(Context ctx, String message) {
        SweetAlertDialog pDialog = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(R.color.colorAccent);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.setTitleText(ctx.getString(R.string.loading_text));
        pDialog.setContentText(message);
        return pDialog;

    }

    public static boolean validate(EditText[] fields) {

        for (int i = 0; i < fields.length; i++) {
            EditText currentField = fields[i];
            if (currentField.getText().toString().length() <= 0) {
                currentField.setError(currentField.getHint() +" is required");
                return false;
            }
        }

        return true;
    }


    public static void animateClicks(View v) {
       v.startAnimation(AnimationUtils.loadAnimation(App.getInstance(), R.anim.btn_animation));
    }

    public static boolean validatePhoneNumber(String phoneNo) {
        //validate phone numbers of format "1234567890"
//        if (phoneNo.matches("\\d{10}")) return true;
//            //validating phone number with -, . or spaces
//        else if(phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")) return true;
//            //validating phone number with extension length from 3 to 5
//        else if(phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}")) return true;
//            //validating phone number where area code is in braces ()
//        else if(phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}")) return true;
//            //return false if nothing matches the input
//        else return false;
        return true;

    }


    //method for setting up toolbar back home arror
    public static void backHomeToolbar(Toolbar toolbar, String title, Context ctx){

        ((AppCompatActivity) ctx).setSupportActionBar(toolbar);
        ((AppCompatActivity) ctx).getSupportActionBar().setTitle(title);
        ((AppCompatActivity) ctx).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //method to display back arrow key for activites

    public static void writeLog(Class name, String content ){

        String Tag= name.getSimpleName();

        Log.e(Tag, "=============================================="+Tag+"======================================================");
        Log.e(Tag, content );
        Log.e(Tag, "===========================================================================================================" );
    }

    public static String getDate(long timeStamp) {

        try {
            DateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "xx";
        }
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

    public static boolean validEmail(String email){

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    public static void pushKeyBoardUp(Context ctx){
        ((Activity)ctx).getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    // converts object to json
    public static String getObjectString(Object o){
        return new Gson().toJson(o);
    }


//    String date_before = "1970-01-01";
//    String date_after = formateDateFromstring("yyyy-MM-dd", "dd, MMM yyyy", date_before);
    public static String formateDateFromstring(String inputFormat, String outputFormat, String inputDate){

        Date parsed = null;
        String outputDate = "";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {
            Log.e("date", "ParseException - dateFormat");
        }

        return outputDate;

    }
}
