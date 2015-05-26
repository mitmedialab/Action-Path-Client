package org.actionpath;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.actionpath.issues.Issue;
import org.actionpath.issues.IssueDatabase;
import org.actionpath.logging.LoggerService;


public class ResponseActivity extends Activity {

//    private RadioGroup radioGroup;
    private Button resBtn;
    private Button unresBtn;
    private TextView res_address;
    private TextView res_description;
    int id = 0;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.short_response);


        Bundle bundle = getIntent().getExtras();
        id = bundle.getInt("issueID");
        Log.e("and now we are here", "issue id from response: " + id);
        Issue issue = IssueDatabase.get(id);
        String issue_description = issue.getIssueDescription();
        String issue_address = issue.getIssueAddress();


        res_address = (TextView) findViewById(R.id.address);
        res_address.setText(issue_address);

        res_description = (TextView) findViewById(R.id.description);
        res_description.setText(issue_description);



        resBtn = (Button) findViewById(R.id.resolved_button);
        resBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(ResponseActivity.this,LoggerService.class);
                loggerServiceIntent.putExtra("logType", "action");
                loggerServiceIntent.putExtra("userID", String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra("issueID", String.valueOf(id));
                loggerServiceIntent.putExtra("action", "SurveyResponse");
                startService(loggerServiceIntent);
                Log.e("Action","SurveyResponse1 Response");

                Intent intent = new Intent(ResponseActivity.this, AfterActionActivity.class);
                intent.putExtra("issueID", id);
                startActivity(intent);
            }
        });

        unresBtn = (Button) findViewById(R.id.unresolved_button);
        unresBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // CREATE AN ACTION LOG
                Intent loggerServiceIntent = new Intent(ResponseActivity.this,LoggerService.class);
                loggerServiceIntent.putExtra("logType", "action");
                loggerServiceIntent.putExtra("userID", String.valueOf(MainActivity.getUserID()));
                loggerServiceIntent.putExtra("issueID", String.valueOf(id));
                loggerServiceIntent.putExtra("action", "SurveyResponse");
                startService(loggerServiceIntent);
                Log.e("Action","SurveyResponse2 Response");

                Intent intent = new Intent(ResponseActivity.this, AfterActionActivity.class);
                intent.putExtra("issueID", id);
                startActivity(intent);
            }
        });


//        radioGroup = (RadioGroup) findViewById(R.id.myRadioGroup);

//        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                // find which radio button is selected
//                if(checkedId == R.id.radio_resolved) {
//                    Toast.makeText(getApplicationContext(), "choice: Resolved",
//                            Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getApplicationContext(), "choice: Unresolved",
//                            Toast.LENGTH_SHORT).show();
//                }
//            }
//        });


    }



}