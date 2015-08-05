package org.actionpath.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.actionpath.R;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;


public class IssueDetailActivity extends AbstractBaseActivity {

    public static final String PARAM_ISSUE_ID = "issueID";

    public String TAG = this.getClass().getName();

    private Issue issue;
    private ImageView issueImage;
    private ImageLoader imageLoader;

    private View.OnClickListener onFollowClickListener;

    private FloatingActionButton followFloatingButton;
    private Menu toolbarMenu;
    private MenuItem followToolbarMenuItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_detail);

        Bundle bundle = getIntent().getExtras();
        // TODO: handle case where issueID is unknown or badly formed
        int issueID = bundle.getInt(PARAM_ISSUE_ID);
        Log.i(TAG, "Details to issue " + issueID);
        issue = IssuesDataSource.getInstance(this).getIssue(issueID);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                Uri gmmIntentUri = Uri.parse("google.navigation:m=w&q=" + issue.getIssueAddress());
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

        if(issue.hasImageUrl()){
            Log.d(TAG,"issue has an image: "+issue.getImageUrl());
            if(imageLoader==null || !imageLoader.isInited()){
                imageLoader = ImageLoader.getInstance();
            }
            issueImage = (ImageView) findViewById(R.id.issue_detail_backdrop);
            imageLoader.displayImage(issue.getImageUrl(), issueImage);
        }

        onFollowClickListener = new View.OnClickListener() {
            @Override public void onClick(View view) { changeFollowedAndUpdateUI(view); }
        };

        followFloatingButton = (FloatingActionButton) findViewById(R.id.issue_detail_favorite_button);
        followFloatingButton.setOnClickListener(onFollowClickListener);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateFollowedButtons(issue.isFollowed());
    }

    private void changeFollowedAndUpdateUI(View view){
        Log.i(TAG, "Setting followed on " + issue.getId() + " to " + !issue.isFollowed());
        // update the issue first
        issue.setFollowed(!issue.isFollowed());
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
        int stringId;
        if (issue.isFollowed()) {
            iconId = R.drawable.ic_favorite_black_24dp;
            stringId = R.string.action_unfollow;
        } else {
            iconId = R.drawable.ic_favorite_border_black_24dp;
            stringId = R.string.action_follow;
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

}
