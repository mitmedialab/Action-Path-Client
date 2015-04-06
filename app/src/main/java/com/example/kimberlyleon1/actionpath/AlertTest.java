package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.location.Geofence;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class AlertTest extends Activity {

    private TextView filler1;
    final float rad = 500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        final double Cambridge_lat = 42.359254;
        final double Cambridge_long = -71.093667;
        final float Cambridge_rad = 2001;
        String id = "1234";
        buildGeofence(Cambridge_lat, Cambridge_long, Cambridge_rad, id);

        filler1 = (TextView) findViewById(R.id.filler1);
        filler1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlertTest.this, Response.class);
                startActivity(intent);
            }
        });

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    URL u = new URL("https://api.dev.actionpath.org/places/9841/issues/");
                    InputStream in = u.openStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    parseResult(result.toString());
                    Log.e("GAH", "url success: "+ result);
                } catch (Exception ex) {


                    System.err.println(ex);
                }
            }
        });
        thread.start();

    }

    // parse result from server and send info to create geofences
    public void parseResult(String result){
        List<String> items = Arrays.asList(result.split("\\{"));
        //do more things to parse
        //all the things
    }


    // creates a geofence at given location of given radius
    // TODO: keep track of each geofence's summary, address, etc.
    public void buildGeofence(double latitude, double longitude, float radius, String id){
        List<Geofence> new_geo = new ArrayList<>();
        Geofence.Builder builder_test = new Geofence.Builder();
        builder_test.setRequestId(id);
        builder_test.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        builder_test.setCircularRegion(latitude, longitude, radius);
        builder_test.setExpirationDuration(5000);

        GeofencingRegisterer registerCambridge = new GeofencingRegisterer(this);
        new_geo.add(builder_test.build());
        registerCambridge.registerGeofences(new_geo);
    }


}

