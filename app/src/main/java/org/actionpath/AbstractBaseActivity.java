package org.actionpath;

import android.app.Activity;

import org.actionpath.logging.LogsDataSource;

public abstract class AbstractBaseActivity extends Activity {

    protected void logMsg(String action){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),action);
    }

    protected void logMsg(int issueId, String action){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),issueId,action);
    }

}