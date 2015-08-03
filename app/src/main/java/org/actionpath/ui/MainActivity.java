package org.actionpath.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.google.android.gms.location.Geofence;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.actionpath.R;
import org.actionpath.geofencing.GeofencingRegisterer;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogSyncService;
import org.actionpath.util.Installation;

import java.util.ArrayList;
import java.util.List;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class MainActivity extends AbstractBaseActivity implements
        IssuesFragmentList.OnIssueSelectedListener, PickPlaceFragmentList.OnPlaceSelectedListener {

    private Button updateIssues;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private static String TAG = MainActivity.class.getName();

    public static final String PREFS_NAME = "ActionPathPrefs";
    public static final String PREF_PLACE_ID = "placeId";
    private static int INVALID_PLACE_ID = -1;

    ListView favoritedIssueList;
    SimpleCursorAdapter favoritedIssueDataAdaptor;

    protected int placeId = INVALID_PLACE_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        // create the issue database
        addTestIssues();

        // create an image loader instance
        if(!ImageLoader.getInstance().isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
            ImageLoader.getInstance().init(config);
        }
        setContentView(R.layout.activity_main);

        // setup the UI
        // @see http://www.android4devs.com/2015/06/navigation-view-material-design-support.html
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                    case R.id.nav_my_issues:
                        displayIssuesListFragment(IssuesFragmentList.FOLLOWED_ISSUES);
                        return true;
                    case R.id.nav_all_issues:
                        displayIssuesListFragment(IssuesFragmentList.ALL_ISSUES);
                        return true;
                    case R.id.nav_update_issues:
                        updateIssues();
                        return true;
                    case R.id.nav_pick_place:
                        displayPickPlaceFragment();
                        return true;
                    case R.id.nav_about:
                        // TODO
                        return true;
                    default:
                        Log.e(TAG,"Got an unknown selection from nav drawer menu :-(");
                        return true;
                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open, R.string.drawer_close){
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        Intent i= new Intent(this, LogSyncService.class);
        this.startService(i);

        // check that we have a place selected
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        placeId = settings.getInt(PREF_PLACE_ID,INVALID_PLACE_ID);
        if(placeId==INVALID_PLACE_ID){
            Log.w(TAG,"No place set yet");
            displayPickPlaceFragment();
        }

        if (!Installation.hasId()) {
            logMsg(LogMsg.ACTION_INSTALLED_APP);
        }

    }

    private void updateIssues(){
        new Thread(new Runnable() {
            public void run() {
                logMsg(LogMsg.ACTION_LOADED_LATEST_ISSUES);
                Log.d(TAG, "Loading new issues");
                ArrayList<Issue> newIssues = ActionPathServer.getLatestIssues(placeId);
                IssuesDataSource dataSource = IssuesDataSource.getInstance();
                for(Issue i:newIssues){
                    dataSource.insertOrUpdateIssue(i);
                }
                Log.d(TAG, "Pulled " + newIssues.size() + " issues from the server");
                buildGeofences();
            }
        }).start();
    }

    private void displayPickPlaceFragment(){
        PickPlaceFragmentList fragment = PickPlaceFragmentList.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_content, fragment);
        fragmentTransaction.commit();
    }

    private void displayIssuesListFragment(int type){
        IssuesFragmentList fragment = IssuesFragmentList.newInstance(type);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_content, fragment);
        fragmentTransaction.commit();
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
        dataSource.updateIssueFollowed(1234, true);
        dataSource.insertOrUpdateIssue(testIssue2);
        dataSource.updateIssueFollowed(2345, true);
        long issueCount = dataSource.getIssueCount(getPlaceId());
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
        Cursor cursor = IssuesDataSource.getInstance(this).getNonGeoFencedIssuesCursor(getPlaceId());
        while (!cursor.isAfterLast()) {
            int issueId = cursor.getInt(0);
            Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
            buildGeofence(issueId, cursor.getDouble(1), cursor.getDouble(2), issue.getRadius());
            IssuesDataSource.getInstance(this).updateIssueGeofenceCreated(issueId, true);
            logMsg(issueId, LogMsg.ACTION_ADDED_GEOFENCE);
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

    @Override
    public void onIssueSelected(int issueId) {
        Log.d(TAG, "clicked item with id: " + issueId);
        logMsg(issueId, LogMsg.FOLLOWED_ISSUE_CLICK);
        // Then you start a new Activity via Intent
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, ResponseActivity.class);
        intent.putExtra(ResponseActivity.PARAM_ISSUE_ID, issueId);
        startActivity(intent);
    }

    @Override
    public void onPlaceSelected(int placeId) {
        Log.d(TAG, "clicked place id: " + placeId);
        // now save that we set the place
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(MainActivity.PREF_PLACE_ID, placeId);
        editor.apply();
        Log.i(TAG, "Saved place " + placeId);
        // and jump back to the issue list
        displayIssuesListFragment(IssuesFragmentList.ALL_ISSUES);
    }

    public int getPlaceId(){
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        return settings.getInt(MainActivity.PREF_PLACE_ID,INVALID_PLACE_ID);
    }

}

