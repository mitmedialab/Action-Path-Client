package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by kimberlyleon1 on 4/6/15.
 */
public class AfterAction extends Activity{

    private Button unfollowBtn;
    private Button homeBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.short_response);


        homeBtn = (Button) findViewById(R.id.home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AfterAction.this, AlertTest.class);
                startActivity(intent);
            }
        });

        unfollowBtn = (Button) findViewById(R.id.unfollow_btn);
        unfollowBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AfterAction.this, AlertTest.class);
                startActivity(intent);
            }
        });

    }


}
