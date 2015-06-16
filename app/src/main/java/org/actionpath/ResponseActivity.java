package org.actionpath;

import android.app.Activity;
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


public class ResponseActivity extends Activity {

    public final String PARAM_ISSUE_ID = "issueID";

    public String TAG = this.getClass().getName();

    private Button resolvedButton;
    private Button unresolvedButton;
    private TextView issueAddressText;
    private TextView issueDescriptionText;
    private ImageView issueImage;

    int id = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.short_response);

        Bundle bundle = getIntent().getExtras();
        // TODO: handle case where issueID is unknown or badly formed
        id = Integer.parseInt(bundle.getString(PARAM_ISSUE_ID));
        Log.i(TAG, "Responding to Issue " + id);
        Issue issue = IssueDatabase.get(id);
        String issue_description = issue.getIssueDescription();
        String issue_address = issue.getIssueAddress();


        issueAddressText = (TextView) findViewById(R.id.issue_address_text);
        issueAddressText.setText(issue_address);

        issueDescriptionText = (TextView) findViewById(R.id.issue_description_text);
        issueDescriptionText.setText(issue_description);

        if(issue.hasImageUrl()){
            issueImage = (ImageView) findViewById(R.id.issue_image);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(issue.getImageUrl(), issueImage);
        }

        resolvedButton = (Button) findViewById(R.id.resolved_button);
        resolvedButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO: actually post the response to the server

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(ResponseActivity.this, LoggerService.class);
                loggerServiceIntent.putExtra(LoggerService.PARAM_LOG_TYPE, LoggerService.LOG_TYPE_ACTION);
                loggerServiceIntent.putExtra(LoggerService.PARAM_USER_ID, String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ISSUE_ID, String.valueOf(id));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ACTION, LoggerService.ACTION_SURVEY_RESPONSE);
                startService(loggerServiceIntent);
                Log.i(TAG, "Response to Issue " + id + ": Resolved");

                Intent intent = new Intent(ResponseActivity.this, AfterActionActivity.class);
                intent.putExtra(AfterActionActivity.EXTRA_ISSUE_ID, id);
                startActivity(intent);
            }
        });

        unresolvedButton = (Button) findViewById(R.id.unresolved_button);
        unresolvedButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO: actually post the response to the server

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(ResponseActivity.this, LoggerService.class);
                loggerServiceIntent.putExtra(LoggerService.PARAM_LOG_TYPE, LoggerService.LOG_TYPE_ACTION);
                loggerServiceIntent.putExtra(LoggerService.PARAM_USER_ID, String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ISSUE_ID, String.valueOf(id));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ACTION, LoggerService.ACTION_SURVEY_RESPONSE);
                startService(loggerServiceIntent);
                Log.i(TAG, "Response to Issue " + id + ": Unresolved");

                Intent intent = new Intent(ResponseActivity.this, AfterActionActivity.class);
                intent.putExtra(AfterActionActivity.EXTRA_ISSUE_ID, id);
                startActivity(intent);
            }
        });

    }

}