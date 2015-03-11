package com.example.kimberlyleon1.actionpath;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;



public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {
    @Override
    protected void onEnteredGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onEnter");

        //have pop-up on screen with specified location


        //change so it takes in a list of strings
        //ex: String[] alert where
        //alert[0] is latitude
        //alert[1] is longitude
        //alert[2] is Name of alert
        //alert[3] is a short description of the alert
        //alert[4] is a picture url??
        openAlert(strings);
    }

    private void openAlert(String[] alert) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GeofencingReceiver.this);
        //alert[2]
        alertDialogBuilder.setTitle("Alert");
        //alert[3]
        alertDialogBuilder.setMessage("This will depend on the specific event");
        // set positive button: Respond message
        alertDialogBuilder.setPositiveButton("Respond",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // go to a new activity of the app
                Intent positveActivity = new Intent(GeofencingReceiver.this,
                        Response.class);
                startActivity(positveActivity);
            }
        });
        // set negative button: Ignore message
        alertDialogBuilder.setNegativeButton("Ignore",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
                Toast.makeText(GeofencingReceiver.this, "You chose a to ignore the alert",
                        Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }

    @Override
    protected void onExitedGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onExit");

        //remove pop-up from screen
        //or do nothing
    }

    @Override
    protected void onError(int i) {
        Log.e(GeofencingReceiver.class.getName(), "Error: " + i);
        }

}
