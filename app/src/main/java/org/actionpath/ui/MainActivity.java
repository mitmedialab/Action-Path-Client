package org.actionpath.ui;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.net.Uri;

import org.actionpath.R;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;
import org.actionpath.util.Development;

//TODO: create account page at start & send data
// include: city following (account page where this can be edited), user_id

public class MainActivity extends AbstractLocationActivity implements
        IssuesFragmentList.OnIssueSelectedListener, PickPlaceFragmentList.OnPlaceSelectedListener,
        UpdateIssuesFragment.OnIssuesUpdatedListener, AboutFragment.OnFragmentInteractionListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;

    private static String TAG = MainActivity.class.getName();

    public static final String PREFS_NAME = "ActionPathPrefs";
    public static final String PREF_PLACE_ID = "placeId";
    public static final String PREF_PLACE_NAME = "placeName";
    private static int INVALID_PLACE_ID = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        // setup the UI
        // @see http://www.android4devs.com/2015/06/navigation-view-material-design-support.html
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);

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
                        displayUpdateIssuesFragment();
                        return true;
                    case R.id.nav_pick_place:
                        displayPickPlaceFragment();
                        return true;
                    case R.id.nav_about:
                        displayAboutFragment();
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

    @Override
    public void onStart(){
        super.onStart();
        // check that we have a place selected
        if(getPlaceId()==INVALID_PLACE_ID){
            Log.w(TAG,"No place set yet");
            displayPickPlaceFragment();
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
        logMsg(issueId, LogMsg.FOLLOWED_ISSUE_CLICK);
        // Then you start a new Activity via Intent
        Intent intent = new Intent()
            .setClass(MainActivity.this, IssueDetailActivity.class)
            .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, issueId)
            .putExtra(IssueDetailActivity.PARAM_SHOW_QUESTION, false);
        startActivity(intent);
    }

    @Override
    public void onPlaceSelected(int placeId, String placeName) {
        Log.d(TAG, "clicked place id: " + placeId);
        // now save that we set the place
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(MainActivity.PREF_PLACE_ID, placeId);
        editor.putString(MainActivity.PREF_PLACE_NAME, placeName);
        editor.apply();
        Log.i(TAG, "Saved place " + placeId);
        // and jump to update the issues
        displayUpdateIssuesFragment();
    }

    @Override
    public void onIssuesUpdated(int newIssueCount){
        String feedback = getResources().getQuantityString(R.plurals.updated_issues,newIssueCount,newIssueCount);
        Snackbar.make(findViewById(R.id.main_content), feedback, Snackbar.LENGTH_SHORT).show();
        displayIssuesListFragment(IssuesFragmentList.ALL_ISSUES);
    }

    public int getPlaceId(){
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        return settings.getInt(MainActivity.PREF_PLACE_ID,INVALID_PLACE_ID);
    }

    public String getPlaceName(){
        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        return settings.getString(MainActivity.PREF_PLACE_NAME, "Unknown City");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

}

