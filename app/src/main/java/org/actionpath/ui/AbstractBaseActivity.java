package org.actionpath.ui;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.actionpath.R;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;
import org.actionpath.logging.LogSyncService;
import org.actionpath.logging.LogsDataSource;
import org.actionpath.util.ActionPathServer;
import org.actionpath.util.Development;
import org.actionpath.util.Installation;


public abstract class AbstractBaseActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();

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
        // create the issue database
        addTestIssues();
    }

    protected String getInstallId(){
        return Installation.id(this.getApplicationContext());
    }

    protected void logMsg(int issueId, String action, Location loc){
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(),issueId,action, loc);
    }

    private void addTestIssues(){
        final float mediaLabLat = 42.360396f;
        final float mediaLabLng= -71.087233f;
        final float massAndVassarLat = 42.360167f;
        final float massAndVassarLng = -71.094868f;
        Issue testIssue1 = new Issue(1234, "Acknowledged", "Toy Train Hack", "Giant Toy Train hack on Kendall Square T entrance.",
                mediaLabLat, mediaLabLng, "350 Main Street, Cambridge, Massachusetts", "", null, null,
                Development.PLACE_ID_CAMBRIDGE);
        testIssue1.setTest(true);
        Issue testIssue2 = new Issue(2345, "Acknowledged", "Pothole", "Pothole on the corner of Mass Ave and Vassar.",
                massAndVassarLat, massAndVassarLng, "Massachusetts Ave./Vassar St., Cambridge, Massachusetts", "", null, null,
                Development.PLACE_ID_CAMBRIDGE);
        testIssue2.setTest(true);
        Log.d(TAG, "added test issues");
        IssuesDataSource dataSource = IssuesDataSource.getInstance(this);
        dataSource.insertOrUpdateIssue(testIssue1);
        dataSource.updateIssueFollowed(1234, true);
        dataSource.insertOrUpdateIssue(testIssue2);
        dataSource.updateIssueFollowed(2345, true);

    }

}