package org.actionpath.ui;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TextView;

import org.actionpath.R;
import org.actionpath.db.RequestType;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.places.Place;
import org.actionpath.tasks.UpdateIssuesAsyncTask;
import org.actionpath.util.Config;
import org.actionpath.util.Development;
import org.actionpath.util.GoogleApiClientNotConnectionException;
import org.actionpath.util.Preferences;

/**
 * The entry point for the app, handles menu nav too
 */
public class MainActivity extends AbstractLocationBaseActivity implements
        IssuesListFragment.OnIssueSelectedListener, PickPlaceListFragment.OnPlaceSelectedListener,
        UpdateIssuesAsyncTask.OnIssuesUpdatedListener, AboutFragment.OnDisplayExternalURLListener,
        AssignRequestTypeFragment.OnRequestTypeAssignedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private UpdateIssuesAsyncTask updateIssuesTask;

    public static String PARAM_FRAGMENT_MENU_ID = "fragmentMenuIdToShow";
    public static String PARAM_FROM_UPDATE_NOTIFICATION = "cameFromAnUpdatedIssuesNotification";

    private static String TAG = MainActivity.class.getName();

    public static String ISSUE_CHANGE_NOTIFICATION_TAG = "issueChange";
    private Preferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContextWrapper contextWrapper = this;

        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        this.prefs = Preferences.getInstance(this);

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

                // show an alert that they need to pick a place first if they don't have one
                if (!Preferences.getInstance(contextWrapper).hasPlace() && menuItem.getItemId() != R.id.nav_about && menuItem.getItemId() != R.id.nav_home) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(R.string.need_to_pick_place)
                            .setTitle(R.string.error_dialog_title)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    // Add the buttons
                    dialog.show();
                    return false;
                }
                int menuItemId = menuItem.getItemId();
                return handleMenuItemClick(menuItemId);

            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
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

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        updateNavBarHeaderText();

        // handle the case where this was started from an intent (ie. from a notification)
        boolean startedWithFragmentToShow = false;
        Intent intent  = getIntent();
        if(intent!=null) {
            Bundle bundle = null;
            bundle = getIntent().getExtras();
            if(bundle!=null) {
                if (bundle.containsKey(PARAM_FRAGMENT_MENU_ID)) {
                    int menuItemToClick = bundle.getInt(PARAM_FRAGMENT_MENU_ID);
                    Log.d(TAG,"Going to main activity from notification "+menuItemToClick);
                    boolean cameFromUpdateNotification = bundle.getBoolean(PARAM_FROM_UPDATE_NOTIFICATION);
                    if (cameFromUpdateNotification) {
                        logMsg(LogMsg.ACTION_CLICKED_ON_UPDATED_ISSUES_NOTIFICATION);
                    }
                    handleMenuItemClick(menuItemToClick);
                    startedWithFragmentToShow = true;
                }
            }
        }
        if(!startedWithFragmentToShow){
            Log.v(TAG,"Starting with default appropriate home fragment");
            if(prefs.hasPlace()) {
                showAppropriateHomeFragment();
            }   // if not, onResume will show the right UI
        }
        // automatically update issues (if we have all the info we need to)
        /*if( (Config.getInstance().isPickPlaceMode() && (getPlace() == null))
                || (Config.getInstance().isAssignRequestTypeMode() && getAssignedRequestType()!=null)){
            updateIssues();
        } else {
            Log.v(TAG,"not enough info to update issues now, skipping");
        }*/

    }

    private boolean handleMenuItemClick(int menuItemId) {
        //Check to see which item was being clicked and perform appropriate action
        switch (menuItemId) {
            case R.id.nav_home:
                Log.v(TAG,"Nav: home");
                logMsg(LogMsg.ACTION_CLICKED_HOME);
                if(!prefs.hasPlace()){
                    showPickPlaceFragment();
                } else {
                    showAppropriateHomeFragment();
                }
                return true;
            case R.id.nav_recently_updated_issues:
                Log.v(TAG,"Nav: recently updated");
                logMsg(LogMsg.ACTION_CLICKED_RECENTLY_UPDATED_ISSUES);
                displayIssuesListFragment(IssuesDataSource.RECENTLY_UPDATED_ISSUES_LIST);
                return true;
            case R.id.nav_my_issues:
                Log.v(TAG,"Nav: my issues");
                logMsg(LogMsg.ACTION_CLICKED_MY_ISSUES);
                displayIssuesListFragment(IssuesDataSource.FOLLOWED_ISSUES_LIST);
                return true;
            case R.id.nav_map:
                Log.v(TAG,"Nav: map");
                logMsg(LogMsg.ACTION_CLICKED_ISSUES_MAP);
                displayMapFragment();
                return true;
            case R.id.nav_all_issues:
                Log.v(TAG,"Nav: all issues");
                logMsg(LogMsg.ACTION_CLICKED_ALL_ISSUES);
                displayIssuesListFragment(IssuesDataSource.ALL_ISSUES_LIST);
                return true;
            case R.id.nav_update_issues:
                Log.v(TAG,"Nav: update issues");
                logMsg(LogMsg.ACTION_CLICKED_UPDATE_ISSUES);
                displayUpdateIssuesFragment();
                return true;
            /*case R.id.nav_pick_place:
                Log.v(TAG,"Nav: pick place");
                logMsg(LogMsg.ACTION_CLICKED_PICK_PLACE);
                displayPickPlaceFragment();
                return true;*/
            case R.id.nav_about:
                Log.v(TAG,"Nav: about");
                logMsg(LogMsg.ACTION_CLICKED_ABOUT);
                displayAboutFragment();
                return true;
            case R.id.nav_stats:
                Log.v(TAG,"Nav: stats");
                logMsg(LogMsg.ACTION_CLICKED_STATS);
                displayStatsFragment();
                return true;
            /*case R.id.nav_save_debug_info:
                Log.v(TAG,"Nav: save debug info");
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
                return true;*/
            default:
                Log.e(TAG, "Got an unknown selection from nav drawer menu :-(");
                return true;
        }
    }

    /*
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
    */

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "onResume");
        if(!Development.DEBUG_MODE && !prefs.hasGivenConsent()){
            Intent intent = new Intent().setClass(this, ConsentActivity.class);
            startActivity(intent);
            return;
        }
        if(!prefs.hasPlace()){
            Log.w(TAG, "onResume: No place set yet");
            showPickPlaceFragment();
            return;
        }
        // now update the dynamic nav menu text
        /*long responsesToUpload = ResponsesDataSource.getInstance(this).countDataToSync() + ResponsesDataSource.getInstance(this).countDataNeedingLocation();
        long logsToUpload = LogsDataSource.getInstance(this).countDataToSync() + LogsDataSource.getInstance(this).countDataNeedingLocation();
        if((responsesToUpload + logsToUpload) > 0){
            MenuItem debugMenuItem = (MenuItem) navView.getMenu().findItem(R.id.nav_save_debug_info);
            String strToFormat = getResources().getString(R.string.nav_save_debug_info);
            String formattedStr = String.format(strToFormat, logsToUpload, responsesToUpload);
            debugMenuItem.setTitle(formattedStr);
        }*/
    }

    private void updateNavBarHeaderText(){
        TextView headerText = (TextView) findViewById(R.id.nav_bar_header_text);
        if(Config.getInstance().isAssignRequestTypeMode()) {
            //protect because on first run we don't have an assigned request type yet!
            RequestType assignedRequestType = prefs.getAssignedRequestType();
            if(getPlace()!=null && assignedRequestType!=null) {
                headerText.setText(getPlace().name + ": " + assignedRequestType.nickname);
            }
        } else {
            headerText.setText(getString(R.string.app_name) + ": " + getPlace().name);
        }
    }

    private void showAppropriateHomeFragment(){
        Log.v(TAG,"Showing showAppropriateHomeFragment");
        if((getPlace()!=null) && (IssuesDataSource.getInstance(this).countFollowedIssues(getPlace().id)>0)){
            displayIssuesListFragment(IssuesDataSource.FOLLOWED_ISSUES_LIST);
        } else {
            displayIssuesListFragment(IssuesDataSource.ALL_ISSUES_LIST);
        }
    }

    private void showPickPlaceFragment(){
        if (Config.getInstance().isPickPlaceMode()) {
            // On first load check to see if we have a place selected if so load My Actions Page
            displayPickPlaceFragment();
        } else if(Config.getInstance().isAssignRequestTypeMode()){
            displayAssignRequestTypeFragment();
        }
    }

    private void displayFragment(Fragment fragment) {
        displayFragment(fragment, true);
    }
    private void displayFragment(Fragment fragment, boolean addToBackStack){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_content, fragment);
        if(addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        };
        fragmentTransaction.commit();
    }

    private void displayUpdateIssuesFragment(){
        toolbar.setTitle(String.format(getResources().getString(R.string.update_issues_header), getPlace().name));
        logMsg(LogMsg.ACTION_LOADED_LATEST_ISSUES);
        UpdateIssuesFragment fragment = UpdateIssuesFragment.newInstance(getPlace().id);
        displayFragment(fragment, false);
    }

    private void displayPickPlaceFragment(){
        toolbar.setTitle(R.string.pick_place_header);
        PickPlaceListFragment fragment = PickPlaceListFragment.newInstance();
        displayFragment(fragment, false);
    }

    private void displayAssignRequestTypeFragment(){
        toolbar.setTitle(R.string.assign_request_type_header);
        AssignRequestTypeFragment fragment = AssignRequestTypeFragment.newInstance();
        displayFragment(fragment, false);
    }

    private void displayIssuesListFragment(int type){
        switch(type){
            case IssuesDataSource.ALL_ISSUES_LIST:
                toolbar.setTitle(String.format(getResources().getString(R.string.all_issues_header), getPlace().name));
                break;
            case IssuesDataSource.FOLLOWED_ISSUES_LIST:
                toolbar.setTitle(String.format(getResources().getString(R.string.followed_issues_header), getPlace().name));
                break;
            case IssuesDataSource.RECENTLY_UPDATED_ISSUES_LIST:
                toolbar.setTitle(String.format(getResources().getString(R.string.recently_updated_issues_header), getPlace().name));
                break;
        }
        IssuesListFragment fragment = IssuesListFragment.newInstance(type, prefs.getPlaceId(),
                prefs.getAssignedRequestTypeId());
        displayFragment(fragment);
    }

    private void displayMapFragment(){
        toolbar.setTitle(R.string.issues_map_title);
        try {
            Location loc = getLocation();
            if(loc==null){
                loc = Development.getFakeLocation();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.cant_get_location_detail)
                        .setTitle(R.string.cant_get_location_title)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                return;
                            }
                        });
                AlertDialog dialog = builder.create();
                // Add the buttons
                dialog.show();
            }
            IssuesMapFragment mapFragment = IssuesMapFragment.newInstance(0,prefs.getPlaceId(),
                        prefs.getAssignedRequestTypeId(),
                        loc.getLatitude(), loc.getLongitude());
            displayFragment(mapFragment);
        } catch (GoogleApiClientNotConnectionException e) {
            Log.e(TAG,"tried to display map fragment but couldn't connect to goole api client");
            e.printStackTrace();
        }
    }

    private void displayAboutFragment() {
        toolbar.setTitle(R.string.about_header);
        AboutFragment fragment = AboutFragment.newInstance();
        displayFragment(fragment);
    }

    private void displayStatsFragment() {
        toolbar.setTitle(R.string.stats_header);
        StatsFragment fragment = StatsFragment.newInstance();
        displayFragment(fragment);
    }

    public void onPause() {
        super.onPause();
        // make sure to cancel the issue update task if it is running
        if(updateIssuesTask!=null && updateIssuesTask.getStatus()!= AsyncTask.Status.FINISHED){
            updateIssuesTask.cancel(true);
        }
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    public void onIssueSelected(int issueId) {
        Log.d(TAG, "clicked item with id: " + issueId);
        drawerLayout.closeDrawers();    //Safety - close the drawers just in cas
        logMsg(issueId, LogMsg.ACTION_CLICKED_ON_ISSUE_IN_LIST);
        // Then you start a new Activity via Intent
        Intent intent = new Intent()
            .setClass(this, IssueDetailActivity.class)
            .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, issueId)
                .putExtra(IssueDetailActivity.PARAM_FROM_GEOFENCE_NOTIFICATION, false);
        startActivity(intent);
    }

    @Override
    public void onPlaceSelected(Place place) {
        Log.d(TAG, "clicked place id: " + place.id);
        // now save that we set the place
        prefs.savePlace(place);
        logMsg(LogMsg.NO_ISSUE, LogMsg.ACTION_PICKED_PLACE);
        // and jump to update the issues
        displayUpdateIssuesFragment();
    }

    @Override
    public ContextWrapper getContextWrapper() {
        return this;
    }

    @Override
    public void onIssuesUpdateSucceeded(int newIssueCount){
        String feedback = getResources().getQuantityString(R.plurals.updated_issues, newIssueCount, newIssueCount);
        Snackbar.make(findViewById(R.id.main_content), feedback, Snackbar.LENGTH_SHORT).show();
        displayIssuesListFragment(IssuesDataSource.ALL_ISSUES_LIST);
    }

    @Override
    public void onIssueUpdateFailed(){
        Snackbar.make(findViewById(R.id.main_content), R.string.issues_update_failed, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onFollowedIssueStatusChanged(int issueId, String oldStatus, String newStatus){
        Log.d(TAG,"status change alert on issue "+issueId+": "+oldStatus+" -> "+newStatus);
        Location loc = null;
        try {
            loc = getLocation();
        } catch(GoogleApiClientNotConnectionException e){
        }
        logMsg(issueId, LogMsg.ACTION_ISSUE_STATUS_UPDATED, oldStatus + ":" + newStatus, loc);
        // Fire a low priority notification
        sendNotification(ISSUE_CHANGE_NOTIFICATION_TAG, issueId, newStatus);
        // Mark issue as having something "new" on fav list (to drive a UI indication)
        IssuesDataSource.getInstance(this).updateIssueNewInfo(issueId, true);
    }

    /**
     * Posts a notification in the notification bar when a update is called.
     * If the user clicks the notification, control goes to the Main Activity.
     * param transitionType The type of transition that occurred.
     * For now, ActionPath only handles enter transitionTypes
     */
    private void sendNotification(String notificationTag, int issueId, String newStatus) {
        Log.d(TAG,"Sending notification for issueId: "+issueId);

        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String updateSummary = newStatus + ": " + issue.getSummary();

        PendingIntent pi = getPendingIntentToIssueDetail(this, issueId, true, false);

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
        notificationManager.notify(notificationTag, issueId, notification);
    }

    // TODO: move this to an intent helper class?
    public static PendingIntent getPendingIntentToIssueDetail(ContextWrapper contextWrapper, int issueId, boolean fromUpdate, boolean fromGeofence) {
        Issue issue = IssuesDataSource.getInstance(contextWrapper).getIssue(issueId);
        String summary = issue.getSummary();

        Log.v(TAG, "Returning update intent for IssueDetailActivity.class for issue: " + summary);

        Intent updateIntent = new Intent(contextWrapper, IssueDetailActivity.class)
                .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, issueId);
        updateIntent.putExtra(IssueDetailActivity.PARAM_FROM_UPDATE_NOTIFICATION, fromUpdate);
        updateIntent.putExtra(IssueDetailActivity.PARAM_FROM_GEOFENCE_NOTIFICATION, fromGeofence);
        updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(contextWrapper, 0, updateIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDisplayExternalURL(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));
        startActivity(browserIntent);
    }

    private void updateIssues(){
        Log.d(TAG, "request to update issues");
        if(updateIssuesTask==null || updateIssuesTask.isCancelled() || updateIssuesTask.getStatus()==AsyncTask.Status.FINISHED) {
            updateIssuesTask = new UpdateIssuesAsyncTask(this);
            updateIssuesTask.execute();
        }
    }

    @Override
    public void onRequestTypeAssigned(RequestType requestType){
        Place place = Config.getInstance().getPlace();  // do this here so place and request type are guaranteed to be set together
        prefs.savePlace(place);
        Log.d(TAG, "user was assigned " + requestType);
        updateNavBarHeaderText();
        logMsg(LogMsg.INVALID_ID, LogMsg.ACTION_PICKED_REQUEST_TYPE, requestType.name);
        Preferences.getInstance(this).saveAssignedRequestType(requestType);
        displayUpdateIssuesFragment();
    }

    protected void logMsg(String action){
        logMsg(LogMsg.NO_ISSUE, action);
    }

}


