package org.actionpath;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.actionpath.issues.Issue;
import org.actionpath.issues.IssueDatabase;
import org.actionpath.logging.LoggerService;
import org.actionpath.util.Installation;


public class ResponseActivity extends BaseActivity {

    public final String PARAM_ISSUE_ID = "issueID";

    public String TAG = this.getClass().getName();

    private Button resolvedButton;
    private Button unresolvedButton;
    private TextView issueAddressText;
    private TextView issueDescriptionText;
    private ImageView issueImage;

    int issueID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.short_response);

        Bundle bundle = getIntent().getExtras();

        // TODO: handle case where issueID is unknown or badly formed
        issueID = bundle.getInt(PARAM_ISSUE_ID);
        Log.i(TAG, "Responding to issue " + issueID);
        Issue issue = IssueDatabase.getById(issueID);
        Log.i(TAG, "Description: "+issue.getIssueDescription());
        String issue_description = issue.getIssueDescription();
        Log.i(TAG, "Address: "+issue.getIssueAddress());
        String issue_address = issue.getIssueAddress();

        issueAddressText = (TextView) findViewById(R.id.issue_address_text);
        issueAddressText.setText(issue_address);

        issueDescriptionText = (TextView) findViewById(R.id.issue_description_text);
        issueDescriptionText.setText(issue_description);

        if(issue.hasImageUrl()){
            // TODO: Catch ImageLoader already initialized so not re-initing every time
            issueImage = (ImageView) findViewById(R.id.issue_image);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(issue.getImageUrl(), issueImage);
        }

        final Context appContext = this.getApplicationContext();

        resolvedButton = (Button) findViewById(R.id.resolved_button);
        resolvedButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO: actually post the response to the server

                // CREATE AN ACTION LOG
                Intent logIntent = LoggerService.intentOf(ResponseActivity.this,issueID,LoggerService.ACTION_SURVEY_RESPONSE);
                startService(logIntent);
                Log.i(TAG, "Response to Issue " + issueID + ": Resolved");

                Intent intent = new Intent(ResponseActivity.this, AfterActionActivity.class);
                intent.putExtra(AfterActionActivity.EXTRA_ISSUE_ID, issueID);
                startActivity(intent);
            }
        });

        unresolvedButton = (Button) findViewById(R.id.unresolved_button);
        unresolvedButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO: actually post the response to the server

                // CREATE AN ACTION LOG
                Intent logIntent = LoggerService.intentOf(ResponseActivity.this,issueID,LoggerService.ACTION_SURVEY_RESPONSE);
                startService(logIntent);
                Log.i(TAG, "Response to Issue " + issueID + ": Unresolved");

                Intent intent = new Intent(ResponseActivity.this, AfterActionActivity.class);
                intent.putExtra(AfterActionActivity.EXTRA_ISSUE_ID, issueID);
                startActivity(intent);
            }
        });

    }

}