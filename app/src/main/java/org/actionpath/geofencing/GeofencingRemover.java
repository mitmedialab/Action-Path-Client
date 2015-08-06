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

public class GeofencingRemover extends AbstractGeofencingManager {

    private List<String> geofenceRequstIdsToRemove;
    private GeofencingRemovalListener listener;

    public GeofencingRemover(Context context, List<String> geofenceRequstIdsToRemove, GeofencingRemovalListener listener){
        super(context);
        this.geofenceRequstIdsToRemove = geofenceRequstIdsToRemove;
    }

    @Override
    public PendingResult<Status> onReadyForAction(){
        return LocationServices.GeofencingApi.removeGeofences(googleApiClient,geofenceRequstIdsToRemove);
    }

    @Override
    public void onRequestSuccess(){
        listener.onGeofenceRemovalSuccess(geofenceRequstIdsToRemove);
    }

    @Override
    public void onRequestFailure(Status status){
        listener.onGeofenceRemovalFailure(status);
    }

}
