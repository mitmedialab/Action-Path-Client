package org.actionpath.geofencing;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import org.actionpath.ui.IssueDetailActivity;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;
import org.actionpath.R;
import org.actionpath.logging.LogsDataSource;

public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {

    public String TAG = this.getClass().getName();

    @Override
    protected void onEnteredGeofences(String[] issueIds) {
        for(String str:issueIds){
            int issueId = Integer.parseInt(str);
            sendNotification(issueId);
            LogsDataSource.getInstance(getApplicationContext()).insertLog(
                    getApplicationContext(), issueId, LogMsg.ACTION_ENTERED_GEOFENCE, null);
        }
    }

    @Override
    protected void onExitedGeofences(String[] strings) {
        //TODO: remove pop-up from screen
    }

    @Override
    protected void onError(int i) {
        Log.e(TAG, "GeofencingReceiver Error: " + i);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * param transitionType The type of transition that occurred.
     * For now, ActionPath only handles enter transitionTypes
     */
    private void sendNotification(int issueId) {
        Log.d(TAG,"Sending notification for issueId: "+issueId);

        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String summary = issue.getIssueSummary();

        PendingIntent pi = getPendingIntent(issueId);

        // create the notification
        Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getResources().getString(R.string.nearby_notification))
                .setContentText(summary)
                .setContentIntent(pi);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.notify(1, notification);
    }

    private PendingIntent getPendingIntent(int issueId) {
        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String summary = issue.getIssueSummary();

        Log.v(TAG, "Returning survey intent for IssueDetailActivity.class for issue: " + summary);

        Intent surveyIntent = new Intent(this, IssueDetailActivity.class)
                .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, issueId)
                .putExtra(IssueDetailActivity.PARAM_FROM_SURVEY_NOTIFICATION, true)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 0, surveyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

}
