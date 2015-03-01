package com.example.kimberlyleon1.actionpath;

import android.util.Log;

/**
* Created by kimberlyleon1 on 2/23/15.
*/
public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {
    @Override
    protected void onEnteredGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onEnter");

        //do something!
        //check if geofence id is within a certain distance
        //if it is, have pop-up on screen with specified location

    }

    @Override
    protected void onExitedGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onExit");

        //do something!
        //either remove pop-up from screen (if it hasn't been addressed by the user
        //or do nothing
    }

@Override
protected void onError(int i) {
        Log.e(GeofencingReceiver.class.getName(), "Error: " + i);
        }

}
