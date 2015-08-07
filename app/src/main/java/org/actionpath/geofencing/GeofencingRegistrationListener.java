package org.actionpath.geofencing;

import com.google.android.gms.common.api.Status;

import java.util.List;

/**
 * Simple interface for geofencing registration listener
 */
public interface GeofencingRegistrationListener {

    void onGeofenceRegistrationSuccess(List data);
    void onGeofenceRegistrationFailure(Status status);

}
