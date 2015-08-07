package org.actionpath.geofencing;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

/**
 * Lets you add or remove geofences via the onReadyForAction method you have to implement
 */
public abstract class AbstractGeofencingManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    protected Context context;
    protected GoogleApiClient googleApiClient;

    private final String TAG = this.getClass().getName();

    public AbstractGeofencingManager(Context context){
        this.context = context;
    }

    public void sendRequest(){
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        PendingResult<Status> result = onReadyForAction();

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.d(TAG,"Got action result!");
                if (status.isSuccess()) {
                    // Successfully registered
                    onRequestSuccess();
                } else if (status.hasResolution()) {
                    Log.d(TAG,"got hasResolution status");
                    // Google provides a way to fix the issue
                    /*
                    status.startResolutionForResult(
                            context,     // your current activity used to receive the result
                            RESULT_CODE); // the result code you'll look for in your
                    // onActivityResult method to retry registering
                    */
                } else {
                    //TODO: check errr code, if 1000 pop up dialog asking user to enable location in settings
                    // No recovery. Weep softly or inform the user.
                    onRequestFailure(status);
                }
            }
        });
    }

    public abstract PendingResult<Status> onReadyForAction();

    public abstract void onRequestSuccess();

    public abstract void onRequestFailure(Status status);

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorCode());
    }

}
