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
    protected void onEnteredGeofences(String[] strings) {
        int issueId = Integer.parseInt(strings[0]);
        sendNotification(issueId);
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(), Integer.valueOf(strings[0]), LogMsg.ACTION_ENTERED_GEOFENCE);
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
        Log.d(TAG,"sending notification for issueId: "+issueId);

        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String summary = issue.getIssueSummary();

        PendingIntent pi = getPendingIntent(issueId);

        // create the notification
        Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Action Nearby")
                .setContentText(summary)
                .setContentIntent(pi);
/*
                //TODO: ADD THESE BACK IN WHEN NEEDED
                .addAction(R.drawable.ic_notification_overlay, "Take Action", pi);
                .addAction(R.drawable.ic_stat_snooze, "Snooze", pi); // TODO: Make this an actual snooze button
*/
        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.notify(1, notification);
    }

    public PendingIntent getPendingIntent(int issueId) {
        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String summary = issue.getIssueSummary();

        Log.v(TAG,"Returning intent for ResponseActivity.class for issue: " + summary);

        Intent surveyIntent = new Intent(this, IssueDetailActivity.class)
                .putExtra(IssueDetailActivity.PARAM_ISSUE_ID, issueId)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 0, surveyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

}
