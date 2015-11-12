package org.actionpath.sync;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.TimerTask;

/**
 * Base class for timer tasks that need access to location information
 * Created by rahulb on 11/12/15.
 */
public abstract class AbstractLocationTimerTask extends TimerTask {

    protected GoogleApiClient googleApiClient;

    public AbstractLocationTimerTask(GoogleApiClient googleApiClient){
        this.googleApiClient = googleApiClient;
    }

    public Location getLocation(){
        return LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
    }

}
