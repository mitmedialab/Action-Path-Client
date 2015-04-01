package com.example.kimberlyleon1.actionpath;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class Notification extends ActionBarActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Button respondBtn = (Button) findViewById(R.id.respond_issue);
        Button ignoreBtn = (Button) findViewById(R.id.ignore_issue);


        ignoreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    finish();
                    System.exit(0);
            }
        });

        respondBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Notification.this, Response.class);
                startActivity(intent);
            }
        });
    }

    Intent intent = getIntent();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
        return true;
    }



    public void toResponse(View view) {
        Intent intent = new Intent(view.getContext(), Response.class);
        startActivityForResult(intent, 0);
    }
}
