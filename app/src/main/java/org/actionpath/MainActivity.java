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
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.issues.IssuesDbHelper;
import org.actionpath.logging.LogSyncService;
import org.actionpath.logging.LoggerService;
import org.actionpath.util.Installation;

import java.util.ArrayList;
import java.util.List;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class MainActivity extends AbstractBaseActivity {

    private Button updateIssues;

    private String TAG = this.getClass().getName();

    public static final String MY_PREFS_NAME = "PREFIDS";
    final ArrayList<String> newsfeedList = new ArrayList<>();
    final ArrayList<Integer> newsfeedIDs = new ArrayList<>();
    ListView favoritedIssueList;
    SimpleCursorAdapter favoritedIssueDataAdaptor;

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

        buildGeofences();   // add in geofences for any new issues

        updateIssues = (Button) findViewById(R.id.update);
        updateIssues.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent logIntent = LoggerService.intentOf(MainActivity.this, LoggerService.NO_ISSUE, LoggerService.ACTION_LOADED_LATEST_ISSUES);
                startService(logIntent);
                new Thread(new Runnable() {
                    public void run() {
                        Log.d(TAG, "Loading new issues");
                        ArrayList<Issue> newIssues = ActionPathServer.getNewIssues();
                        IssuesDataSource dataSource = IssuesDataSource.getInstance();
                        for(Issue i:newIssues){
                            dataSource.insertOrUpdateIssue(i);
                        }
                        Log.d(TAG, "Pulled " + newIssues.size() + " new issues from the server");
                        buildGeofences();
                    }
                }).start();
            }
        });

        displayFavoritedListView();

        Intent i= new Intent(this, LogSyncService.class);
        this.startService(i);

    }

    private void displayFavoritedListView(){

        Log.i(TAG, "Favorited Issues: " + IssuesDataSource.getInstance(this).countFavoritedIssues());

        String[] fromColumns = new String[] { IssuesDbHelper.ISSUES_SUMMARY_COL,
                IssuesDbHelper.ISSUES_DESCRIPTION_COL };

        int[] toTextViews = new int[] {R.id.issue_summary, R.id.issue_description };

        favoritedIssueList = (ListView) findViewById(R.id.newsfeed);

        favoritedIssueDataAdaptor = new SimpleCursorAdapter(
                this, R.layout.issue_list_item,
                IssuesDataSource.getInstance(this).getFavoritedIssuesCursor(),
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
        testIssue1.setRadius(Cambridge_rad);
        testIssue1.setTest(true);
        Issue testIssue2 = new Issue(2345, "Acknowledged", "Pothole", "Pothole on the corner of Mass Ave and Vassar.", Cambridge_lat2, Cambridge_long2, "Massachusetts Ave./Vassar St., Cambridge, Massachusetts", "", null, null, 9841);
        testIssue2.setRadius(Cambridge_rad);
        testIssue2.setTest(true);
        Log.d(TAG, "added test issues");
        IssuesDataSource dataSource = IssuesDataSource.getInstance(this);
        dataSource.insertOrUpdateIssue(testIssue1);
        dataSource.updateIssueFavorited(1234, true);
        dataSource.insertOrUpdateIssue(testIssue2);
        dataSource.updateIssueFavorited(2345, true);
        long issueCount = dataSource.getIssueCount();
        Log.i(TAG, issueCount + " issues in the db");
    }

    public void onStop() {
        super.onStop();
    }

    /**
     * Build geofences for all non-geofenced issues in the database
     * TODO: consider filtering for closed issues, and remember we can only do 100 total
     */
    private void buildGeofences(){
        Log.d(TAG, "Building geofences");
        Cursor cursor = IssuesDataSource.getInstance(this).getNonGeoFencedIssuesCursor();
        while (!cursor.isAfterLast()) {
            int issueId = cursor.getInt(0);
            Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
            buildGeofence(issueId, cursor.getDouble(1), cursor.getDouble(2), issue.getRadius());
            IssuesDataSource.getInstance(this).updateIssueGeofenceCreated(issueId,true);
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

