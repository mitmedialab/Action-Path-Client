package org.actionpath.geofencing;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.actionpath.db.properties.PropertiesDataSource;
import org.actionpath.ui.IssueDetailActivity;
import org.actionpath.db.issues.Issue;
import org.actionpath.db.issues.IssuesDataSource;
import org.actionpath.db.logs.LogMsg;
import org.actionpath.R;
import org.actionpath.db.logs.LogsDataSource;
import org.actionpath.ui.MainActivity;

public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {

    public String TAG = this.getClass().getName();

    private String GEOFENCE_NOTIFICATION_TAG = "geofence";

    @Override
    protected void onEnteredGeofences(String[] issueIds) {
        for(String str:issueIds){
            int issueId = Integer.parseInt(str);
            sendNotification(issueId);
            // track that we hit a geofence, in various ways
            LogsDataSource.getInstance(getApplicationContext()).insert(
                    getApplicationContext(), issueId, LogMsg.ACTION_ENTERED_GEOFENCE, null);
            PropertiesDataSource.getInstance().incrementGeofenceNotificationFiredCount();
        }
    }

    @Override
    protected void onExitedGeofences(String[] issueIds) {
        for (String str:issueIds){
            int issueId = Integer.parseInt(str);
            Log.d(TAG,"left geofence for "+issueId+" - canceling notification");
            getNotificationManager().cancel(GEOFENCE_NOTIFICATION_TAG, issueId);
        }
    }

    @Override
    protected void onError(int i) {
        Log.e(TAG, "GeofencingReceiver Error: " + i);
    }

    private void sendNotification(int issueId) {
        Log.d(TAG,"Sending notification for issueId: "+issueId);

        Issue issue = IssuesDataSource.getInstance(this).getIssue(issueId);
        String summary = issue.getSummary();

        PendingIntent pi = MainActivity.getPendingIntentToIssueDetail(this, issueId, false, true);

        // create the notification
        Builder notificationBuilder = new Notification.Builder(this);

        // Create a version of the ic_launcher that scaled to the notification size
        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.ic_launcher);

        int height = (int) this.getResources().getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) this.getResources().getDimension(android.R.dimen.notification_large_icon_width);
        Bitmap largeIconScaled = Bitmap.createScaledBitmap(largeIcon, width, height, false);

        notificationBuilder
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(largeIconScaled)
                .setContentTitle(getResources().getString(R.string.nearby_notification))
                .setContentText(summary)
                .setContentIntent(pi);

        Notification notification = notificationBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.notify(GEOFENCE_NOTIFICATION_TAG, issueId, notification);
    }


    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

}
