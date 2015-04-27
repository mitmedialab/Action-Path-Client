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
import com.google.android.gms.location.LocationClient;

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

    Location mCurrentLocation;
    LocationClient mLocationClient;

    Stack<ArrayList<String>> queuedActionLogs;

    String storagePath = "/Android/data/action_path";
    String storageFile = "geodata.txt";

    SQLiteDatabase myDB= null;
    String TableName = "eventTable";

    public LoggerService(){
        super("LoggerService");
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mLocationClient = new LocationClient(this, this, this);
        queuedActionLogs = new Stack<ArrayList<String>>();



        String Data="";

  /* Create a Database. */
        try {
            myDB = this.openOrCreateDatabase("DatabaseName", MODE_PRIVATE, null);

   /* Create a Table in the Database. */
            myDB.execSQL("CREATE TABLE IF NOT EXISTS "
                    + TableName
                    + " (timestamp TEXT, userID TEXT, geofenceID TEXT, lat TEXT, long TEXT, actionType TEXT);");

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
                myDB.close();
        }


    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // TODO Auto-generated method stub

        Bundle extras = intent.getExtras();
        String logType = intent.getStringExtra("logType");
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
//            LoadedLatestActions
//    - timestamp, user id, GPS coordinates
//                              Entered (Geofence)
//    - timestamp, user id, geofence id, GPS coordinates
//    NotificationClick
//    - timestamp, user id, geofence id, GPS coordinates
//    SurveyResponse
//    - timestamp, user id, geofence id, GPS coordinates
//    ThanksDismissed / Unfollowed Issue
//    - timestamp, user id, geofence id
    // need: timestamp, user id, geofence id, gps coords, action
    public void logQueuedActions(){
           /* Insert data to a Table*/
        Iterator<ArrayList<String>> it = queuedActionLogs.iterator();
        while(it.hasNext()){
            ArrayList<String> splitAction = it.next();
            Issue issue = AlertTest.getIssue(Integer.valueOf(splitAction.get(2)));
            String longitude = String.valueOf(issue.getLatitude());
            String latitude = String.valueOf(issue.getLongitude());
            myDB.execSQL("INSERT INTO "
                    + TableName
                    + " (timestamp, userID, geofenceID, lat, long, actionType)"
                    + " VALUES ("+splitAction.get(0)+", "+splitAction.get(1)+", "+splitAction.get(2)+", "+latitude+", "+longitude+", "+splitAction.get(3)+ ");");
        }
        queuedActionLogs.clear(); // TODO: could be a garbage collection issue
    }

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

