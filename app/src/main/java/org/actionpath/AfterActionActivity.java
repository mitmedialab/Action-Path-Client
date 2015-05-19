package org.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.actionpath.logging.LoggerService;

public class AfterActionActivity extends Activity{

    private Button unfollowBtn;
    private Button homeBtn;
    int id = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_action);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getInt("issueID");
        Log.e("AFTER", "issue id from AfterAction: " + id);



        homeBtn = (Button) findViewById(R.id.home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(AfterActionActivity.this,LoggerService.class);
                loggerServiceIntent.putExtra("logType", "action");
                loggerServiceIntent.putExtra("userID", String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra("issueID", String.valueOf(id));
                loggerServiceIntent.putExtra("action", "ThanksDismissed");
                startService(loggerServiceIntent);
                Log.e("Action","ThanksDismissed AfterAction");

                Intent intent = new Intent(AfterActionActivity.this, MainActivity.class);
                intent.putExtra("followThisID", id);
                startActivity(intent);
            }
        });


        unfollowBtn = (Button) findViewById(R.id.unfollow_btn);
        unfollowBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(AfterActionActivity.this,LoggerService.class);
                loggerServiceIntent.putExtra("logType", "action");
                loggerServiceIntent.putExtra("userID", String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra("issueID", String.valueOf(id));
                loggerServiceIntent.putExtra("action", "UnfollowedIssue");
                startService(loggerServiceIntent);
                Log.e("Action","UnfollowedIssue AfterAction");
                Intent intent = new Intent(AfterActionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


}
