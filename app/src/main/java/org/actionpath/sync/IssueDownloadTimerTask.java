package org.actionpath.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

import org.actionpath.R;
import org.actionpath.db.RequestType;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.db.responses.Response;
import org.actionpath.db.responses.ResponsesDataSource;
import org.actionpath.tasks.UpdateIssuesAsyncTask;
import org.actionpath.ui.MainActivity;
import org.actionpath.util.ActionPathServer;
import org.actionpath.util.Config;
import org.actionpath.util.GoogleApiClientNotConnectionException;
import org.actionpath.util.Installation;
import org.actionpath.util.Preferences;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimerTask;

/**
 * Download the latest issues near me periodically
 */
public class IssueDownloadTimerTask extends AbstractLocationTimerTask implements UpdateIssuesAsyncTask.OnIssuesUpdatedListener {

    private String TAG = this.getClass().getName();

    private ContextWrapper contextWrapper;

    public IssueDownloadTimerTask(GoogleApiClient googleApiClient, ContextWrapper contextWrapper) {
        super(googleApiClient);
        this.contextWrapper = contextWrapper;
    }

    @Override
    public void run() {
        Log.d(TAG, "Timer says we should update issues from the server!");
        if(!Preferences.getInstance(this.getContextWrapper()).hasGivenConsent()){
            return;
        }
        // bail if no set up yet
        if(Config.getInstance(this.getContextWrapper()).isPickPlaceMode()) {
            if (!Preferences.getInstance(this.getContextWrapper()).hasPlace()) {
                return;
            }
        } else {
            if(Preferences.getInstance(this.getContextWrapper()).getAssignedRequestType()==null){
                return;
            }
        }
        // now grab the data from the server
        UpdateIssuesAsyncTask updateIssuesTask = new UpdateIssuesAsyncTask(this);
        updateIssuesTask.execute();
    }

    @Override
    public ContextWrapper getContextWrapper(){
        return this.contextWrapper;
    }

    @Override
    public void onIssuesUpdateSucceeded(int newIssueCount){
        // log that it worked
        Log.i(TAG,"Background task update issues");
        LogsDataSource.getInstance(this.getContextWrapper()).insert(
                this.getContextWrapper(), Issue.INVALID_ID, LogMsg.ACTION_BACKGROUND_TASK_UPDATED_ISSUES,
                newIssueCount+"",getLocation());
    }

    @Override
    public void onIssueUpdateFailed(){
        Log.i(TAG,"Background task failed to update issues");
    }

    @Override
    public void onFollowedIssueStatusChanged(int issueId, String oldStatus, String newStatus){
        // log that it happened
        Log.i(TAG,"followed issue status changed");
        Log.d(TAG, "status change alert on issue " + issueId + ": " + oldStatus + " -> " + newStatus);
        LogsDataSource.getInstance(this.getContextWrapper()).insert(
                this.getContextWrapper(), issueId, LogMsg.ACTION_ISSUE_STATUS_UPDATED,
                oldStatus + ":" + newStatus, getLocation());
        IssuesDataSource.getInstance(this.getContextWrapper()).updateIssueNewInfo(issueId, true);
        // fire a notification
        Issue issue = IssuesDataSource.getInstance(this.getContextWrapper()).getIssue(issueId);
        String updateSummary = newStatus + ": " + issue.getSummary();
        PendingIntent pi = MainActivity.getPendingIntentToIssueDetail(this.getContextWrapper(), issueId, true, false);
        Notification.Builder notificationBuilder = new Notification.Builder(this.getContextWrapper());
        notificationBuilder
                .setPriority(Notification.PRIORITY_LOW)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(contextWrapper.getResources().getString(R.string.update_notification))
                .setContentText(updateSummary)
                .setContentIntent(pi);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager =
                (NotificationManager) contextWrapper.getSystemService(contextWrapper.NOTIFICATION_SERVICE);
        notificationManager.notify(MainActivity.ISSUE_CHANGE_NOTIFICATION_TAG, issueId, notification);
    }

}
