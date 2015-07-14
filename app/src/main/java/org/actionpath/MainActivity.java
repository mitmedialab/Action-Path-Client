package org.actionpath;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.location.Geofence;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.actionpath.geofencing.GeofencingRegisterer;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssueManager;
import org.actionpath.logging.LoggerService;
import org.actionpath.util.Installation;

import java.util.ArrayList;
import java.util.List;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class MainActivity extends AbstractBaseActivity {

    public static final String PREF_INSTALL_ID = "installationId";
    public static final int DEFAULT_INSTALL_ID = 0;

    //public static final String SERVER_BASE_URL = "https://api.dev.actionpath.org";
    public static final String SERVER_BASE_URL = "http://action-path-server.rahulbot.c9.io"; // test server

    private Button updateGeofences;

    private String TAG = this.getClass().getName();

    private IssueManager issueDB;

    public static final String MY_PREFS_NAME = "PREFIDS";
    final ArrayList<String> newsfeedList = new ArrayList<>();
    final ArrayList<Integer> newsfeedIDs = new ArrayList<>();
    ListView favoritedIssueList;
    SimpleCursorAdapter favoritedIssueDataAdaptor;
    String mString = "";

//
//    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//
//    String restoredText = prefs.getString("text", null);

//    if (prefs != null) {
//        mString = prefs.getString("name", "No name defined");//"No name defined" is the default value.
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        if (!Installation.hasId()) {
            // Create an Action Log for new installation
            Intent logIntent = LoggerService.intentOf(MainActivity.this, LoggerService.NO_ISSUE, LoggerService.ACTION_INSTALLED_APP);
            startService(logIntent);
        }
        // create the issue database
        addTestIssues();
        // create an image loader instance
        if(!ImageLoader.getInstance().isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
            ImageLoader.getInstance().init(config);
        }
        setContentView(R.layout.home_page);

        for (Issue issue : DatabaseManager.getInstance().getAllIssues()) {
            if (issue.isTest()) {
                buildGeofence(issue.getId(),issue.getLatitude(), issue.getLongitude(), issue.getRadius());
            }
        }

        updateGeofences = (Button) findViewById(R.id.update);
        updateGeofences.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent logIntent = LoggerService.intentOf(MainActivity.this, LoggerService.NO_ISSUE, LoggerService.ACTION_LOADED_LATEST_ISSUES);
                startService(logIntent);
                new Thread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "Loading new issues");
                        IssueManager.loadNewIssues();
                        Log.d(TAG, "Building geofences");
                        buildGeofences();
                    }
                }).start();
            }
        });

        // follow the test issue by default
        int testIssueId = 1234;
        Issue testIssue = issueDB.getById(testIssueId);
        if (testIssue != null) {
            String testIssueSummary = testIssue.getIssueSummary();
            newsfeedList.add(testIssueSummary);
            newsfeedIDs.add(testIssueId);
        }

        displayFavoritedListView();

    }

    private void displayFavoritedListView(){

        Log.i(TAG,"Favorited Issues: "+DatabaseManager.getInstance().countFavoritedIssues());

        String[] fromColumns = new String[] { DatabaseManager.ISSUES_SUMMARY_COL,
                DatabaseManager.ISSUES_DESCRIPTION_COL };

        int[] toTextViews = new int[] {R.id.issue_summary, R.id.issue_description };

        favoritedIssueList = (ListView) findViewById(R.id.newsfeed);

        favoritedIssueDataAdaptor = new SimpleCursorAdapter(
                this, R.layout.issue_list_item,
                DatabaseManager.getInstance().getFavoritedIssues(),
                fromColumns,
                toTextViews,
                0);

        favoritedIssueList.setAdapter(favoritedIssueDataAdaptor);

        favoritedIssueList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> theListView, final View view,
                                    int position, long id) {
                Cursor cursor = (Cursor) theListView.getItemAtPosition(position);
                int issueId = (int) id;
                Log.d(TAG, "clicked item with id: " + issueId + " @ position " + position);
                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = LoggerService.intentOf(MainActivity.this, issueId, LoggerService.ACTION_NEWS_FEED_CLICK);
                startService(loggerServiceIntent);
                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ResponseActivity.class);
                intent.putExtra(ResponseActivity.PARAM_ISSUE_ID, issueId);
                startActivity(intent);
            }

        });

    }

    private void addTestIssues(){
        final double Cambridge_lat = 42.359254;
        final double Cambridge_long = -71.093667;
        final float Cambridge_rad = 1601;
        final double Cambridge_lat2 = 42.359255;
        final double Cambridge_long2 = -71.093666;
        Issue testIssue1 = new Issue(1234, "Acknowledged", "Toy Train Hack", "Giant Toy Train hack on Kendall Square T entrance.", Cambridge_lat, Cambridge_long, "350 Main Street, Cambridge, Massachusetts", "", null, null, 9841);
        testIssue1.setTest(true);
        Issue testIssue2 = new Issue(2345, "Acknowledged", "Pothole", "Pothole on the corner of Mass Ave and Vassar.", Cambridge_lat, Cambridge_long, "Massachusetts Ave./Vassar St., Cambridge, Massachusetts", "", null, null, 9841);
        testIssue2.setTest(true);
        Log.d(TAG, "added test issues");
        DatabaseManager db = DatabaseManager.getInstance(this);
        db.insertIssue(testIssue1);
        db.updateIssueFavorited(1234, true);
        db.insertIssue(testIssue2);
        db.updateIssueFavorited(2345, true);
        int issueCount = db.getIssueCount();
        Log.i(TAG, issueCount + " issues in the db");
    }

    public void onStop() {
        saveArray();
        super.onStop();
    }

    /**
     * Save all the issues that you've followed to a local prefs file so we don't have to ask the
     * server for them each time (?)
     */
    public void saveArray() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        String stringIds = "";
        for (Integer each: newsfeedIDs){
            stringIds.concat(each.toString()+",");
        }
        editor.putString("newsfeedSaved", stringIds).commit();
        editor.commit();
    }

    /**
     * Build geofences for all non-geofenced issues in the database
     * TODO: consider filtering for closed issues, and remember we can only do 100 total
     */
    private void buildGeofences(){
        Cursor cursor = DatabaseManager.getInstance().getNonGeoFencedIssues();
        while (cursor.isAfterLast() == false) {
            int issueId = cursor.getInt(0);
            buildGeofence(issueId, cursor.getDouble(1), cursor.getDouble(2), Issue.DEFAULT_RADIUS);
            DatabaseManager.getInstance().updateIssueGeofenceCreated(issueId,true);
            Intent loggerServiceIntent = LoggerService.intentOf(this,issueId,LoggerService.ACTION_ADDED_GEOFENCE);
            startService(loggerServiceIntent);
            cursor.moveToNext();
        }
        cursor.close();
    }

    private void buildGeofence(int issueId, double latitude, double longitude, float radius){
        List<Geofence> newGeoFences = new ArrayList<>();
        Geofence.Builder geofenceBuilder = new Geofence.Builder();
        geofenceBuilder.setRequestId((new Integer(issueId)).toString());
        geofenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        geofenceBuilder.setCircularRegion(latitude, longitude, radius);
        geofenceBuilder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        GeofencingRegisterer registerer= new GeofencingRegisterer(this);
        newGeoFences.add(geofenceBuilder.build());
        registerer.registerGeofences(newGeoFences);
    }

}

