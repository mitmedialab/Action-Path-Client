package com.example.kimberlyleon1.actionpath;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class GeofencingReceiver extends ReceiveGeofenceTransitionIntentService {

    @Override
    protected void onEnteredGeofences(String[] strings) {
        Log.d(GeofencingReceiver.class.getName(), "onEnter");

        sendNotification(strings[0]);




        //change so it takes in a list of strings
        //ex: String[] alert where
        //alert[0] is latitude
        //alert[1] is longitude
        //alert[2] is Name of alert
        //alert[3] is a short description of the alert
        //alert[4] is a picture url??
//        openAlert(strings);
    }

    private void openAlert(String[] alert) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GeofencingReceiver.this);
        //alert[2]
        alertDialogBuilder.setTitle("Alert");
        //alert[3]
        alertDialogBuilder.setMessage("This will depend on the specific event");
        // set positive button: Respond message
        alertDialogBuilder.setPositiveButton("Respond",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // go to a new activity of the app
                Intent positveActivity = new Intent(GeofencingReceiver.this,
                        Response.class);
                startActivity(positveActivity);
            }
        });
        // set negative button: Ignore message
        alertDialogBuilder.setNegativeButton("Ignore",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
                Toast.makeText(GeofencingReceiver.this, "You chose a to ignore the alert",
                        Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
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


//    private void sendNotifications(String transitionType, String[] ids){
//
//        //retrieve the relevant survey keys and print them
//        Context ctx = this.getApplicationContext();
//        SurveyGeofenceStore mPrefs = new SurveyGeofenceStore(ctx);
//        ArrayList<String> surveyKeys = mPrefs.getUniqueSurveyKeys(ids);
//        ArrayList<String> y = mPrefs.getGeofenceStoreKeys();
//        Log.i("NotificationContext", ctx.toString());
//        for (String surveyKey : surveyKeys) {
//            sendNotification(surveyKey);
//        }
//    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * param transitionType The type of transition that occurred.
     * For now, ActionPath only handles enter transitionTypes
     *
     */
    private void sendNotification(String surveyKey) {

        Log.d("sendNotification","sending notification build thing in ReceiveTransitionsIntentService");
        Log.i("sendNotification", surveyKey);

        //surveyKey="Chuckie Harris Park";

        // create "surveyIntent" to be triggered when user clicks on notification
        PendingIntent pi = getPendingIntent(surveyKey);

        // create the notification
        Builder notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setContentTitle("Action: " + surveyKey)
                //notificationBuilder.setContentTitle("ActionPath " + transitionType + " " + TextUtils.join(GeofenceUtils.GEOFENCE_ID_DELIMITER,ids))
                // Notification title
                // not sure how to make this appear, or where it does appear
                //.setContentText("You have " + transitionType + " " + ids.length + "ActionPaths")
                // you can put subject line.
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("My notification")
                .setContentText("it works!");
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

        // TODO: Create a way to clear the notification once it has been clicked

    }


    //creates a PendingIntent for bigPicture notifications
    public PendingIntent getPendingIntent(String surveyKey) {
        Log.v("INTENT","returning an intent for SurveyActivity.class");

        Intent surveyIntent = new Intent(this, Response.class)
                .putExtra("surveyKey", surveyKey)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(this, 0, surveyIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

}
