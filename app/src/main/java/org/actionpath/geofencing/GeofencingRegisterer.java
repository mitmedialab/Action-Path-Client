package org.actionpath.geofencing;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofencingRegisterer extends AbstractGeofencingManager {

    private List<Geofence> geofencesToAdd;
    private PendingIntent geofencePendingIntent;
    private GeofencingRegistrationListener listener;

    public GeofencingRegisterer(Context context, List<Geofence> geofencesToAdd, GeofencingRegistrationListener listener){
        super(context);
        this.listener = listener;
        this.geofencesToAdd = geofencesToAdd;
        geofencePendingIntent = createRequestPendingIntent();
    }

    @Override
    public PendingResult<Status> onReadyForAction(){
        return LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencesToAdd, geofencePendingIntent);
    }

    @Override
    public void onRequestSuccess(){
        listener.onGeofenceRegistrationSuccess(geofencesToAdd);
    }

    @Override
    public void onRequestFailure(Status status){
        listener.onGeofenceRegistrationFailure(status);
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location
     * Services issues the Intent inside this PendingIntent whenever a geofence
     * transition occurs for the current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence
     * transitions.
     */
    private PendingIntent createRequestPendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        } else {
            Intent intent = new Intent(context, GeofencingReceiver.class);
            return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }
}
