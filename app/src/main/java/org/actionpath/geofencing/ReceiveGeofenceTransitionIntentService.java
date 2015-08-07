package org.actionpath.geofencing;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.Arrays;



public abstract class ReceiveGeofenceTransitionIntentService extends IntentService {

    private String TAG = this.getClass().getName();

    /**
     * Sets an identifier for this class' background thread
     */
    public ReceiveGeofenceTransitionIntentService() {
        super("ReceiveGeofenceTransitionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "handling geofence trigger");
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if(event != null){

            if(event.hasError()){
                onError(event.getErrorCode());
            } else {
                int transition = event.getGeofenceTransition();
                if(transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT){
                    String[] issuesIds = new String[event.getTriggeringGeofences().size()];
                    for (int index = 0; index < event.getTriggeringGeofences().size(); index++) {
                        issuesIds[index] = event.getTriggeringGeofences().get(index).getRequestId();
                        Log.d(TAG, "triggered issues: " + Arrays.toString(issuesIds));
                    }

                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                        onEnteredGeofences(issuesIds);
                    } else {
                        onExitedGeofences(issuesIds);
                    }
                }
            }

        }
    }

    protected abstract void onEnteredGeofences(String[] geofenceIds);

    protected abstract void onExitedGeofences(String[] geofenceIds);

    protected abstract void onError(int errorCode);
}
