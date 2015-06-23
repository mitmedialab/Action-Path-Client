package org.actionpath.logging;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;


//need to log:
//timestamp,
//location, and
//activity completed:
//        geo-fence loaded,
//        geo-fence triggered,
//        notification clicked (entered app),
//        survey completed, or
//        following page dismissed


public class LoggerService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;

    public String TAG = this.getClass().getName();

    public static final String PARAM_LOG_TYPE = "logType";
    public static final String LOG_TYPE_ACTION = "action";

    public static final String PARAM_USER_ID = "userID";
    public static final String PARAM_ISSUE_ID = "issueID";

    public static final String PARAM_ACTION = "action";
    public static final String ACTION_NEWS_FEED_CLICK = "NewsfeedClick";
    public static final String ACTION_NOTIFICATION_IGNORE_CLICK = "NotificationIgnoreClick";
    public static final String ACTION_NOTIFICATION_RESPOND_CLICK = "NotificationRespondClick";
    public static final String ACTION_SURVEY_RESPONSE = "SurveyResponse";
    public static final String ACTION_THANKS_DISMISSED = "ThanksDismissed";
    public static final String ACTION_ENTERED_GEOFENCE = "EnteredGeofence";
    public static final String ACTION_LOADED_LATEST_ISSUES = "LoadedLatestIssues";
    public static final String ACTION_INSTALLED_APP = "InstalledApp";

    public static final Integer LOG_STATUS_NEW = 0;
    public static final Integer LOG_STATUS_SYNCING = 1;
    public static final Integer LOG_STATUS_DID_NOT_SYNC = 2;

    public static final String DATABASE_PATH = "/data/data/org.actionpath/databases/logging";
    public static final String DB_TABLE_NAME = "actions";

    Location lastLocation;

    Stack<ArrayList<String>> queuedActionLogs;

    public LoggerService(){
        super("LoggerService");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.d(TAG,"onCreate");
        queuedActionLogs = new Stack<ArrayList<String>>();
        // Create a GoogleApiClient instance
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        Intent intent=new Intent(getApplicationContext(),this.getClass());
        this.startService(intent);
        try {
            SQLiteDatabase logDB = this.openOrCreateDatabase(DATABASE_PATH, MODE_PRIVATE, null);
            // TODO: add a version table so we don't need to do migrations
            logDB.execSQL("CREATE TABLE IF NOT EXISTS "
                    + DB_TABLE_NAME
                    + "  (timestamp VARCHAR, userID VARCHAR, issueID VARCHAR, lat VARCHAR, long VARCHAR, actionType VARCHAR, status INT,id integer primary key autoincrement);");
            logDB.close();
        } catch(Exception e) {
            Log.e("LoggerService", "Could not create logging db and table: "+e.toString());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.d("LoggerService", "Starting ");
        onHandleIntent(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String logType = intent.getStringExtra(PARAM_LOG_TYPE);
        Log.i("LoggerService", "recieved intent to log " + logType);
        if (LOG_TYPE_ACTION.equals(logType)) {
            String userID = String.valueOf(intent.getStringExtra(PARAM_USER_ID));
            String issueID = intent.getStringExtra(PARAM_ISSUE_ID);
            String action = intent.getStringExtra(PARAM_ACTION);
            Log.d("LoggerService", "action is "+action);
            queueLogItem(userID, issueID, action);
        } else {
            Log.e("LoggerService", "unknown action logType: "+logType);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
		/*
		 * Google Play services can resolve some errors it detects.
		 * If the error has a resolution, try sending an Intent to
		 * start a Google Play services activity that can resolve
		 * error.
		 */
        //TODO: determine if we need to do this or not
        Log.e(TAG, "connection to google services failed");
        if (result.hasResolution()) {
            // if there's a way to resolve the result
        } else {
            // otherwise consider showing an error
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "connection to google services worked");
        // also update last known location (current location)
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if(lastLocation==null){
            Log.w(TAG,"unable to get last location");
        } else {
            Log.d(TAG,"@ "+lastLocation.getLatitude()+","+lastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG,"connection to google services suspended");
    }

    /**
     * queue it now and write it once we get a connection to services to get the lat/lng
     * @param userID
     * @param issueID
     * @param action
     */
    private void queueLogItem(String userID, String issueID, String action){
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ArrayList<String> a = new ArrayList<>();
        // TODO: should we get the lat/lng here?
        a.add(0,now.toString());
        a.add(1,userID);
        a.add(2,issueID);
        a.add(3, action);
        Log.i(TAG, action);
        queuedActionLogs.push(a);
        writeLogQueueToDatabase();//log them even though you didn't get lat/lng
    }

    //LOG ACTIONS TO A FILE
    //QUESTION: Which actions need location data?
//    AddedGeofence
//    - timestamp, user id, geofence id
//    LoadedLatestActions
//    - timestamp, user id, GPS coordinates
//    Entered (Geofence)
//    - timestamp, user id, geofence id, GPS coordinates
//    NotificationClick
//    - timestamp, user id, geofence id, GPS coordinates
//    SurveyResponse
//    - timestamp, user id, geofence id, GPS coordinates
//    ThanksDismissed / Unfollowed Issue
//    - timestamp, user id, geofence id
    // need: timestamp, user id, geofence id, gps coords, action
    //actions: AddedGeofence
    // TODO: turn these into constants and fix where they are usde in other classes
    //         LoadedLatestActions
    //         NotificationRespondClick
    //         NotificationIgnoreClick
    //         SurveyResponse
    //         EnteredGeofence
    //         ThanksDismissed
    //         UnfollowedIssue
    private void writeLogQueueToDatabase() {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        SQLiteDatabase logDB = this.openOrCreateDatabase(DATABASE_PATH, MODE_PRIVATE, null);
        Iterator<ArrayList<String>> it = queuedActionLogs.iterator();
        Log.i(TAG,"writing queue to db");
        while (it.hasNext()) {
            ArrayList<String> splitAction = it.next();
            String longitude = "";
            String latitude = "";
            if (lastLocation != null) {
                latitude = String.valueOf(lastLocation.getLatitude());
                longitude = String.valueOf(lastLocation.getLongitude());
                Log.d(TAG,"lastLocation @ "+lastLocation.getLatitude()+","+lastLocation.getLongitude());
            } else {
                Log.w(TAG,"lastLocation is null");
            }
            logDB.execSQL("INSERT INTO "
                    + DB_TABLE_NAME +"(timestamp, userID, issueID, lat, long, actionType, status) "
                    + " VALUES ('" + splitAction.get(0) + "','" + splitAction.get(1) + "','" + splitAction.get(2) + "','" + latitude + "','" + longitude + "','" + splitAction.get(3) + "', "+LOG_STATUS_NEW+");");
        } // TODO: are the locations actually being saved?
        logDB.close();
        queuedActionLogs.clear(); // TODO: could be a garbage collection issue
        // and now try to sync to server
        Intent logSyncServiceIntent = new Intent(LoggerService.this, LogSyncService.class);
        logSyncServiceIntent.putExtra(LogSyncService.PARAM_SYNC_TYPE, LogSyncService.SYNC_TYPE_SEND);
        startService(logSyncServiceIntent);
    }

}

