package org.actionpath.geofencing;

import com.google.android.gms.common.api.Status;

import java.util.List;

/**
 * Simple interface for geofence removal listener
 */
public interface GeofencingRemovalListener {

    void onGeofenceRemovalSuccess(List data);
    void onGeofenceRemovalFailure(Status status);

}
