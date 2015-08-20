package org.actionpath.sync;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogsDataSource;
import org.actionpath.logging.LogsDbHelper;
import org.actionpath.responses.Response;
import org.actionpath.responses.ResponsesDataSource;
import org.actionpath.responses.ResponsesDbHelper;
import org.actionpath.util.ActionPathServer;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by rahulb on 8/20/15.
 */
public class ResponseSyncTimerTask extends TimerTask{

    public static String TAG = ResponseSyncTimerTask.class.getName();

    private ResponsesDataSource dataSource;
    private GoogleApiClient googleApiClient;
    private String installId;

    public ResponseSyncTimerTask(String installId, GoogleApiClient googleApiClient, Context context) {
        this.dataSource = ResponsesDataSource.getInstance(context);
        this.googleApiClient = googleApiClient;
        this.installId = installId;
    }

    public Location getLocation(){
        return LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
    }

    public void run() {
        Log.d(TAG, "Timer says we should sync responses now!");
        Log.d(TAG, "  " + dataSource.countResponsesToSync() + " responses to sync");
        Log.d(TAG, "  " + dataSource.countResponsesNeedingLocation() + " responses needing location");
        // check if we have a location or not
        if (googleApiClient.isConnected()) {
            Location loc = getLocation();
            if (loc != null) {
                dataSource.updateAllResponsesNeedingLocation(loc.getLatitude(), loc.getLongitude());
            }
        } else {
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();  // try to reconnect!
            }
        }
        // first assemble all the data
        JSONArray sendJSON = getUnsyncedResponsesAsJson();
        final ArrayList<Integer> ids = new ArrayList<>();
        try {
            for (int i = 0; i < sendJSON.length(); i++) {
                JSONObject row = sendJSON.getJSONObject(i);
                ids.add(row.getInt(LogsDbHelper.LOGS_ID_COL));
            }
        } catch (JSONException e) {
            // TODO: Throw this exception and offer a snackbar alert
            e.printStackTrace();
        }
        if (ids.size() == 0) {   // if nothing to sync, don't send to server
            return;
        }
        // now send off the data to the server
        Boolean worked = false;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(ActionPathServer.BASE_URL + "/responses/sync.json");
            List<NameValuePair> nameValuePairs = new ArrayList<>(2);
            nameValuePairs.add(new BasicNameValuePair("data", sendJSON.toString()));
            nameValuePairs.add(new BasicNameValuePair("install_id", installId));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            String responseStr = EntityUtils.toString(httpResponse.getEntity());
            JSONObject jsonResponse = new JSONObject(responseStr);
            Log.v(TAG, "responseStatus = " + jsonResponse.getString(ActionPathServer.RESPONSE_STATUS));
            if (ActionPathServer.RESPONSE_STATUS_OK.equals(jsonResponse.getString(ActionPathServer.RESPONSE_STATUS))) {
                Log.d(TAG, "Sent all loggable actions to " + ActionPathServer.BASE_URL);
                Log.v(TAG, "Response from server: " + responseStr.toString());
                // delete sync'ed log items
                for (int id : ids) {
                    ResponsesDataSource.getInstance().deleteResponse(id);
                }
            }
            worked = true;
        } catch (IOException ioe){
            Log.e(TAG, "Server said it failed to sync responses: "+ioe.toString());
            worked = false;
        } catch (JSONException jse){
            Log.e(TAG, "Server said it failed to sync responses: "+jse.toString());
            worked = false;
        }
        if(!worked){
            for (int id : ids) {
                ResponsesDataSource.getInstance().updateResponseStatus(id, Response.RESPONSE_STATUS_DID_NOT_SYNC);
            }
        }
    }

    private JSONArray getUnsyncedResponsesAsJson(){
        Cursor cursor = ResponsesDataSource.getInstance().getResponsesToSyncCursor();
        JSONArray resultSet = new JSONArray();
        ArrayList<Integer> ids = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            // TODO: move this into a LogMsg.getJson helper method that uses GSON to serialize itself
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for( int i=0 ;  i< totalColumn ; i++ ){
                if( cursor.getColumnName(i) != null ){
                    if(cursor.getColumnName(i).equals(ResponsesDbHelper.RESPONSES_ID_COL)){
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
                        Log.e(TAG, e.getMessage()  );
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
            LogsDataSource.getInstance().updateLogStatus(id, Response.RESPONSE_STATUS_SYNCING);
        }
        Log.v(TAG, "JSON TO UPLOAD: " + resultSet.toString());
        return resultSet;
    }

}