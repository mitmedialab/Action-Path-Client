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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class AlertTest extends Activity {

    private TextView filler1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        final float rad = 500;

        List<Geofence> cambridge = new ArrayList<Geofence>();
        final double Cambridge_lat = 42.359254;
        final double Cambridge_long = -71.093667;
        final float Cambridge_rad = 2001;
        Geofence.Builder builder_test = new Geofence.Builder();
        builder_test.setRequestId("1234");
        builder_test.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        builder_test.setCircularRegion(Cambridge_lat, Cambridge_long, Cambridge_rad);
        builder_test.setExpirationDuration(5000);

        GeofencingRegisterer registerCambridge = new GeofencingRegisterer(this);
        cambridge.add(builder_test.build());
        registerCambridge.registerGeofences(cambridge);

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
//                    System.out.println(result.toString());




                    Log.e("GAH", "url success: "+ result.toString());
                } catch (Exception ex) {


                    System.err.println(ex);
                }
            }
        });
        thread.start();



//        String readJSON = HTTPRequests.getJSON("https://api.dev.actionpath.org/places/9841/issues/");
//        try{
//
//            JSONArray array = new JSONArray(readJSON);
//            for (int i = 0; i < array.length(); i++) {
//                JSONObject issue = array.getJSONObject(i);
//                String issue_status = issue.getString("status");
//                double issue_lat = issue.getInt("lat");
//                double issue_long = issue.getInt("lng");
//                String issue_id = issue.getString("id");
//                String issue_summary = issue.getString("summary");
//
//                Geofence.Builder builder123 = new Geofence.Builder();
//                builder123.setRequestId(issue_id);
//                builder123.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
//                builder123.setCircularRegion(issue_lat, issue_long, rad);
//                builder123.setExpirationDuration(5000);
//            }
//
//
//            //info for each issue:
//            //   "status"
//            //   "summary"
//            //   "description"
//            //  "lat"
//            //  "lng"
//            //     "address"
//            //  "image_full"
//            // "created_at",  null: false
//            // "updated_at",  null: false
//            //  "place_id"
//
//
//        } catch(Exception e){e.printStackTrace();}
//        finally{System.out.println("Success");}
//
//
    }


    public String readFully(InputStream inputStream, String encoding)
            throws IOException {
        return new String(readFully(inputStream), encoding);
    }

    private byte[] readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length = 0;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }


}

