package com.example.kimberlyleon1.actionpath;

import com.google.android.gms.common.ConnectionResult;

/**
* Created by kimberlyleon1 on 2/23/15.
*/
public interface GeofencingRegistererCallbacks {
    public void onApiClientConnected();
    public void onApiClientSuspended();
    public void onApiClientConnectionFailed(ConnectionResult connectionResult);

    public void onGeofencesRegisteredSuccessful();
}