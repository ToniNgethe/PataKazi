package com.example.toni.patakazi.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.toni.patakazi.PostAskillActivity;
import com.example.toni.patakazi.PostJobActivity;
import com.example.toni.patakazi.R;
/**
 * Created by toni on 2/8/17.
 */

public class AddChoiceDialog extends Dialog implements View.OnClickListener{
    
    private Activity a;
    
    private Button job,skill;
    
    public AddChoiceDialog(Activity context) {
        super(context);
        a = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        
        job = (Button) findViewById(R.id.postAjobBtn);
        skill = (Button) findViewById(R.id.skillsBtn);
        
        job.setOnClickListener(this);
        skill.setOnClickListener(this);
        
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.postAjobBtn:

                Intent i = new Intent(a, PostJobActivity.class);
                a.startActivity(i);
                
                break;
            
            case R.id.skillsBtn:

                Intent m = new Intent(a, PostAskillActivity.class);
                a.startActivity(m);

                break;
        }
    }
}
