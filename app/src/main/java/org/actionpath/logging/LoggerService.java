package org.actionpath.logging;

import android.app.IntentService;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{
    private GoogleApiClient mGoogleApiClient;

    public static final String PARAM_LOG_TYPE = "logType";
    public static final String LOG_TYPE_ACTION = "action";
    public static final String PARAM_USER_ID = "userID";
    public static final String PARAM_ISSUE_ID = "issueID";
    public static final String PARAM_ACTION = "action";

    public static final String DATABASE_PATH = "/data/data/org.actionpath/databases/logging";
    public static final String DB_TABLE_NAME = "actions";

    Location mLastLocation;
    LocationClient mLocationClient;

    Stack<ArrayList<String>> queuedActionLogs;

    SQLiteDatabase myDB= null;

    public LoggerService(){
        super("LoggerService");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mLocationClient = new LocationClient(this, this, this);
        queuedActionLogs = new Stack<ArrayList<String>>();
        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();
        Intent intent=new Intent(getApplicationContext(),this.getClass());
        this.startService(intent);


  /* Create a Database. */
        try {
            myDB = this.openOrCreateDatabase(DATABASE_PATH, MODE_PRIVATE, null);
   /* Create a Table in the Database. */
            myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                    + DB_TABLE_NAME
                    + "  (timestamp VARCHAR, userID VARCHAR, geofenceID VARCHAR, lat VARCHAR, long VARCHAR, actionType VARCHAR);");

//   /*retrieve data from database */
//            Cursor c = myDB.rawQuery("SELECT * FROM " + TableName , null);
//
//            int Column1 = c.getColumnIndex("Field1");
//            int Column2 = c.getColumnIndex("Field2");
//
//            // Check if our result was valid.
//            c.moveToFirst();
//            if (c != null) {
//                // Loop through all Results
//                do {
//                    String Name = c.getString(Column1);
//                    int Age = c.getInt(Column2);
//                    Data =Data +Name+"/"+Age+"\n";
//                }while(c.moveToNext());
//            }
//            TextView tv = new TextView(this);
//            tv.setText(Data);
//            setContentView(tv);
        }
        catch(Exception e) {
            Log.e("LoggerService", "Could not create logging db and table: "+e.toString());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LoggerService","Starting ");
        onHandleIntent(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String logType = intent.getStringExtra(PARAM_LOG_TYPE);
        Log.i("LoggerService", "recieved intent to log "+logType);
        if (LOG_TYPE_ACTION.equals(logType)) {
            String userID = String.valueOf(intent.getStringExtra(PARAM_USER_ID));
            String issueID = intent.getStringExtra(PARAM_ISSUE_ID);
            String action = intent.getStringExtra(PARAM_ACTION);
            Log.d("LoggerService", "action is "+action);
            queueAction(userID, issueID, action);
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

        if (result.hasResolution()) {
            // if there's a way to resolve the result
        } else {
            // otherwise consider showing an error
        }
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        // when connected, log queued locations
        logQueuedActions();

        // also update last known location (current location)
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onDisconnected() {
    }


    public void queueAction(String userID, String issueID, String action){
        Timestamp now = new Timestamp(System.currentTimeMillis());
        ArrayList<String> a = new ArrayList<>();
        a.add(0,now.toString());
        a.add(1,userID);
        a.add(2,issueID);
        a.add(3,action);
        Log.i("QUEUEING ACTION", action);
        queuedActionLogs.push(a);
        logQueuedActions();//TODO: FIX THIS
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
    //         LoadedLatestActions
    //         NewsfeedClick
    //         NotificationRespondClick
    //         NotificationIgnoreClick
    //         SurveyResponse
    //         EnteredGeofence
    //         ThanksDismissed
    //         UnfollowedIssue
    public void logQueuedActions(){
           /* Insert data to a Table*/
        Iterator<ArrayList<String>> it = queuedActionLogs.iterator();
        while(it.hasNext()){
            ArrayList<String> splitAction = it.next();
            String longitude = "";
            String latitude = "";
            if (mLastLocation != null) {
                latitude = String.valueOf(mLastLocation.getLatitude());
                longitude = String.valueOf(mLastLocation.getLongitude());
            }
            myDB = this.openOrCreateDatabase(DATABASE_PATH,MODE_PRIVATE,null);
            myDB.execSQL("INSERT INTO "
                    + DB_TABLE_NAME
//                    + " (timestamp, userID, issueID, lat, long, actionType)"
                    + " VALUES ('"+splitAction.get(0)+"','"+splitAction.get(1)+"','"+splitAction.get(2)+"','"+latitude+"','"+longitude+"','"+splitAction.get(3)+"');");
        }
        queuedActionLogs.clear(); // TODO: could be a garbage collection issue
        Intent logSyncServiceIntent = new Intent(LoggerService.this,LogSyncService.class);
        logSyncServiceIntent.putExtra(LogSyncService.PARAM_SYNC_TYPE, LogSyncService.SYNC_TYPE_SEND);
        startService(logSyncServiceIntent);
    }



//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
//    {
//
//        db.execSQL("DROP TABLE IF IT EXISTS " + TableName);
//        Log.d("onUpgrade", "dropping table");
//        onCreate(db);
//    }


}

