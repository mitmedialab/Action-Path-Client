package com.example.kimberlyleon1.actionpath;

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

    Location mLastLocation;
    LocationClient mLocationClient;

    Stack<ArrayList<String>> queuedActionLogs;

    String storagePath = "/Android/data/action_path";
    String storageFile = "geodata.txt";

    SQLiteDatabase myDB= null;
    private static final String TableName = "Actions";

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
        Intent intent=new Intent("com.sample.service.serviceClass");
        this.startService(intent);


  /* Create a Database. */
        try {
            myDB = this.openOrCreateDatabase("DatabaseName", MODE_PRIVATE, null);
   /* Create a Table in the Database. */
            myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                    + TableName
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
            Log.e("Error", "Error", e);
        } finally {
            if (myDB != null)
                Log.e("Error", "Error finally");
                myDB.close();
        }


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("LOGGERSERVICE","NEW THING");
        onHandleIntent(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub

        Log.e("LOGGERSERVICE","LOGGERSERVICEHGALSDKFLAKSDFMALSD");
        String logType = intent.getStringExtra("logType");
        Log.e("logtype",logType);
        Log.i("LoggerService", logType);
        if(logType.equals("action")) {
            String userID = String.valueOf(intent.getStringExtra("userID"));
            String issueID = intent.getStringExtra("issueID");
            String action = intent.getStringExtra("action");
            Log.i("LoggerAction", action);
            queueAction(userID, issueID, action);
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
            myDB = this.openOrCreateDatabase("DatabaseName", MODE_PRIVATE, null);
            myDB.execSQL("INSERT INTO "
                    + TableName
//                    + " (timestamp, userID, issueID, lat, long, actionType)"
                    + " VALUES ('"+splitAction.get(0)+"','"+splitAction.get(1)+"','"+splitAction.get(2)+"','"+latitude+"','"+longitude+"','"+splitAction.get(3)+"');");
        }
        queuedActionLogs.clear(); // TODO: could be a garbage collection issue
        Intent logSyncServiceIntent = new Intent(LoggerService.this,LogSyncService.class);
        logSyncServiceIntent.putExtra("syncType", "send");
        startService(logSyncServiceIntent);
    }



//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
//    {
//
//        db.execSQL("DROP TABLE IF IT EXISTS " + TableName);
//        Log.d("onUpgrade", "dropping table");
//        onCreate(db);
//    }

    /// LOG CURRENT LOCATION TO A FILE
    public void logCurrentLocation(String timestamp, String action, String data, String latitude, String longitude){
        try{
            String root = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
            File dir = new File(root + storagePath);
            Log.i("LogCurrentLocation",root);
            if(dir.mkdirs() || dir.isDirectory()){

                FileWriter write = new FileWriter(root + storagePath + File.separator + storageFile, true);
                String line = timestamp + "," + action + "," + data +
                        "," + latitude + "," + longitude+"\n";
                Log.i("LogCurrentLocation",line);
                write.append(line);
                write.flush();
                write.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }



}

