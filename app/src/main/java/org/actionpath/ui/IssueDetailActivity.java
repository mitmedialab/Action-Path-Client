package org.actionpath.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.actionpath.R;
import org.actionpath.geofencing.GeofencingRemovalListener;
import org.actionpath.geofencing.GeofencingRemover;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.db.responses.ResponsesDataSource;

import java.util.ArrayList;
import java.util.List;


public class IssueDetailActivity extends AbstractLocationActivity implements
        OnMapReadyCallback, IssueQuestionFragment.OnAnswerSelectedListener,
        GeofencingRemovalListener {

    public static final String PARAM_ISSUE_ID = "issueID";
    public static final String PARAM_FROM_SURVEY_NOTIFICATION = "fromSurveyNotification";
    public static final String PARAM_FROM_UPDATE_NOTIFICATION = "fromUpdateNotification";

    public String TAG = this.getClass().getName();

    private Issue issue;
    private ImageLoader imageLoader;
    private boolean fromSurveyNotification;
    private boolean fromUpdateNotification;

    private AsyncTask answeringQuestionTask;

    private View.OnClickListener onFollowClickListener;

    private FloatingActionButton followFloatingButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        Bundle bundle = getIntent().getExtras();
        // TODO: handle case where issueID is unknown or badly formed
        int issueID = bundle.getInt(PARAM_ISSUE_ID);
        fromSurveyNotification = bundle.getBoolean(PARAM_FROM_SURVEY_NOTIFICATION);
        fromUpdateNotification = bundle.getBoolean(PARAM_FROM_UPDATE_NOTIFICATION);
        if(fromSurveyNotification){
            logMsg(issueID,LogMsg.ACTION_CLICKED_ON_SURVEY_NOTIFICATION);
        } else if (fromUpdateNotification) {
            logMsg(issueID,LogMsg.ACTION_CLICKED_ON_UPDATE_NOTIFICATION);
        }
        Log.i(TAG, "Showing details for issue " + issueID);
        issue = IssuesDataSource.getInstance(this).getIssue(issueID);
        Log.v(TAG,"  at ("+issue.getLatitude()+","+issue.getLongitude()+")");

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

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(issue.getIssueSummary());

        TextView summary = (TextView) findViewById(R.id.issue_detail_summary);
        summary.setText(issue.getIssueSummary());
        TextView description = (TextView) findViewById(R.id.issue_detail_description);
        description.setText(issue.getIssueDescription());
        TextView status = (TextView) findViewById(R.id.issue_detail_status);
        status.setText(issue.getStatus());
        TextView location = (TextView) findViewById(R.id.issue_detail_location);
        location.setText(issue.getIssueAddress());

        Button walkThereButton = (Button) findViewById(R.id.issue_detail_walk_there_button);
        walkThereButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:mode=w&q=" + issue.getIssueAddress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        Button viewOnlineButton = (Button) findViewById(R.id.issue_detail_view_online);
        viewOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri intentUri = Uri.parse(issue.getUrl());
                Intent urlIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                startActivity(urlIntent);
            }
        });

        showQuestionUiFragment();

        onFollowClickListener = new View.OnClickListener() {
            @Override public void onClick(View view) { changeFollowedAndUpdateUI(view); }
        };

        followFloatingButton = (FloatingActionButton) findViewById(R.id.issue_detail_favorite_button);
        followFloatingButton.setOnClickListener(onFollowClickListener);

        // create and add the map
        LatLng issueLatLng = new LatLng(issue.getLatitude(), issue.getLongitude());
        GoogleMapOptions options = new GoogleMapOptions();
        options.mapType(GoogleMap.MAP_TYPE_NORMAL)
                .camera(CameraPosition.fromLatLngZoom(issueLatLng, 14))
                .liteMode(true);
        MapFragment mapFragment = MapFragment.newInstance(options);
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.issue_detail_map_wrapper, mapFragment);
        fragmentTransaction.commit();
        mapFragment.getMapAsync(this);

        if(issue.hasImageUrl()){
            Log.d(TAG,"issue has an image: "+issue.getImageUrl());
            if(imageLoader==null || !imageLoader.isInited()){
                imageLoader = ImageLoader.getInstance();
            }
            ImageView issueImage = (ImageView) findViewById(R.id.issue_detail_backdrop);
            imageLoader.displayImage(issue.getImageUrl(), issueImage);
        }

    }

    @Override
    public void onStart(){
        super.onStart();
        updateFollowedButtons(issue.isFollowed());
    }

    private void changeFollowedAndUpdateUI(View view) {
        changeFollowedAndUpdateUI(view, !issue.isFollowed(), true);
    }

    private void changeFollowedAndUpdateUI(View view,boolean follow,boolean showSnackbar){
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

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_issue_detail, menu);
        toolbarMenu = menu;
        followToolbarMenuItem = (MenuItem) toolbarMenu.findItem(R.id.issue_detail_action_follow);
        return true;
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
     */

    private void answerQuestion(View view, String answer){
        Log.i(TAG, "Answered '" + answer + "' on issue " + issue.getId());
        // update the UI
        final String newAnswer = answer;
        changeFollowedAndUpdateUI(view, true, false);
        final View v = view;
        final String answerText = answer;
        final Context context = getApplicationContext();
        final Location loc = updateLastLocation();  // gotta call that instead of getLocation to avoid exception
        // save the answer
        answeringQuestionTask = new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                ResponsesDataSource dataSource = ResponsesDataSource.getInstance(context);
                dataSource.insert(context, issue.getId(), answerText, loc);
                logMsg(issue.getId(), LogMsg.ACTION_RESPONDED_TO_QUESTION);
                return true;
            }
            @Override
            protected void onPostExecute(Object o) {
                boolean success = (boolean) o;
                Log.d(TAG,"saved answer to server "+success);
                if(success) {
                    // show some snackbar feedback
                    int feedbackStringId = R.string.issue_question_answered;
                    Snackbar.make(v, feedbackStringId, Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_unfollow, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    changeFollowedAndUpdateUI(v, false, true);
                                }
                            })
                            .show();
                } else {
                    // something in the server comms failed
                    Snackbar.make(v, R.string.failed_to_save_answer, Snackbar.LENGTH_LONG).show();
                }
            }
        };
        answeringQuestionTask.execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        LatLng issueLatLng = new LatLng(issue.getLatitude(), issue.getLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(issueLatLng)
                .title(issue.getIssueSummary()));

    }

    private void showQuestionUiFragment(){
        Fragment fragment = IssueQuestionFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.issue_detail_question_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onAnswerSelected(int answerIndex) {
        logMsg(issue.getId(),LogMsg.ACTION_FOLLOWED_ISSUE_BY_ANSWERING);
        String answerText = "";
        // TODO: Use constants on this switch
        switch(answerIndex) {
            case 0:
                answerText = "no";
                break;
            case 1:
                answerText = "yes";
                break;
        }
        answerQuestion(findViewById(R.id.issue_detail_question_container), answerText);
        if(fromSurveyNotification) {
            // only remove the geofence if we got an alert and then answered a question
            removeGeofence();
        }
    }

    protected void removeGeofence(){
        List<String> issuesToRemove = new ArrayList<String>();
        issuesToRemove.add(issue.getId() + "");
        GeofencingRemover remover = new GeofencingRemover(getApplicationContext(),
            issuesToRemove,this);
        remover.sendRequest();
    }

    @Override
    public void onGeofenceRemovalSuccess(List<String> requestIdsRemoved){
        for(String issueId: requestIdsRemoved){
            Log.d(TAG,"Removing geofence for issue "+issueId);
            IssuesDataSource.getInstance(getApplicationContext()).updateIssueGeofenceCreated(
                    Integer.parseInt(issueId),false);
        }
    }

    @Override
    public void onGeofenceRemovalFailure(Status status) {
        Log.w(TAG, "Failed to remove geofence for issue " + issue.getId() + " - " + status.getStatus());
    }

    @Override
    public void onStop(){
        super.onStop();
        if(answeringQuestionTask!=null){
            answeringQuestionTask.cancel(true);
        }
    }

}
