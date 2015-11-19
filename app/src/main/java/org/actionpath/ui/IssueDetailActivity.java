package org.actionpath.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.actionpath.R;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;


public class IssueDetailActivity extends AbstractLocationBaseActivity implements
        OnMapReadyCallback, AppBarLayout.OnOffsetChangedListener {

    public static final String PARAM_ISSUE_ID = "issueID";
    public static final String PARAM_FROM_GEOFENCE_NOTIFICATION = "fromGeofenceNotification";
    public static final String PARAM_FROM_UPDATE_NOTIFICATION = "fromUpdateNotification";

    private static final int LONG_SNACKBAR_DURATION = 5500;

    public String TAG = this.getClass().getName();

    private ImageLoader imageLoader;
    private boolean fromGeofenceNotification;
    private boolean fromUpdateNotification;

    private View.OnClickListener onFollowClickListener;
    private Menu toolbarMenu;
    private CollapsingToolbarLayout collapsingToolbar;
    private int issueId;
    private Issue issue;
    private TextView summary;
    private TextView description;
    private TextView status;
    private ImageView issueImage;
    private TextView location;
    private Button walkThereButton;
    private Button viewOnlineButton;

    private FloatingActionButton followFloatingButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            Log.e(TAG, "Action Bar not available not created yet.");
        }
        // weird that I have to do this manually...
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(this);
        summary = (TextView) findViewById(R.id.issue_detail_summary);
        description = (TextView) findViewById(R.id.issue_detail_description);
        status = (TextView) findViewById(R.id.issue_detail_status);
        location = (TextView) findViewById(R.id.issue_detail_location);
        walkThereButton = (Button) findViewById(R.id.issue_detail_walk_there_button);
        viewOnlineButton = (Button) findViewById(R.id.issue_detail_view_online);
        followFloatingButton = (FloatingActionButton) findViewById(R.id.issue_detail_favorite_button);
        followFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { changeFollowedAndUpdateUI(view); }
        });
        Button takeActionButton = (Button) findViewById(R.id.issue_detail_take_action_button);
        takeActionButton.setOnClickListener(new View.OnClickListener(){
            @Override public void onClick(View view) { showTakeActionFragment(); }
        });
        if(imageLoader==null || !imageLoader.isInited()){
            imageLoader = ImageLoader.getInstance();
        }
        issueImage = (ImageView) findViewById(R.id.issue_detail_backdrop);
        onFollowClickListener = new View.OnClickListener() {
            @Override public void onClick(View view) { changeFollowedAndUpdateUI(view); }
        };
        // parse the intent args
        Bundle bundle = getIntent().getExtras();
        // TODO: handle case where issueID is unknown or badly formed
        issueId = bundle.getInt(PARAM_ISSUE_ID);
        fromGeofenceNotification = bundle.getBoolean(PARAM_FROM_GEOFENCE_NOTIFICATION);
        fromUpdateNotification = bundle.getBoolean(PARAM_FROM_UPDATE_NOTIFICATION);
        if(fromGeofenceNotification){
            logMsg(issueId, LogMsg.ACTION_CLICKED_ON_SURVEY_NOTIFICATION);
        } else if (fromUpdateNotification) {
            logMsg(issueId,LogMsg.ACTION_CLICKED_ON_UPDATE_NOTIFICATION);
        }
    }

    /**
     * http://developer.android.com/reference/android/app/Activity.html#onNewIntent(android.content.Intent)
     * @param intent
     */
    public void onNewIntent(Intent intent){
        Log.d(TAG,"issue detail onNewIntent: " + intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG, "Showing details for issue " + issueId);
        issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        Log.v(TAG, "  at (" + issue.getLatitude() + "," + issue.getLongitude() + ")");
        Log.v(TAG, "  geofenced = " + issue.isGeofenceCreated() + " (radius=" + issue.getRadius() + ")");
        // populate the UI
        updateFollowedButtons(issue.isFollowed());
        collapsingToolbar.setTitle(issue.getSummary());
        summary.setText(issue.getSummary());
        if (!issue.getDescription().equals("")) {
            description.setText(issue.getDescription());
        } else {
            // hide description if there is none like in a custom question
            findViewById(R.id.issue_detail_description_header).setVisibility(View.GONE);
            description.setVisibility(View.GONE);
        }
        status.setText(issue.getStatus());
        location.setText(issue.getAddress());
        walkThereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:mode=w&q=" + issue.getAddress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        viewOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri intentUri = Uri.parse(issue.getUrl());
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                startActivity(urlIntent);
            }
        });
        // show the view online button if doesn't have a custom question - not the best
        // proxy but it'll work for now
        if(issue.hasCustomQuestion()){
            viewOnlineButton.setVisibility(View.GONE);
        } else {
            viewOnlineButton.setVisibility(View.VISIBLE);
        }
        // create and add the map
        LatLng issueLatLng = new LatLng(issue.getLatitude(), issue.getLongitude());
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .camera(CameraPosition.fromLatLngZoom(issueLatLng, 14));
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.issue_detail_map_wrapper, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);
        // show the issue's image, failing that the latest image from someone else's responses
        String headerImageUrl = null;
        if(issue.hasImageUrl()){
            Log.d(TAG, "issue has an image: " + issue.getImageUrl());
            headerImageUrl = issue.getImageUrl();
        } else {
            String otherReponseImageUrl = issue.lastestOtherResponseImage();
            if(otherReponseImageUrl!=null){
                headerImageUrl = otherReponseImageUrl;
            }
        }
        if(headerImageUrl!=null){
            imageLoader.displayImage(headerImageUrl, issueImage);
            issueImage.setVisibility(View.VISIBLE);
        } else {
            issueImage.setVisibility(View.GONE);
        }
        // update the count of your responses
        Log.v(TAG,"Issue has "+issue.getResponseCount()+" responses");
        Log.v(TAG, "Updating response count feedack to " + issue.getResponseCount());
        String responseCountFeedback = getResources().getQuantityString(R.plurals.response_count,
                issue.getResponseCount(),issue.getResponseCount(),issue.getResponseCount());
        TextView responseCountTextView = (TextView) findViewById(R.id.issue_detail_response_count);
        responseCountTextView.setText(responseCountFeedback);
        // update the list of other responses
        if(issue.hasOtherReponses()){
            Log.v(TAG,"adding other responses fragment");
            ((View)findViewById(R.id.issue_detail_other_responses_container)).setVisibility(View.VISIBLE);
            Fragment issueResponsesFragment = IssueResponsesFragment.newInstance(this.issue.getId());
            FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
            fragmentTransaction2.replace(R.id.issue_detail_other_responses_container, issueResponsesFragment);
            fragmentTransaction2.commit();
        } else {
            Log.v(TAG,"skipping other responses fragment");
            ((View)findViewById(R.id.issue_detail_other_responses_container)).setVisibility(View.GONE);
        }
    }

    /**
     * change behaviour when toolbar is collapsed
     * @param appBarLayout
     * @param offset
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        if (offset == 0) {// Collapsed

        } else { // Not collapsed

        }
    }

    private void changeFollowedAndUpdateUI(View view) {
        changeFollowedAndUpdateUI(view, !issue.isFollowed(), true);
    }

    private void changeFollowedAndUpdateUI(View view, boolean follow, boolean showSnackbar) {
        Log.i(TAG, "Setting followed on " + issue.getId() + " to " + !issue.isFollowed());
        // update the issue first
        issue.setFollowed(follow);
        IssuesDataSource.getInstance().updateIssueFollowed(issue.getId(), issue.isFollowed());
        // update the icons
        updateFollowedButtons(issue.isFollowed());
        // show the snackbar feedback
        int feedbackStringId;
        if(issue.isFollowed()){
            logMsg(issue.getId(),LogMsg.ACTION_UNFOLLOWED_ISSUE_FROM_FOLLOW_BUTTON);
            feedbackStringId = R.string.followed_issue_feedback;
        } else {
            logMsg(issue.getId(),LogMsg.ACTION_FOLLOWED_ISSUE_FROM_FOLLOW_BUTTON);
            feedbackStringId = R.string.unfollowed_issue_feedback;
        }
        if(showSnackbar) {
            Snackbar.make(view, feedbackStringId, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_undo, onFollowClickListener)
                    .show();
        }
    }

    private void updateFollowedButtons(boolean isFollowed){
        // update the floating action bar and toolbar icon
        int iconId;
        //int stringId;
        if (isFollowed) {
            iconId = R.drawable.ic_favorite_black_24dp;
            //stringId = R.string.action_unfollow;
        } else {
            iconId = R.drawable.ic_favorite_border_black_24dp;
            //stringId = R.string.action_follow;
        }
        followFloatingButton.setImageResource(iconId);
        //followToolbarMenuItem.setIcon(iconId);
        //followToolbarMenuItem.setTitle(stringId);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*
        getMenuInflater().inflate(R.menu.menu_issue_detail, menu);
        toolbarMenu = menu;
        return true;
        */
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.issue_detail_action_follow) {
            changeFollowedAndUpdateUI(findViewById(R.id.issue_detail_scroll_view));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        LatLng issueLatLng = new LatLng(issue.getLatitude(), issue.getLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(issueLatLng)
                .title(issue.getSummary()));

    }

    private void showTakeActionFragment(){
        Intent intent = new Intent()
                .setClass(this, IssueTakeActionActivity.class)
                .putExtra(IssueTakeActionActivity.PARAM_ISSUE_ID, issue.getId())
                .putExtra(IssueTakeActionActivity.PARAM_FROM_GEOFENCE_NOTIFICATION, fromGeofenceNotification);
        startActivity(intent);
    }

}
