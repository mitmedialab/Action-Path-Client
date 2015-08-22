package org.actionpath.sync;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.actionpath.db.AbstractSyncableModel;
import org.actionpath.db.AbstractSyncableDataSource;
import org.actionpath.db.SyncableDbHelper;
import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.db.responses.Response;
import org.actionpath.db.responses.ResponsesDataSource;
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

    public static String TAG = AbstractSyncTimerTask.class.getName();

    protected GoogleApiClient googleApiClient;
    protected String installId;
    protected ContextWrapper contextWrapper;
    protected AbstractSyncableDataSource dataSource;

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
        // first assemble all the data
        JSONArray recordsToSync = getUnsyncedDataAsJson();
        if (recordsToSync.length() == 0) {   // if not logs to sync, don't send to server
            return;
        }
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
        // now send off the data to the server
        Boolean worked = false;
        String syncUrl = getUploadUrl();
        try {
            JSONObject jsonResponse = ActionPathServer.syncToServer(syncUrl, recordsToSync, installId);
            if (ActionPathServer.RESPONSE_STATUS_OK.equals(jsonResponse.getString(ActionPathServer.RESPONSE_STATUS))) {
                Log.d(TAG, "Sent all records to " + ActionPathServer.BASE_URL);
                Log.d(TAG, "Need to delete "+recordsToSync.length()+" records");
                // delete sync'ed log items
                for (int i = 0; i < recordsToSync.length(); i++) {
                    JSONObject obj = recordsToSync.getJSONObject(i);
                    int id = obj.getInt(SyncableDbHelper.ID_COL);
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
            try {
                for (int i = 0; i < recordsToSync.length(); i++) {
                    JSONObject obj = recordsToSync.getJSONObject(i);
                    int id = obj.getInt(SyncableDbHelper.ID_COL);
                    dataSource.updateStatus(id, AbstractSyncableModel.STATUS_DID_NOT_SYNC);
                }
            } catch (JSONException jse){
                Log.e(TAG,"wasn't able to mark the records to sync as "+AbstractSyncableModel.STATUS_DID_NOT_SYNC);
            }
        }
    }

    private Location getLocation(){
        return LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
    }

    private JSONArray getUnsyncedDataAsJson(){
        Cursor cursor = ResponsesDataSource.getInstance().getDataToSyncCursor();
        JSONArray recordsToSync = new JSONArray();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            JSONObject jsonObject = dataSource.cursorToJsonObject(cursor);
            recordsToSync.put(jsonObject);
            cursor.moveToNext();
        }
        cursor.close();
        // build a list of the ids for deletion later
        if(recordsToSync.length()==0){   // if nothing to sync, don't send to server
            return recordsToSync;
        }
        // update the issues saying we are trying to sync
        try {
            for (int i = 0; i < recordsToSync.length(); i++) {
                JSONObject obj = recordsToSync.getJSONObject(i);
                int id = obj.getInt(SyncableDbHelper.ID_COL);
                dataSource.updateStatus(id, AbstractSyncableModel.STATUS_SYNCING);
            }
        } catch(JSONException jse){
            Log.e(TAG,"wasn't able to mark the records to sync as "+AbstractSyncableModel.STATUS_SYNCING);
        }
        Log.v(TAG, "JSON TO UPLOAD: " + recordsToSync.toString());
        return recordsToSync;
    }

}
