package org.actionpath.geofencing;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import org.actionpath.ui.ResponseActivity;
import org.actionpath.issues.Issue;
import org.actionpath.issues.IssuesDataSource;
import org.actionpath.logging.LogMsg;
import org.actionpath.R;
import org.actionpath.logging.LogsDataSource;

public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {

    public String TAG = this.getClass().getName();

    @Override
    protected void onEnteredGeofences(String[] strings) {
        sendNotification(strings[0]);

        // CREATE AN ACTION LOG
        LogsDataSource.getInstance(getApplicationContext()).insertLog(
                getApplicationContext(), Integer.valueOf(strings[0]), LogMsg.ACTION_ENTERED_GEOFENCE);

        //TODO: change so it takes in a list of strings
        //ex: String[] main where
        //main[0] is latitude
        //main[1] is longitude
        //main[2] is Name of main
        //main[3] is a short description of the main
        //main[4] is a picture url??

        //openAlert(strings);
    }



    @Override
    protected void onExitedGeofences(String[] strings) {
        //TODO: remove pop-up from screen
        //or do nothing
    }

    @Override
    protected void onError(int i) {
        Log.e(TAG, "Error: " + i);
    }


    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * param transitionType The type of transition that occurred.
     * For now, ActionPath only handles enter transitionTypes
     *
     */
    private void sendNotification(String issueID) {

        Log.d(TAG,"sending notification for issueId: "+issueID);

        int id = Integer.parseInt(issueID);
        Issue issue = IssuesDataSource.getInstance(this).getIssue(id);
        String summary = issue.getIssueSummary();
        //surveyKey="Chuckie Harris Park";

        // create "surveyIntent" to be triggered when user clicks on notification
        PendingIntent pi = getPendingIntent(issueID);

        // create the notification
        Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setContentTitle("Action: " + issueID)
                //notificationBuilder.setContentTitle("ActionPath " + transitionType + " " + TextUtils.join(GeofenceUtils.GEOFENCE_ID_DELIMITER,ids))
                // Notification title
                // not sure how to make this appear, or where it does appear
                //.setContentText("You have " + transitionType + " " + ids.length + "ActionPaths")
                // you can put subject line.
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Action Path")
                .setContentText(summary)
                .setContentIntent(pi);
                        // Set your notification icon here.

                        //TODO: ADD THESE BACK IN WHEN NEEDED
//                .addAction(R.drawable.ic_notification_overlay, "Take Action", pi);
    	/*.addAction(
    			R.drawable.ic_stat_snooze,
    			"Snooze", pi); // TODO: Make this an actual snooze button*/

        //notificationBuilder.setContentIntent(pi);

        Notification notification = notificationBuilder.build();



        // Now create the Big picture notification.
//        Notification notification = new Notification.BigTextStyle(notificationBuilder)
//                .bigText("Take the survey!").build();
        //	Notification notification = new Notification.BigPictureStyle(notificationBuilder).build();
    	/*.bigPicture(
    			BitmapFactory.decodeResource(getResources(),
    					R.drawable.ic_notification_placeholder)).build();*/
        // Put the auto cancel notification flag
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = getNotificationManager();
        notificationManager.notify(1, notification);


    }


    //creates a PendingIntent for bigPicture notifications
    //TODO: Build a bigPicture Notification with issue info and respond/ignore buttons
    public PendingIntent getPendingIntent(String issueID) {
        Log.v(TAG,"returning an intent for ResponseActivity.class");

        int id = Integer.parseInt(issueID);
        Issue issue = IssuesDataSource.getInstance(this).getIssue(id);
        String summary = issue.getIssueSummary();

        Intent surveyIntent = new Intent(this, ResponseActivity.class)
                .putExtra(ResponseActivity.PARAM_ISSUE_ID, id)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 0, surveyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

}
