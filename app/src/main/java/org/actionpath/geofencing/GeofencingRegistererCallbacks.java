package org.actionpath.geofencing;

import com.google.android.gms.common.ConnectionResult;



public interface GeofencingRegistererCallbacks {
    void onApiClientConnected();
    void onApiClientSuspended();
    void onApiClientConnectionFailed(ConnectionResult connectionResult);

    void onGeofencesRegisteredSuccessful();
}