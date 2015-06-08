package org.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.actionpath.logging.LoggerService;

public class AfterActionActivity extends Activity{

    public static String EXTRA_ISSUE_ID = "issueID";

    public String TAG = this.getClass().getName();

    private Button unfollowBtn;
    private Button homeBtn;
    int id = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_action);

        Bundle bundle = getIntent().getExtras();
        id = bundle.getInt(EXTRA_ISSUE_ID);
        Log.e(TAG, "issue id from AfterAction: " + id);



        homeBtn = (Button) findViewById(R.id.home_btn);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(AfterActionActivity.this,LoggerService.class);
                loggerServiceIntent.putExtra(LoggerService.PARAM_LOG_TYPE,LoggerService.PARAM_ACTION);
                loggerServiceIntent.putExtra(LoggerService.PARAM_USER_ID, String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ISSUE_ID, String.valueOf(id));
                loggerServiceIntent.putExtra(LoggerService.PARAM_ACTION, LoggerService.ACTION_THANKS_DISMISSED);
                startService(loggerServiceIntent);
                Log.i(TAG,"Thanks Dismissed");

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
                Log.i(TAG,"Unfollowed Issue");
                Intent intent = new Intent(AfterActionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


}
