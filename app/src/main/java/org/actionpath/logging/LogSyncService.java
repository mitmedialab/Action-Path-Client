package org.actionpath.logging;

import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.actionpath.ui.ActionPathServer;
import org.actionpath.util.Installation;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LogSyncService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public static String TAG = LogSyncService.class.getName();

    private static boolean running = false;

    public synchronized static boolean isRunning(){
        return running;
    }
    private synchronized void setRunning(boolean r){
        running = r;
    }

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    public LogSyncService() {
        super();
        LogsDataSource.getInstance(this);   // to set up the database correctly
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        Log.i(TAG,"Starting LogSyncService");
        googleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        setRunning(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                LogsDataSource dataSource = LogsDataSource.getInstance(getApplicationContext());
                Log.d(TAG,"Timer says we should sync logs now!");
                Log.d(TAG,"  "+dataSource.countLogsToSync()+" logs to sync");
                Log.d(TAG, "  " + dataSource.countLogsNeedingLocation() + " logs needing location");
                if(googleApiClient.isConnected()) {
                    Location loc = getLocation();
                    dataSource.updateAllLogsNeedingLocation(loc.getLatitude(), loc.getLongitude());
                } else {
                    googleApiClient.connect();  // try to reconnect!
                }
                SyncHttpClient client = new SyncHttpClient();
                JSONArray sendJSON = getUnsyncedLogsAsJson();
                final ArrayList<Integer> logIds = new ArrayList<>();
                try {
                    for (int i = 0; i < sendJSON.length(); i++) {
                        JSONObject row = sendJSON.getJSONObject(i);
                        logIds.add(row.getInt(LogsDbHelper.LOGS_ID_COL));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(logIds.size()==0){   // if not logs to sync, don't send to server
                    return;
                }
                RequestParams params = new RequestParams();
                params.add("data",sendJSON.toString());
                params.add("install_id", getInstallationId());
                client.post(ActionPathServer.BASE_URL + "/logs/sync", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.d(TAG, "Sent all loggable actions to " + ActionPathServer.BASE_URL);
                        Log.d(TAG, "Response from server: " + response.toString());
                        // delete sync'ed log items
                        for(int logId:logIds){
                            LogsDataSource.getInstance().deleteLog(logId);
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response) {
                        Log.e(TAG, "Failed to send SQL statusCode" + statusCode);
                        Log.e(TAG, "Response from server: " + response.toString());
                        // mark that we were not able to sync them
                        for(int logId:logIds){
                            LogsDataSource.getInstance().updateLogStatus(logId, LogMsg.LOG_STATUS_DID_NOT_SYNC);
                        }
                    }
                });
            }
        }, 60*1000);
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

    private JSONArray getUnsyncedLogsAsJson(){
        Cursor cursor = LogsDataSource.getInstance().getLogsToSyncCursor();
        JSONArray resultSet = new JSONArray();
        ArrayList<Integer> logIds = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // TODO: move this into a LogMsg.getJson helper method that uses GSON to serialize itself
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    if(cursor.getColumnName(i).equals(LogsDbHelper.LOGS_ID_COL)){
                        logIds.add(cursor.getInt(i));
                    }
                    try{
                        if( cursor.getString(i) != null ){
                            //Log.d("LogSyncService", "  "+cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        }else{
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }catch( Exception e ){
                        Log.e("LogSyncService", e.getMessage()  );
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        if(logIds.size()==0){   // if not logs to sync, don't send to server
            return resultSet;
        }
        // update the issues saying we are trying to sync
        for(int logId:logIds){
            LogsDataSource.getInstance().updateLogStatus(logId, LogMsg.LOG_STATUS_SYNCING);
        }
        Log.v("LogSyncService", "JSON TO UPLOAD: " + resultSet.toString());
        return resultSet;
    }

    public Location getLocation(){
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        return lastLocation;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "connection to google services failed (errorCode="+result.getErrorCode()+")");
        if (result.hasResolution()) {
            Log.e(TAG,"has a resolution");
        } else {
            Log.e(TAG,"no a resolution");
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to google services");
        // also update last known location (current location)
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if(lastLocation==null){
            Log.w(TAG,"unable to get last location");
        } else {
            Log.d(TAG, "got location @ " + lastLocation.getLatitude() + "," + lastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "connection to google services suspended");
    }
}
