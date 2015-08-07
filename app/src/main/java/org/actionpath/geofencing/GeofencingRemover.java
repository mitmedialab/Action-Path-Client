package org.actionpath.geofencing;

import android.content.Context;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class GeofencingRemover extends AbstractGeofencingManager {

    private List<String> geofenceRequestIdsToRemove;
    private GeofencingRemovalListener listener;

    public GeofencingRemover(Context context, List<String> geofenceRequestIdsToRemove, GeofencingRemovalListener listener){
        super(context);
        this.geofenceRequestIdsToRemove = geofenceRequestIdsToRemove;
        this.listener=listener;
    }

    @Override
    public PendingResult<Status> onReadyForAction(){
        return LocationServices.GeofencingApi.removeGeofences(googleApiClient,geofenceRequestIdsToRemove);
    }

    @Override
    public void onRequestSuccess(){
        listener.onGeofenceRemovalSuccess(geofenceRequestIdsToRemove);
    }

    @Override
    public void onRequestFailure(Status status){
        listener.onGeofenceRemovalFailure(status);
    }

}
