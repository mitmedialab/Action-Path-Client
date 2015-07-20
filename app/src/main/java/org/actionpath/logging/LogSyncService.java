package org.actionpath.logging;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.actionpath.ActionPathServer;
import org.actionpath.util.Installation;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LogSyncService extends Service{

    public static String LOG_TAG = LogSyncService.class.getName();

    public LogSyncService() {
        super();
        LogsDataSource.getInstance(this);   // to set up the database correctly
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                LogsDataSource dataSource = LogsDataSource.getInstance(getApplicationContext());
                Log.d(LOG_TAG,"Timer says we should sync logs now!");
                Log.d(LOG_TAG,"  "+dataSource.countLogsToSync()+" logs to sync");
                Log.d(LOG_TAG,"  "+dataSource.countLogsNeedingLocation()+" logs needing location");
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
                        Log.d(LOG_TAG, "Sent all loggable actions to " + ActionPathServer.BASE_URL);
                        Log.d(LOG_TAG, "Response from server: " + response.toString());
                        // delete sync'ed log items
                        for(int logId:logIds){
                            LogsDataSource.getInstance().deleteLog(logId);
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject response) {
                        Log.e(LOG_TAG, "Failed to send SQL statusCode" + statusCode);
                        Log.e(LOG_TAG, "Response from server: " + response.toString());
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
        Log.v(LOG_TAG, "in onBind");
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    private String getInstallationId() {
        return Installation.id(this);
    }

    private JSONArray getUnsyncedLogsAsJson(){
        Cursor cursor = LogsDataSource.getInstance().getLogsToSyncCursor();
        JSONArray resultSet = new JSONArray();
        ArrayList<Integer> logIds = new ArrayList<Integer>();
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
        Log.v("LogSyncService", "JSON TO UPLOAD: "+resultSet.toString());
        return resultSet;
    }

}
