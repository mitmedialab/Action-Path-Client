package org.actionpath.geofencing;

import com.google.android.gms.common.api.Status;

import java.util.List;

/**
 * Created by rahulb on 8/6/15.
 */
public interface GeofencingRegistrationListener {

    void onGeofenceRegistrationSuccess(List data);
    void onGeofenceRegistrationFailure(Status status);

}
