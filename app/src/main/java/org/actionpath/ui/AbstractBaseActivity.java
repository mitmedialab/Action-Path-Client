package org.actionpath.ui;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.actionpath.R;
import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogSyncService;
import org.actionpath.logging.LogsDataSource;
import org.actionpath.places.Place;
import org.actionpath.util.Development;
import org.actionpath.util.Installation;

import java.util.ArrayList;

public abstract class AbstractBaseActivity extends AppCompatActivity {

    /**
     * Do any config and setup that applies no matter how we enter the app here
     */
    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        // make sure the LogSyncService is running
        if(!LogSyncService.isRunning()) {
            Intent i = new Intent(this, LogSyncService.class);
            this.startService(i);
        }
        // generate a new installId user if we need to
        if (!Installation.hasId()) {
            AsyncTask<Object, Void, Object> task = new AsyncTask<Object, Void, Object>() {
                @Override
                protected Object doInBackground(Object[] params) {
                    logMsg(LogMsg.ACTION_INSTALLED_APP);
                    boolean success = ActionPathServer.createUser(getInstallId());
                    return success;
                }
                @Override
                protected void onPostExecute(Object o) {
                    boolean success = (boolean) o;
                    if(success){
                        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content),
                            R.string.created_new_user,Snackbar.LENGTH_SHORT);
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

    protected void logMsg(String action){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),action);
    }

    protected void logMsg(int issueId, String action){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),issueId,action);
    }

    protected String getInstallId(){
        return Installation.id(this.getApplicationContext());
    }


}