package org.actionpath.util;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
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

    public String LOG_TAG = this.getClass().getName();

    public static Locator instance;

    private Context context;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    public static synchronized Locator getInstance(Context context){
        if(instance==null){
            instance = new Locator(context);
        }
        return instance;
    }

    private Locator(Context context){
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(this.context)
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
        //TODO: determine if we need to do this or not
        Log.e(LOG_TAG, "connection to google services failed");
        if (result.hasResolution()) {
            // if there's a way to resolve the result
        } else {
            // otherwise consider showing an error
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(LOG_TAG, "Connected to google services");
        // also update last known location (current location)
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if(lastLocation==null){
            Log.w(LOG_TAG,"unable to get last location");
        } else {
            Log.d(LOG_TAG, "gog location @ " + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
            LogsDataSource.getInstance(this.context).updateAllLogsNeedingLocation(
                    lastLocation.getLatitude(), lastLocation.getLongitude()
            );
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(LOG_TAG, "connection to google services suspended");
    }

}
