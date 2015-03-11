package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;


public class AlertTest extends Activity {


    private Button mainBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert);


        List<Geofence> cambridge = new ArrayList<Geofence>();
        final double Cambridge_lat = 42.359254;
        final double Cambridge_long = -71.093667;
        final float Cambridge_rad = 2000;
        Geofence.Builder builder_test = new Geofence.Builder();
        builder_test.setRequestId("1234");
        builder_test.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        builder_test.setCircularRegion(Cambridge_lat, Cambridge_long, Cambridge_rad);
        builder_test.setExpirationDuration(5000);


        GeofencingRegisterer registerCambridge = new GeofencingRegisterer(this);

        cambridge.add(builder_test.build());

        registerCambridge.registerGeofences(cambridge);




        mainBtn = (Button) findViewById(R.id.button);
        mainBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openAlert(v);
            }
        });
    }



    private void openAlert(View view) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AlertTest.this);

        alertDialogBuilder.setTitle("Title of notification Nearby");

        alertDialogBuilder.setMessage("Short description of notification\n\nWould you like to respond?");
        // set positive button: Yes message
        alertDialogBuilder.setPositiveButton("Respond",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // go to a new activity of the app
                Intent positveActivity = new Intent(AlertTest.this,
                        Response.class);
                startActivity(positveActivity);
            }
        });

        // set negative button: No message

        alertDialogBuilder.setNegativeButton("Ignore",new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog,int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
                Toast.makeText(AlertTest.this, "You chose a negative answer",
                        Toast.LENGTH_LONG).show();
            }
        });

        // set neutral button: Exit the app message

        alertDialogBuilder.setNeutralButton("Exit the app",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // exit the app and go to the HOME
                AlertTest.this.finish();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();

    }
}

