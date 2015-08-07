package org.actionpath.geofencing;

import android.content.Context;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class GeofencingRemover extends AbstractGeofencingManager {

    private List<String> geofenceRequstIdsToRemove;
    private GeofencingRemovalListener listener;

    public GeofencingRemover(Context context, List<String> geofenceRequstIdsToRemove, GeofencingRemovalListener listener){
        super(context);
        this.geofenceRequstIdsToRemove = geofenceRequstIdsToRemove;
        this.listener=listener;
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
