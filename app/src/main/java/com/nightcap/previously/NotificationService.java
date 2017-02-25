package com.nightcap.previously;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

/**
 * Service for scheduling notifications.
 */

public class NotificationService extends IntentService {
    private String TAG = "NotificationService";

    // Preferences
    private SharedPreferences prefs;
    final String KEY_VIBRATE = "notifications_vibrate";
    final String KEY_RINGTONE = "notifications_ringtone";

    // Set an ID for the notification
    static final int overdueNotificationId = 001;

    // Keys (from old app, repurpose or delete these later...)
    public static final String ACTION_UPDATE_DATA = "com.nightcap.oleo.beta.UPDATE_DATA";

    public static final String EXTRA_UPDATE_CITY = "com.nightcap.oleo.beta.UPDATE_CITY";
    public static final String EXTRA_UPDATE_FUEL = "com.nightcap.oleo.beta.UPDATE_FUEL";
    public static final String EXTRA_MOST_RECENT_DATA_TIME = "com.nightcap.oleo.beta.MOST_RECENT_DATA_TIME";
    public static final String EXTRA_UPDATE_RESULT = "com.nightcap.oleo.beta.UPDATE_RESULT";
    public static final String EXTRA_ALARM_TRIGGERED = "com.nightcap.oleo.beta.ALARM_TRIGGERED";

    private final static String KEY_NOTIFICATION_PRIORITY = "notifications_priority";

    // Codes for broadcasting
    public static final int CODE_UPDATE_SUCCESSFUL = 42;
    public static final int CODE_UPDATE_UNSUCCESSFUL = -100;
    public static final int CODE_NO_NEW_DATA = -11;
    public static final int CODE_NO_DATA_CONNECTION = 404;

    // Get an instance of the NotificationManager service
    NotificationManager nm;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "NotificationService intent triggered by alarm");
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Check database for overdue tasks
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        List<Event> overdueList = databaseHandler.getOverdueEvents();

        // Set a notification if there are overdue events
        if (overdueList.size() > 0) {
            Log.d(TAG, "There are overdue events; setting notification");
            setNotification(overdueList);
        } else {
            Log.d(TAG, "No more overdue events; removing notification");
            nm.cancel(overdueNotificationId);
        }

//        boolean isAlarmed = intent.getBooleanExtra(EXTRA_ALARM_TRIGGERED, true);
//        if (isAlarmed) {
//
//        }

//        // See if local data is obsolete
//        dataHandler = new DatabaseHandler(getApplicationContext());
//        String city = intent.getStringExtra(EXTRA_UPDATE_CITY);
//        String fuel = intent.getStringExtra(EXTRA_UPDATE_FUEL);
//
//        boolean isDataObsolete = true; // dataHandler.checkPhoneData(city, fuel);
//
//        if (isDataObsolete) {
//            Log.d(TAG,"Phone data may be out of date");
//
//            // Check connectivity
//            ConnectivityManager cm =
//                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getActiveNetworkInfo();
//
//            if (netInfo != null && netInfo.isConnected()) {
//                Log.d(TAG, "Data connection available");
//                try {
////                    new UpdateAllDataTask().execute();
////                    getNewData(city, fuel);
//                    Log.d(TAG, "Getting new data");
//                } catch(Exception e) {
//                    publishResults(CODE_UPDATE_UNSUCCESSFUL);
//                    e.printStackTrace();
//                }
//            } else {
//                publishResults(CODE_NO_DATA_CONNECTION);
//                // Reschedule for later
//            }
//        } else {
//            Log.d(TAG, "Not updating because local data is current");
//            publishResults(NotificationService.CODE_NO_NEW_DATA);
//        }
    }

    private void publishResults(int result) {
//        // Acknowledge data transfer to InfoFragment
//        Intent intent = new Intent(ACTION_UPDATE_DATA);
//        intent.putExtra(EXTRA_UPDATE_RESULT, result);
//        sendBroadcast(intent);
    }

    public void setNotification(List<Event> overdue) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // Prevent double stacking

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Start building the notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_action_done_light)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setContentTitle("Time to do things")
                        .setContentIntent(resultPendingIntent)
                        .setPriority(Notification.PRIORITY_LOW);

        // Add overdue events text, and handle plurals
        String overdueText;
        if (overdue.size() == 1) {
            overdueText = "You have " + overdue.size() + " overdue event.";
        } else {
            overdueText = "You have " + overdue.size() + " overdue events.";
        }
        builder.setContentText(overdueText);

        // Account for notification vibration preference
        if (prefs.getBoolean(KEY_VIBRATE, true)) {  // Vibration preference
//            builder.setVibrate(new long[] { 50, 100, 200, 50, 100, 50 });   // Delay, on, off, on...
            builder.setVibrate(new long[] { 50, 50, 50, 50, 50, 50, 50, 50, 50 });   // Delay, on, off, on...
        }

        // Build the notification and issues it
        nm.notify(overdueNotificationId, builder.build());
    }

}