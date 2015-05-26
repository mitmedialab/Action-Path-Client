package org.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.actionpath.issues.Issue;
import org.actionpath.issues.IssueDatabase;
import org.actionpath.logging.LoggerService;


public class NotificationActivity extends Activity {

    int id = 0;
    private TextView description;

    public String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Bundle bundle = getIntent().getExtras();

        if(bundle.getString("issueID")!= null)
        {
            id = Integer.parseInt(bundle.getString("issueID"));
            Log.i(TAG, "issue " + id);
        }
        Issue issue = IssueDatabase.get(id);
        String issue_description = issue.getIssueDescription();


        description = (TextView) findViewById(R.id.description);
        description.setText(issue_description);


        Button respondBtn = (Button) findViewById(R.id.respond_issue);
        Button ignoreBtn = (Button) findViewById(R.id.ignore_issue);
        ignoreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(NotificationActivity.this,LoggerService.class);
                loggerServiceIntent.putExtra(LoggerService.PARAM_LOG_TYPE, LoggerService.LOG_TYPE_ACTION);
                loggerServiceIntent.putExtra(LoggerService.PARAM_USER_ID, String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ISSUE_ID, String.valueOf(id));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ACTION, LoggerService.ACTION_NOTIFICATION_IGNORE_CLICK);
                startService(loggerServiceIntent);
                Log.i(TAG,"Notification Ignore Button Clicked");
                finish();
                System.exit(0);
            }
        });

        respondBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(NotificationActivity.this,LoggerService.class);
                loggerServiceIntent.putExtra(LoggerService.PARAM_LOG_TYPE, LoggerService.LOG_TYPE_ACTION);
                loggerServiceIntent.putExtra(LoggerService.PARAM_USER_ID, String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ISSUE_ID, String.valueOf(id));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ACTION, LoggerService.ACTION_NOTIFICATION_RESPOND_CLICK);
                startService(loggerServiceIntent);
                Log.i(TAG, "Notification Response Button Clicked");

                Intent intent = new Intent(NotificationActivity.this, ResponseActivity.class);
                intent.putExtra("issueID", id);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
        return true;
    }



    public void toResponse(View view) {
        Intent intent = new Intent(view.getContext(), ResponseActivity.class);
        startActivityForResult(intent, 0);
    }
}
