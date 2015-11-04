package org.actionpath.sync;

import android.content.ContextWrapper;
import android.util.Log;

import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.responses.Response;
import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.ui.MainActivity;
import org.actionpath.util.ActionPathServer;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimerTask;

/**
 * Download responses to issue you are following
 */
public class ResponseDownloadTimerTask extends TimerTask {

    private String TAG = this.getClass().getName();

    private String installId;
    private ContextWrapper contextWrapper;
    private ResponsesDataSource responsesDataSource;
    private IssuesDataSource issuesDataSource;
    private long lastCheck;

    public ResponseDownloadTimerTask(ContextWrapper contextWrapper, String installId) {
        this.contextWrapper = contextWrapper;
        this.installId = installId;
        responsesDataSource = ResponsesDataSource.getInstance(contextWrapper);
        issuesDataSource = IssuesDataSource.getInstance(contextWrapper);
        lastCheck = 0;    // a long time ago
    }

    private boolean isOkTimeToUpdate(){
        Calendar c = Calendar.getInstance();
        int hourIn24 = c.get(Calendar.HOUR_OF_DAY);
        Log.v(TAG,"current hour of day is "+hourIn24);
        return (hourIn24 > 8) && (hourIn24 < 23);
    }

    @Override
    public void run() {
        Date now = (new GregorianCalendar()).getTime();
        Log.d(TAG, "Timer says we should download any new responses now!");
        if(!isOkTimeToUpdate()){
            Log.d(TAG, "  skipping because it isn't a valid time of day");
        }
        long followedIssueCount = issuesDataSource.countFollowedIssues(0);
        if(followedIssueCount==0){
            Log.d(TAG, "  skipping because there are zero followed issues");
            return;
        }
        Log.d(TAG, "  " + followedIssueCount + " followed issues");
        Log.d(TAG, "  last checked at " + lastCheck);
        // now send off the data to the server
        List<Integer> issueIds = issuesDataSource.getFollowedIssueIds(0);
        Boolean worked = false;
        try {
            List<Response> otherResponses = ActionPathServer.responsesOnIssues(issueIds, installId, lastCheck);
            Log.d(TAG,"  got "+otherResponses.size()+" new responses");
            // TODO: decide where to save this...
            lastCheck = System.currentTimeMillis()/1000;;
            worked = true;
        } catch (URISyntaxException use){
            Log.e(TAG, "Couldn't sync to server: "+use.toString());
            worked = false;
        } catch (IOException ioe){
            Log.e(TAG, "Server said it failed to sync IO: "+ioe.toString());
            worked = false;
        } catch (JSONException jse){
            Log.e(TAG, "Server said it failed to sync JSON: "+jse.toString());
            worked = false;
        }
    }

}
