package org.actionpath;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.actionpath.logging.LoggerService;
import org.actionpath.util.Installation;

public class AfterActionActivity extends BaseActivity{

    public static String EXTRA_ISSUE_ID = "issueID";

    public String TAG = this.getClass().getName();

    private Button unfollowBtn;
    private Button homeBtn;
    int issueID = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.after_action);

        Bundle bundle = getIntent().getExtras();
        issueID = bundle.getInt(EXTRA_ISSUE_ID);
        Log.d(TAG, "issue id from AfterAction: " + issueID);



        homeBtn = (Button) findViewById(R.id.home_btn);
        final Context appContext = this.getApplicationContext();
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent logIntent = LoggerService.intentOf(AfterActionActivity.this,issueID,LoggerService.ACTION_THANKS_DISMISSED);
                startService(logIntent);
                Log.i(TAG,"Thanks Dismissed on Issue #"+ issueID);

                Intent intent = new Intent(AfterActionActivity.this, MainActivity.class);
                intent.putExtra("followThisID", issueID);
                startActivity(intent);
            }
        });


        unfollowBtn = (Button) findViewById(R.id.unfollow_btn);
        unfollowBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent logIntent = LoggerService.intentOf(AfterActionActivity.this,issueID,LoggerService.ACTION_UNFOLLOWED_ISSUE);
                startService(logIntent);
                Log.i(TAG,"Unfollowed Issue #" + issueID);

                Intent intent = new Intent(AfterActionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }


}
