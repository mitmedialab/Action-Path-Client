package org.actionpath;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import org.actionpath.logging.LogsDataSource;

public abstract class AbstractBaseActivity extends AppCompatActivity {

    protected void logMsg(String action){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),action);
    }

    protected void logMsg(int issueId, String action){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),issueId,action);
    }

}