package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;


public class Response extends Activity {

    private RadioGroup radioGroup;
    private Button submitBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.short_response);


        submitBtn = (Button) findViewById(R.id.submit_button);
        submitBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Response.this, AlertTest.class);
                startActivity(intent);
            }
        });


        radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);

//        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                // find which radio button is selected
//                if(checkedId == R.id.radio_resolved) {
//                    Toast.makeText(getApplicationContext(), "choice: Resolved",
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "choice: Unresolved",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


    }



}