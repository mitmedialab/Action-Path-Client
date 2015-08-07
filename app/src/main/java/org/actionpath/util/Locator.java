package org.actionpath.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogsDataSource;

/**
 * Wrapper that helps you get the phone's last location
 */
public class Locator implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;

    public static String TAG = Locator.class.getName();

    public static Locator instance;

    private Activity activity;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    public boolean hasLocation = false;

    public static synchronized Locator getInstance(Activity activity){
        if(instance==null){
            instance = new Locator(activity);
        }
        return instance;
    }

    public static synchronized Locator getInstance(){
        if(instance==null){
            Log.e(TAG,"Tried to get Locator before initializing it with an instance!");
            return null;
        }
        return instance;
    }

    private Locator(Activity activity){
        this.activity = activity;
        googleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public Location getLocation(){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        return lastLocation;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
		/*
		 * Google Play services can resolve some errors it detects.
		 * If the error has a resolution, try sending an Intent to
		 * start a Google Play services activity that can resolve
		 * error.
		 */
        Log.e(TAG, "connection to google services failed");
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this.activity,RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG,"Unable to resolve error :-( "+e);
            }
        } else {
            switch(result.getErrorCode()){
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                case ConnectionResult.SERVICE_DISABLED:
                case ConnectionResult.SERVICE_MISSING:
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), activity, 1);
                    dialog.show();
                    break;
            }
            Log.e(TAG,"No resolution for failure :-( "+result.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to google services");
        // also update last known location (current location)
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        hasLocation = true;
        if(lastLocation==null){
            Log.w(TAG,"unable to get last location");
        } else {
            Log.d(TAG, "got location @ " + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
            LogsDataSource.getInstance(activity.getApplicationContext()).updateAllLogsNeedingLocation(
                    lastLocation.getLatitude(), lastLocation.getLongitude()
            );
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "connection to google services suspended");
    }

    public synchronized boolean hasLocation(){
        return hasLocation;
    }

}
