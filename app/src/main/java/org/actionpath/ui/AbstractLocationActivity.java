package org.actionpath.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import org.actionpath.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.actionpath.logging.LogMsg;
import org.actionpath.util.GoogleApiClientNotConnectionException;

/**
 * An activity with location helpers
 * See:
 * https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient
 */
public abstract class AbstractLocationActivity extends AbstractBaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;

    private final String TAG = this.getClass().getName();

    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private boolean hasLocation = false;

    /**
     * Set up the client
     */
    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        // TODO: If Location services are off, fire dialog box
        // try to get the location
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void verifyLocationServicesEnabled(){
        LocationManager lm = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsOk = false;
        boolean networkOk = false;

        try {
            if(lm.getAllProviders().contains(LocationManager.GPS_PROVIDER)){
                gpsOk = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } else {
                gpsOk = true;
            }
        } catch(Exception ex) {}

        try {
            if(lm.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                networkOk = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } else {
                networkOk = true;
            }
        } catch(Exception ex) {}

        Log.i(TAG,"LocationManager - GPS: "+gpsOk+", Network:"+networkOk);

        if(!gpsOk && !networkOk) {
            Log.e(TAG, "Location GPS or Network not available :-(");
            // notify user via a dialog alert, linking to a
            // new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        verifyLocationServicesEnabled();
        googleApiClient.connect();
    }

    @Override
    public void onStop(){
        super.onStop();
        googleApiClient.disconnect();
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
                result.startResolutionForResult(this,RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG,"Failed to resolve with startResolutionForResult :-( "+e);
            }
        } else {
            switch(result.getErrorCode()){
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                case ConnectionResult.SERVICE_DISABLED:
                case ConnectionResult.SERVICE_MISSING:
                    Log.w(TAG, "Trying to resolve with dialog " + result.getErrorCode());
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 1);
                    dialog.show();
                    break;
                default:
                    Log.e(TAG,"No resolution for failure :-( "+result.getErrorCode());
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to google services");
        updateLastLocation();
        if(lastLocation==null){
            Log.w(TAG,"unable to get last location");
        } else {
            hasLocation = true;
            Log.d(TAG, "got location @ " + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "connection to google services suspended");
        // Applications should disable UI components that require the service, and wait for a call
        // to onConnected(Bundle) to re-enable them.
    }

    public Location getLocation() throws GoogleApiClientNotConnectionException {
        if(googleApiClient.isConnected()){
            return updateLastLocation();
        }
        throw new GoogleApiClientNotConnectionException();
    }

    public boolean hasLocation(){
        return hasLocation;
    }

    private Location updateLastLocation(){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if(lastLocation!=null){
            hasLocation = true;
        }
        return lastLocation;
    }

    protected void logMsg(String action){
        logMsg(LogMsg.NO_ISSUE, action);
    }

    protected void logMsg(int issueId, String action){
        Location loc = null;
        try {
            loc = getLocation();
        } catch(GoogleApiClientNotConnectionException mce){
            Log.w(TAG,"unable to get location for log message");
        }
        logMsg(issueId, action, loc);
    }

}