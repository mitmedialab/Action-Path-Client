package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AfterAction extends Activity{

    private Button unfollowBtn;
    private Button homeBtn;
    int id = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_action);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getInt("issueID");
        Log.e("AFTER", "issue id from AfterAction: " + id);



        homeBtn = (Button) findViewById(R.id.home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AfterAction.this, AlertTest.class);
                intent.putExtra("followThisID", id);
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
