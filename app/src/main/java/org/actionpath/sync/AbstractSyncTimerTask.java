package org.actionpath.sync;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.actionpath.db.AbstractSyncableModel;
import org.actionpath.db.SyncableDataSource;
import org.actionpath.db.SyncableDbHelper;
import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.db.responses.Response;
import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.db.responses.ResponsesDbHelper;
import org.actionpath.util.ActionPathServer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;

/**
 * Upload SyncableModels to the server
 */
public abstract class AbstractSyncTimerTask extends TimerTask {

    private final String TAG = this.getClass().getName();

    protected GoogleApiClient googleApiClient;
    protected String installId;
    protected ContextWrapper contextWrapper;
    protected SyncableDataSource dataSource;

    public AbstractSyncTimerTask(ContextWrapper contextWrapper, GoogleApiClient googleApiClient, Context context, String installId) {
        this.contextWrapper = contextWrapper;
        this.googleApiClient = googleApiClient;
        this.installId = installId;
    }

    protected abstract String getUploadUrl();

    @Override
    public void run() {
        Log.d(TAG, "Timer says we should sync now!");
        Log.d(TAG, "  " + dataSource.countDataToSync() + " to sync");
        Log.d(TAG, "  " + dataSource.countDataNeedingLocation() + " needing location");
        // check if we have a location or not
        if (googleApiClient.isConnected()) {
            Location loc = getLocation();
            if (loc != null) {
                dataSource.updateDataNeedingLocation(loc.getLatitude(), loc.getLongitude());
            }
        } else {
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();  // try to reconnect!
            }
        }
        // first assemble all the data
        JSONArray sendJSON = getUnsyncedDataAsJson();
        final ArrayList<Integer> ids = new ArrayList<>();
        try {
            for (int i = 0; i < sendJSON.length(); i++) {
                JSONObject row = sendJSON.getJSONObject(i);
                ids.add(row.getInt(SyncableDbHelper.ID_COL));
            }
        } catch (JSONException e) {
            // TODO: Throw this exception and offer a snackbar alert
            e.printStackTrace();
        }
        if (ids.size() == 0) {   // if not logs to sync, don't send to server
            return;
        }
        // now send off the data to the server
        Boolean worked = false;
        String syncUrl = getUploadUrl();
        try {
            JSONObject jsonResponse = ActionPathServer.syncToServer(syncUrl, sendJSON, installId);
            if (ActionPathServer.RESPONSE_STATUS_OK.equals(jsonResponse.getString(ActionPathServer.RESPONSE_STATUS))) {
                Log.d(TAG, "Sent all records to " + ActionPathServer.BASE_URL);
                Log.d(TAG, "Need to delete "+ids.size()+" records");
                // delete sync'ed log items
                for (int id : ids) {
                    dataSource.delete(id);
                }
            }
            worked = true;
        } catch (IOException ioe){
            Log.e(TAG, "Server said it failed to sync logs: "+ioe.toString());
            worked = false;
        } catch (JSONException jse){
            Log.e(TAG, "Server said it failed to sync logs: "+jse.toString());
            worked = false;
        }
        if(!worked){
            for (int logId : ids) {
                dataSource.updateStatus(logId, AbstractSyncableModel.STATUS_DID_NOT_SYNC);
            }
        }
    }

    private Location getLocation(){
        return LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
    }

    private JSONArray getUnsyncedDataAsJson(){
        Cursor cursor = ResponsesDataSource.getInstance().getDataToSyncCursor();
        JSONArray resultSet = new JSONArray();
        ArrayList<Integer> ids = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // TODO: move this into a LogMsg.getJson helper method that uses GSON to serialize itself
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    if(cursor.getColumnName(i).equals(ResponsesDbHelper.ID_COL)){
                        ids.add(cursor.getInt(i));
                    }
                    try{
                        if( cursor.getString(i) != null ){
                            //Log.d("LogSyncService", "  "+cursor.getString(i));
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        }else{
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }catch( Exception e ){
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        if(ids.size()==0){   // if nothing to sync, don't send to server
            return resultSet;
        }
        // update the issues saying we are trying to sync
        for(int id:ids){
            LogsDataSource.getInstance().updateStatus(id, Response.STATUS_SYNCING);
        }
        Log.v(TAG, "JSON TO UPLOAD: " + resultSet.toString());
        return resultSet;
    }

}
