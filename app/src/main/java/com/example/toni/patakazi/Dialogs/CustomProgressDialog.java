package com.example.toni.patakazi.Dialogs;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.toni.patakazi.R;

/**
 * Created by toni on 2/11/17.
 */

public class CustomProgressDialog extends Dialog{

    private Activity a;
    private String message;

    private ProgressBar progressBar;
    private TextView textView;

    public CustomProgressDialog(Activity context, String message) {
        super(context);

        this.a = context;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_progressdialog);

        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        textView = (TextView) findViewById(R.id.dialogMessage);

        textView.setText(message);

    }
}
