package org.actionpath.logging;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LogSyncService extends IntentService{

    public String TAG = this.getClass().getName();

    public static final String PARAM_SYNC_TYPE = "syncType";
    public static final String SYNC_TYPE_SEND = "send";

    public LogSyncService(){
        super("LogSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub
        String logType = intent.getStringExtra(PARAM_SYNC_TYPE); //TODO: make this a constant
        if(logType.equals(SYNC_TYPE_SEND)) {    //TODO: make this a constant
            Log.d(TAG, "Request to send new logs");
            sendToServer();
        }
    }

    private JSONArray getResults(){
        SQLiteDatabase logDB = SQLiteDatabase.openDatabase(LoggerService.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
        String searchQuery = "SELECT  * FROM " + LoggerService.DB_TABLE_NAME +
                " where status="+LoggerService.LOG_STATUS_NEW+" OR status="+LoggerService.LOG_STATUS_DID_NOT_SYNC;
        Cursor cursor = logDB.rawQuery(searchQuery, null );
        JSONArray resultSet = new JSONArray();
        ArrayList<Integer> logIds = new ArrayList<Integer>();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    if(cursor.getColumnName(i).equals("id")){
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
        // update the issues saying we are trying to sync
        for(int logId:logIds){
            String updateSql = "UPDATE "+LoggerService.DB_TABLE_NAME+
                    " SET status="+LoggerService.LOG_STATUS_SYNCING+" WHERE id="+logId;
            logDB.execSQL(updateSql);
        }
        logDB.close();
        Log.d("LogSyncService", "JSON TO UPLOAD: "+resultSet.toString());
        return resultSet;
    }

    public void sendToServer() {
        AsyncHttpClient client = new AsyncHttpClient();
        JSONArray sendJSON = getResults();
        final ArrayList<Integer> logIds = new ArrayList<Integer>();
        try {
            for (int i = 0; i < sendJSON.length(); i++) {
                JSONObject row = sendJSON.getJSONObject(i);
                logIds.add(row.getInt("logId"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(logIds.size()==0){   // if not logs to sync, don't send to server
            return;
        }
        RequestParams params = new RequestParams();
        params.add("data",sendJSON.toString());
        client.post("https://api.dev.actionpath.org/logs/create", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.d(TAG,"sent all the info");
                // delete sync'ed log items
                SQLiteDatabase logDB = SQLiteDatabase.openDatabase(LoggerService.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                for(int logId:logIds){
                    String updateSql = "DELETE FROM "+LoggerService.DB_TABLE_NAME+
                            " WHERE id="+logId;
                    logDB.execSQL(updateSql);
                }
                logDB.close();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Failed to send SQL statusCode" + statusCode);
                // mark that we were not able to sync them
                SQLiteDatabase logDB = SQLiteDatabase.openDatabase(LoggerService.DATABASE_PATH, null, SQLiteDatabase.OPEN_READWRITE);
                for(int logId:logIds){
                    String updateSql = "UPDATE "+LoggerService.DB_TABLE_NAME+
                            " SET status="+LoggerService.LOG_STATUS_DID_NOT_SYNC+" WHERE id="+logId;
                    logDB.execSQL(updateSql);
                }
                logDB.close();
            }
        });
    }

}
