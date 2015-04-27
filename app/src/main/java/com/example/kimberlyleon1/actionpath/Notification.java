package com.example.kimberlyleon1.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class Notification extends Activity {

    int id = 0;
    private TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Bundle bundle = getIntent().getExtras();


        if(bundle.getString("issueID")!= null)
        {
            id = Integer.parseInt(bundle.getString("issueID"));
            Log.e("yusss we are here", "issue id from notification: "+ id);
        }
        Issue issue = AlertTest.getIssue(id);
        String issue_description = issue.getIssueDescription();


        description = (TextView) findViewById(R.id.description);
        description.setText(issue_description);


        Button respondBtn = (Button) findViewById(R.id.respond_issue);
        Button ignoreBtn = (Button) findViewById(R.id.ignore_issue);
        ignoreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(Notification.this,LoggerService.class);
                loggerServiceIntent.putExtra("userID", String.valueOf(AlertTest.getUserID()));
                loggerServiceIntent.putExtra("issueID", String.valueOf(id));
                loggerServiceIntent.putExtra("action", "NotificationIgnoreClick");
                startService(loggerServiceIntent);

                finish();
                System.exit(0);
            }
        });

        respondBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(Notification.this,LoggerService.class);
                loggerServiceIntent.putExtra("userID", String.valueOf(AlertTest.getUserID()));
                loggerServiceIntent.putExtra("issueID", String.valueOf(id));
                loggerServiceIntent.putExtra("action", "NotificationRespondClick");
                startService(loggerServiceIntent);


                Intent intent = new Intent(Notification.this, Response.class);
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
        Intent intent = new Intent(view.getContext(), Response.class);
        startActivityForResult(intent, 0);
    }
}
