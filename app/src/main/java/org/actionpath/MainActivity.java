package org.actionpath;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.actionpath.geofencing.GeofencingRegisterer;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssueDatabase;
import org.actionpath.logging.LoggerService;
import org.actionpath.util.Installation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class MainActivity extends Activity{

    public static final String PREF_INSTALL_ID = "installationId";
    public static final int DEFAULT_INSTALL_ID = 0;

    //public static final String SERVER_BASE_URL = "https://api.dev.actionpath.org";
    public static final String SERVER_BASE_URL = "http://action-path-server.rahulbot.c9.io"; // test server

    private Button updateGeofences;

    private String TAG = this.getClass().getName();

    private IssueDatabase issueDB;

    public static final String MY_PREFS_NAME = "PREFIDS";
    final ArrayList<String> newsfeedList = new ArrayList<>();
    final ArrayList<Integer> newsfeedIDs = new ArrayList<>();
    ListView listview;
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
        Log.i(TAG,"onCreate");
        if(!Installation.hasId()){
            // CREATE AN ACTION LOG
            Intent loggerServiceIntent = new Intent(MainActivity.this,LoggerService.class);
            loggerServiceIntent.putExtra(LoggerService.PARAM_LOG_TYPE, LoggerService.LOG_TYPE_ACTION);
            loggerServiceIntent.putExtra(LoggerService.PARAM_INSTALL_ID, String.valueOf(Installation.id(this)));
            loggerServiceIntent.putExtra(LoggerService.PARAM_ISSUE_ID, "");
            loggerServiceIntent.putExtra(LoggerService.PARAM_ACTION, LoggerService.ACTION_INSTALLED_APP);
            startService(loggerServiceIntent);
        }
        // create the issue database
        issueDB = IssueDatabase.getInstance();
        // create an image loader instance
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        setContentView(R.layout.home_page);

        for(Issue issue : issueDB.getAll()){
            if(issue.isTest()) {
                buildGeofence(issue.getLatitude(), issue.getLongitude(), issue.getRadius(), issue.getId());
            }
        }

        updateGeofences = (Button) findViewById(R.id.update);
        final Context appContext = this.getApplicationContext();
        updateGeofences.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent logIntent = LoggerService.intentOf(appContext,LoggerService.NO_ISSUE,LoggerService.ACTION_LOADED_LATEST_ISSUES);
                startService(logIntent);
                Log.d(TAG, "load new issues");
                IssueDatabase.getInstance().loadNewIssues();
                buildGeofences();
            }
        });

        // follow the test issue by default
        int testIssueId = 1234;
        Issue testIssue = issueDB.get(testIssueId);
        if(testIssue!=null){
            String testIssueSummary = testIssue.getIssueSummary();
            newsfeedList.add(testIssueSummary);
            newsfeedIDs.add(testIssueId);
        }

        listview = (ListView) findViewById(R.id.newsfeed);

        if (mString != ""){
            List<String> nums = Arrays.asList(mString.split(","));
            Log.d(TAG, nums.get(0));
            for (String num: nums){
                Integer old_id = Integer.getInteger(num);
                Issue issue = issueDB.get(old_id);
                String issue_summary = issue.getIssueSummary();
                newsfeedList.add(issue_summary);
                newsfeedIDs.add(old_id);
            }
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, newsfeedList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                int issueID = newsfeedIDs.get(position);
                Log.d(TAG, "YOU CLICKED ITEM with id: "+ issueID);
                Log.d(TAG, "YOU CLICKED ITEM with position: "+ position);
                Log.i("HelloListView", "You clicked Item: " + id);

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = LoggerService.intentOf(MainActivity.this, issueID, LoggerService.ACTION_NEWS_FEED_CLICK);
                startService(loggerServiceIntent);
                Log.d(TAG,"NewsfeedClick AlertTest");

                // Then you start a new Activity via Intent
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ResponseActivity.class);
                intent.putExtra("issueID", issueID);
                startActivity(intent);
            }

        });

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
        String string_ids = "";
        for (Integer each: newsfeedIDs){
            string_ids.concat(each.toString()+",");
        }
        editor.putString("newsfeedSaved", string_ids).commit();
        editor.commit();
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }

    /**
     * Build geofences for all the issues in the database
     * TODO: consider filtering for closed issues, and remember we can only do 100 total
     */
    private void buildGeofences(){
        for(Issue issue : IssueDatabase.getInstance().getAll()) {
            buildGeofence(issue.getLatitude(), issue.getLongitude(), Issue.DEFAULT_RADIUS, issue.getId());
            Intent loggerServiceIntent = LoggerService.intentOf(this,issue.getId(),LoggerService.ACTION_ADDED_GEOFENCE);
            startService(loggerServiceIntent);
        }
    }

    private void buildGeofence(double latitude, double longitude, float radius, int id){
        List<Geofence> newGeoFences = new ArrayList<>();
        Geofence.Builder geofenceBuilder = new Geofence.Builder();
        geofenceBuilder.setRequestId((new Integer(id)).toString());
        geofenceBuilder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER);
        geofenceBuilder.setCircularRegion(latitude, longitude, radius);
        geofenceBuilder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        GeofencingRegisterer registerer= new GeofencingRegisterer(this);
        newGeoFences.add(geofenceBuilder.build());
        registerer.registerGeofences(newGeoFences);
    }

}

