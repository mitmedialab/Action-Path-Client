package org.actionpath.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
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

    private FloatingActionButton favoriteButton;

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

        favoriteButton = (FloatingActionButton) findViewById(R.id.issue_detail_favorite_button);
        setFavoritedButtonIcon(issue.isFavorited());
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Setting favorited on " + issue.getId() + " to " + !issue.isFavorited());
                issue.setFavorited(!issue.isFavorited());
                IssuesDataSource.getInstance().updateIssueFavorited(issue.getId(), issue.isFavorited());
                setFavoritedButtonIcon(issue.isFavorited());
                showFavoritedFeedback(view, issue.isFavorited());
            }
        });

    }

    private void showFavoritedFeedback(View view, boolean favorited){
        int feedbackStringId;
        if(favorited){
            feedbackStringId = R.string.favorited_issue_feedback;
        } else {
            feedbackStringId = R.string.unfavorited_issue_feedback;
        }
        Snackbar.make(view, feedbackStringId, Snackbar.LENGTH_SHORT).show();
    }

    private void setFavoritedButtonIcon(boolean favorited){
        if (favorited) {
            favoriteButton.setImageResource(R.drawable.ic_favorite_black_24dp);
        } else {
            favoriteButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
    }

}