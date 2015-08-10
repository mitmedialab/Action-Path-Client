package org.actionpath.ui;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.actionpath.R;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogSyncService;
import org.actionpath.logging.LogsDataSource;
import org.actionpath.util.Installation;


public abstract class AbstractBaseActivity extends AppCompatActivity {

    /**
     * Do any config and setup that applies no matter how we enter the app here
     */
    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        // make sure database is instantiated
        IssuesDataSource.getInstance(this);
        // make sure the LogSyncService is running
        if(!LogSyncService.isRunning()) {
            Intent i = new Intent(this, LogSyncService.class);
            this.startService(i);
        }
        // generate a new installId user if we need to
        if (!Installation.hasId()) {
            getInstallId(); // create the id
            new AsyncTask<Object, Void, Object>() {
                @Override
                protected Object doInBackground(Object[] params) {
                    logMsg(LogMsg.NO_ISSUE,LogMsg.ACTION_INSTALLED_APP,null);
                    return ActionPathServer.createInstall(getInstallId());
                }
                @Override
                protected void onPostExecute(Object o) {
                    boolean success = (boolean) o;
                    if (success) {
                        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                                R.string.created_new_user, Snackbar.LENGTH_SHORT);
                    }
                }
            }.execute();
        }
        // create an image loader instance
        if(!ImageLoader.getInstance().isInited()){
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
            ImageLoader.getInstance().init(config);
        }
    }

    protected String getInstallId(){
        return Installation.id(this.getApplicationContext());
    }

    protected void logMsg(int issueId, String action, Location loc){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),issueId,action, loc);
    }


}