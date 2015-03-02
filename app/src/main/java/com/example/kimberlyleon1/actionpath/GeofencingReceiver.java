package com.example.kimberlyleon1.actionpath;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
* Created by kimberlyleon1 on 2/23/15.
*/
public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {
    @Override
    protected void onEnteredGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onEnter");

        //have pop-up on screen with specified location
        for (int index = 0; index < strings.length; index++) {
//            openAlert(View v);
        }
    }

    private void openAlert(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GeofencingReceiver.this);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setMessage("This will depend on the specific event");
        // set positive button: Respond message
        alertDialogBuilder.setPositiveButton("Respond",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // go to a new activity of the app
                Intent positveActivity = new Intent(getApplicationContext(),
                        Response.class);
                startActivity(positveActivity);
            }
        });
        // set negative button: Ignore message
        alertDialogBuilder.setNegativeButton("Ignore",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "You chose a negative answer",
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
