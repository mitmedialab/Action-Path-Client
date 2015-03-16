package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;



public class Response extends Activity {

    Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);
    }

    Intent intent = getIntent();


}