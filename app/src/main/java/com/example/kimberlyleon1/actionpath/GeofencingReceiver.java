package com.example.kimberlyleon1.actionpath;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;


public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {

    @Override
    protected void onEnteredGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onEnter"+ strings[0]);

        sendNotification(strings[0]);

        // CREATE AN ACTION LOG
        Intent loggerServiceIntent = new Intent(GeofencingReceiver.this,LoggerService.class);
        loggerServiceIntent.putExtra("userID", String.valueOf(AlertTest.getUserID()));
        loggerServiceIntent.putExtra("issueID", String.valueOf(strings[0]));
        loggerServiceIntent.putExtra("action", "EnteredGeofence");
        startService(loggerServiceIntent);



        //change so it takes in a list of strings
        //ex: String[] alert where
        //alert[0] is latitude
        //alert[1] is longitude
        //alert[2] is Name of alert
        //alert[3] is a short description of the alert
        //alert[4] is a picture url??
//        openAlert(strings);
    }



    @Override
    protected void onExitedGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onExit");

        //remove pop-up from screen
        //or do nothing
    }

    @Override
    protected void onError(int i) {
        Log.e(GeofencingReceiver.class.getName(), "Error: " + i);
        }


    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * param transitionType The type of transition that occurred.
     * For now, ActionPath only handles enter transitionTypes
     *
     */
    private void sendNotification(String issueID) {

        Log.d("sendNotification","sending notification build thing in ReceiveTransitionsIntentService");
        Log.i("sendNotification", issueID);
        Log.e("yo", "issue id: "+ issueID);

        int id = Integer.parseInt(issueID);
        Issue issue = AlertTest.getIssue(id);
        Log.e("issue", "this issue: "+issue);
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

        notificationBuilder.setContentIntent(pi);

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
    public PendingIntent getPendingIntent(String issueID) {
        Log.v("INTENT","returning an intent for SurveyActivity.class");

        int id = Integer.parseInt(issueID);
        Issue issue = AlertTest.getIssue(id);
        String summary = issue.getIssueSummary();

        Intent surveyIntent = new Intent(this, com.example.kimberlyleon1.actionpath.Notification.class)
                .putExtra("issueID", issueID)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 0, surveyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

}
