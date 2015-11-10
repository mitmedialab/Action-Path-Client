package org.actionpath.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.actionpath.R;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.util.Development;
import org.actionpath.util.DeviceUtil;
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

    @Override
    public void onResume(){
        super.onResume();
        boolean locationServicesOk = DeviceUtil.isLocationServicesEnabled(this);
        if(!locationServicesOk) {
            Log.e(TAG, "Location GPS or Network not available :-(");
            // TODO: notify user via a dialog alert, linking to a new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS); to fix it
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(this.getResources().getString(R.string.location_not_enabled));
            dialog.setPositiveButton(this.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            dialog.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO: what to do?
                }
            });
            dialog.show();
        } else {
            if(googleApiClient!=null && !googleApiClient.isConnected())
                googleApiClient.connect();
        }
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
            Log.d(TAG, "got location @ " + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        }
        onGooglePlayServicesConnected();
    }

    protected void onGooglePlayServicesConnected(){
        // override this to do something in your subclass
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

    protected Location updateLastLocation(){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if(lastLocation!=null){
            hasLocation = true;
        } else {
            Log.w(TAG,"couldn't get location from FusedLocationApi in updateLastLocation!");
            if(Development.isSimulator()){
                lastLocation = Development.getFakeLocation();
            }
        }
        return lastLocation;
    }



    protected void logMsg(int issueId, String action){
        logMsg(issueId, action, "");
    }

    protected void logMsg(int issueId, String action, String details){
        Location loc = null;
        Log.i(TAG,"Log Msg "+action+" on "+issueId+" ("+details+")");
        try {
            loc = getLocation();
        } catch(GoogleApiClientNotConnectionException mce){
            Log.w(TAG,"unable to get location for log message");
        }
        logMsg(issueId, action, details, loc);
    }

}