package org.actionpath.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.net.Uri;

import org.actionpath.R;
import org.actionpath.db.AbstractSyncableDataSource;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.util.Development;
import org.actionpath.util.DeviceUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class MainActivity extends AbstractLocationActivity implements
        IssuesFragmentList.OnIssueSelectedListener, PickPlaceFragmentList.OnPlaceSelectedListener,
        UpdateIssuesFragment.OnIssuesUpdatedListener, AboutFragment.OnFragmentInteractionListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    private static String TAG = MainActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        // setup the UI
        // @see http://www.android4devs.com/2015/06/navigation-view-material-design-support.html
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        navView = (NavigationView) findViewById(R.id.navigation_view);

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

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
                        logMsg(LogMsg.ACTION_CLICKED_HOME);
                    case R.id.nav_my_issues:
                        logMsg(LogMsg.ACTION_CLICKED_MY_ISSUES);
                        displayIssuesListFragment(IssuesFragmentList.FOLLOWED_ISSUES);
                        return true;
                    case R.id.nav_all_issues:
                        logMsg(LogMsg.ACTION_CLICKED_ALL_ISSUES);
                        displayIssuesListFragment(IssuesFragmentList.ALL_ISSUES);
                        return true;
                    case R.id.nav_update_issues:
                        logMsg(LogMsg.ACTION_CLICKED_UPDATE_ISSUES);
                        displayUpdateIssuesFragment();
                        return true;
                    /*
                    case R.id.nav_pick_place:
                        logMsg(LogMsg.ACTION_CLICKED_PICK_PLACE);
                        displayPickPlaceFragment();
                        return true;
                        */
                    case R.id.nav_about:
                        logMsg(LogMsg.ACTION_CLICKED_ABOUT);
                        displayAboutFragment();
                        return true;
                    case R.id.nav_save_debug_info:
                        logMsg(LogMsg.ACTION_SAVING_DEBUG_INFO);
                        try {
                            ArrayList<String> filePaths = new ArrayList<String>();
                            String logsFilePath = saveUnsyncedRecordsToFileSystem(LogsDataSource.getInstance(), "logs.json");
                            String responsesFilePath = saveUnsyncedRecordsToFileSystem(ResponsesDataSource.getInstance(), "responses.json");
                            if (logsFilePath != null) filePaths.add(logsFilePath);
                            if (responsesFilePath != null) filePaths.add(responsesFilePath);
                            Snackbar.make(findViewById(R.id.main_content), R.string.backup_unsynced_records_worked, Snackbar.LENGTH_SHORT).show();
                            sendEmailWithUnsyncedRecords(filePaths);
                        } catch (IOException ioe) {
                            Log.e(TAG, "Unable to backup unsynced records " + ioe.toString());
                            Snackbar.make(findViewById(R.id.main_content), R.string.backup_unsynced_records_failed, Snackbar.LENGTH_SHORT).show();
                        }
                        displayIssuesListFragment(IssuesFragmentList.FOLLOWED_ISSUES);
                        return true;
                    default:
                        Log.e(TAG, "Got an unknown selection from nav drawer menu :-(");
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
    }

    private void sendEmailWithUnsyncedRecords(ArrayList<String> filePaths) {
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for (String file : filePaths) {
            File fileIn = new File(file);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, "erhardt@media.mit.edu");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "backups from " + getInstallId());
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        startActivity(emailIntent);
    }

    private String saveUnsyncedRecordsToFileSystem(AbstractSyncableDataSource dataSource, String fileName) throws IOException {
        if(dataSource.countDataToSync()==0){
            return null;
        }
        // assemble the json
        String jsonToSave = dataSource.getUnsyncedRecordsAsJson().toString();
        // write it out
        File file = getPublicBackupFile(fileName);
        if(file==null){
            throw new IOException("Unable to create file");
        }
        PrintWriter printWriter = new PrintWriter(file);
        printWriter.write(jsonToSave);
        printWriter.close();
        Log.i(TAG, "Wrote unsynced records to " + file.getCanonicalPath());
        return file.getAbsolutePath();
    }

    private File getPublicBackupFile(String name){
        if(!DeviceUtil.isExternalStorageWritable()){
            Log.e(TAG, "Tried to saveUnsyncedRecordsToFileSystem but file system isn't writable - fail");
            return null;
        }
        String path = Environment.getExternalStorageDirectory().toString();
        String timestamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
        File file = new File(path,getInstallId()+"-"+timestamp+"-"+name);
        Log.i(TAG,"File created at "+file.getAbsolutePath());
        return file;
    }

    @Override
    public void onResume(){
        super.onResume();
        // On first load check to see if we have a place selected if so load My Actions Page
        if(!(getPlaceId()==INVALID_PLACE_ID)){
            displayIssuesListFragment(IssuesFragmentList.FOLLOWED_ISSUES);
        } else {
            Log.w(TAG,"No place set yet");
            Log.i(TAG, "Defaulting to Mexico City");
            onPlaceSelected(Development.PLACE_MEXICO_CITY_ID, Development.PLACE_MEXICO_CITY_NAME);
            displayUpdateIssuesFragment();
        }
        // now update the dynamic nav menu text
        long responsesToUpload = ResponsesDataSource.getInstance(this).countDataToSync() + ResponsesDataSource.getInstance(this).countDataNeedingLocation();
        long logsToUpload = LogsDataSource.getInstance(this).countDataToSync() + LogsDataSource.getInstance(this).countDataNeedingLocation();
        if((responsesToUpload + logsToUpload) > 0){
            MenuItem debugMenuItem = (MenuItem) navView.getMenu().findItem(R.id.nav_save_debug_info);
            String strToFormat = getResources().getString(R.string.nav_save_debug_info);
            String formattedStr = String.format(strToFormat, logsToUpload, responsesToUpload);
            debugMenuItem.setTitle(formattedStr);
        }
    }

    private void displayFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_content, fragment);
        fragmentTransaction.commit();
    }

    private void displayUpdateIssuesFragment(){
        toolbar.setTitle(String.format(getResources().getString(R.string.update_issues_header), getPlaceName()));
        logMsg(LogMsg.ACTION_LOADED_LATEST_ISSUES);
        UpdateIssuesFragment fragment = UpdateIssuesFragment.newInstance(getPlaceId());
        displayFragment(fragment);
    }

    private void displayPickPlaceFragment(){
        toolbar.setTitle(R.string.pick_place_header);
        PickPlaceFragmentList fragment = PickPlaceFragmentList.newInstance();
        displayFragment(fragment);
    }

    private void displayIssuesListFragment(int type){
        switch(type){
            case IssuesFragmentList.ALL_ISSUES:
                toolbar.setTitle(String.format(getResources().getString(R.string.all_issues_header), getPlaceName()));
                break;
            case IssuesFragmentList.FOLLOWED_ISSUES:
                toolbar.setTitle(String.format(getResources().getString(R.string.followed_issues_header), getPlaceName()));
                break;
        }
        IssuesFragmentList fragment = IssuesFragmentList.newInstance(type);
        displayFragment(fragment);
    }

    private void displayAboutFragment(){
        toolbar.setTitle(R.string.about_header);
        AboutFragment fragment = AboutFragment.newInstance();
        displayFragment(fragment);
    }

    public void onStop() {
        super.onStop();
    }

    @Override
    public void onIssueSelected(int issueId) {
        Log.d(TAG, "clicked item with id: " + issueId);
        logMsg(issueId, LogMsg.ACTION_CLICKED_ON_ISSUE_IN_LIST);
        // Then you start a new Activity via Intent
        Intent intent = new Intent()
            .setClass(MainActivity.this, IssueDetailActivity.class)
            .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, issueId)
            .putExtra(IssueDetailActivity.PARAM_FROM_SURVEY_NOTIFICATION, false);
        startActivity(intent);
    }

    private void savePlaceIdAndName(int placeId, String placeName){
        Log.i(TAG, "Set place to: " + placeId+" = "+placeName);
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(MainActivity.PREF_PLACE_ID, placeId);
        editor.putString(MainActivity.PREF_PLACE_NAME, placeName);
        editor.apply();
        logMsg(LogMsg.ACTION_PICKED_PLACE);
    }

    @Override
    public void onPlaceSelected(int placeId, String placeName) {
        Log.d(TAG, "clicked place id: " + placeId);
        // now save that we set the place
        savePlaceIdAndName(placeId,placeName);
        // and jump to update the issues
        displayUpdateIssuesFragment();
    }

    @Override
    public void onIssuesUpdateSucceeded(int newIssueCount){
        String feedback = getResources().getQuantityString(R.plurals.updated_issues, newIssueCount, newIssueCount);
        Snackbar.make(findViewById(R.id.main_content), feedback, Snackbar.LENGTH_SHORT).show();
        displayIssuesListFragment(IssuesFragmentList.ALL_ISSUES);
    }

    @Override
    public void onIssueUpdateFailed(){
        Snackbar.make(findViewById(R.id.main_content), R.string.issues_update_failed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onFollowedIssueStatusChanged(int issueId, String oldStatus, String newStatus){
        Log.d(TAG,"status change alert on issue "+issueId+": "+oldStatus+" -> "+newStatus);
        // Fire a low priority notification
        sendNotification(issueId, newStatus);

        // TODO: Mark issue as having something "new" on fav list (a UI indication)
    }

    /**
     * Posts a notification in the notification bar when a update is called.
     * If the user clicks the notification, control goes to the Main Activity.
     * param transitionType The type of transition that occurred.
     * For now, ActionPath only handles enter transitionTypes
     */
    private void sendNotification(int issueId, String newStatus) {
        Log.d(TAG,"Sending notification for issueId: "+issueId);

        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String updateSummary = newStatus + ": " + issue.getIssueSummary();

        PendingIntent pi = getPendingIntent(issueId);

        // create the notification
        Notification.Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder
                .setPriority(Notification.PRIORITY_LOW)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getResources().getString(R.string.update_notification))
                .setContentText(updateSummary)
                .setContentIntent(pi);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.notify(1, notification);
    }

    private PendingIntent getPendingIntent(int issueId) {
        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String summary = issue.getIssueSummary();

        Log.v(TAG, "Returning update intent for IssueDetailActivity.class for issue: " + summary);

        Intent updateIntent = new Intent(this, IssueDetailActivity.class)
                .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, issueId)
                .putExtra(IssueDetailActivity.PARAM_FROM_UPDATE_NOTIFICATION, true)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 0, updateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

}

