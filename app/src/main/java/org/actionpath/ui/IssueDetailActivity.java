package org.actionpath.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.actionpath.R;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;


public class IssueDetailActivity extends AbstractLocationActivity implements
        OnMapReadyCallback, IssueQuestionFragment.OnAnswerSelectedListener {

    public static final String PARAM_ISSUE_ID = "issueID";
    public static final String PARAM_SHOW_QUESTION = "showQuestion";

    public String TAG = this.getClass().getName();

    private Issue issue;
    private ImageLoader imageLoader;

    private View.OnClickListener onFollowClickListener;

    private FloatingActionButton followFloatingButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        Bundle bundle = getIntent().getExtras();
        // TODO: handle case where issueID is unknown or badly formed
        int issueID = bundle.getInt(PARAM_ISSUE_ID);
        boolean showQuestion = bundle.getBoolean(PARAM_SHOW_QUESTION);
        Log.i(TAG, "Details to issue " + issueID);
        issue = IssuesDataSource.getInstance(this).getIssue(issueID);

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

        if(showQuestion){
            showQuestionUiFragment();
        }

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
        changeFollowedAndUpdateUI(view, !issue.isFollowed());
    }

    private void changeFollowedAndUpdateUI(View view,boolean follow){
        Log.i(TAG, "Setting followed on " + issue.getId() + " to " + !issue.isFollowed());
        // update the issue first
        issue.setFollowed(follow);
        IssuesDataSource.getInstance().updateIssueFollowed(issue.getId(), issue.isFollowed());
        // update the icons
        updateFollowedButtons(issue.isFollowed());
        // show the snackbar feedback
        int feedbackStringId;
        if(issue.isFollowed()){
            feedbackStringId = R.string.followed_issue_feedback;
        } else {
            feedbackStringId = R.string.unfollowed_issue_feedback;
        }
        Snackbar.make(view, feedbackStringId, Snackbar.LENGTH_SHORT)
                .setAction(R.string.action_undo, onFollowClickListener)
                .show();
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
        Log.i(TAG, "Answered " + answer + " issue " + issue.getId());
        // update the UI
        final String newAnswer = answer;
        changeFollowedAndUpdateUI(view, true);
        // show some snackbar feedback
        int feedbackStringId = R.string.issue_question_answered;
        Snackbar.make(view, feedbackStringId, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_unfollow, new View.OnClickListener() {
                    @Override public void onClick(View v) {changeFollowedAndUpdateUI(v,false);}
                })
                .show();
        // save the answer to the server
        new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] params) {
                logMsg(issue.getId(), LogMsg.ACTION_SURVEY_RESPONSE);
                return ActionPathServer.saveAnswer(getInstallId(), issue.getId(), newAnswer);
            }
            @Override
            protected void onPostExecute(Object o) {
                boolean success = (boolean) o;
                Log.d(TAG,"saved answer to server "+success);
            }
        }.execute();
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
        String answerText = "";
        switch(answerIndex) {
            case 0:
                answerText = "no";
                break;
            case 1:
                answerText = "yes";
                break;
        }
        answerQuestion(findViewById(R.id.issue_detail_question_container),answerText);
    }

}
