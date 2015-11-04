package org.actionpath.sync;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.util.Installation;

import java.util.Timer;
import java.util.TimerTask;

public class SyncService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public static String TAG = SyncService.class.getName();

    private static boolean running = false;

    private static int LOG_SYNC_INTERVAL = 5 * 60 * 1000;
    private static int RESPONSE_SYNC_INTERVAL = 1 * 60 * 1000;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private Timer logTimer;
    private Timer responseTimer;

    public SyncService() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        setRunning(true);
        Log.i(TAG,"Starting LogSyncService");
        // to set up the database correctly
        LogsDataSource.getInstance(this);
        ResponsesDataSource.getInstance(this);
        // to connect for locaiton info
        googleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        // upload log messages periodically
        TimerTask logSyncer = new LogSyncTimerTask(this,getInstallationId(),googleApiClient);
        logTimer = new Timer();
        logTimer.schedule(logSyncer, 0, LOG_SYNC_INTERVAL);
        // upload responses periodically
        TimerTask responseSyncer = new ResponseSyncTimerTask(this,getInstallationId(),googleApiClient);
        responseTimer = new Timer();
        responseTimer.schedule(responseSyncer, 0, RESPONSE_SYNC_INTERVAL);
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "in onBind");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "in onUnbind");
        return true;
    }

    private String getInstallationId() {
        return Installation.id(this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "connection to google services failed (errorCode=" + result.getErrorCode() + ")");
        if (result.hasResolution()) {
            Log.e(TAG, "has a resolution");
        } else {
            Log.e(TAG, "no a resolution");
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to google services");
        // also update last known location (current location)
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if(lastLocation==null){
            Log.w(TAG, "unable to get last location");
        } else {
            Log.d(TAG, "got location @ " + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "connection to google services suspended");
    }

    @Override
    public void onDestroy() {
        if((googleApiClient!=null) && googleApiClient.isConnected() ){
            googleApiClient.disconnect();
        }
        if(logTimer!=null){
            logTimer.cancel();
        }
        if(responseTimer!=null){
            responseTimer.cancel();
        }
    }

    public synchronized static boolean isRunning(){
        return running;
    }

    private synchronized void setRunning(boolean r){
        running = r;
    }


}
