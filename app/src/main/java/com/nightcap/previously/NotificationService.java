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

    private final static String KEY_NOTIFICATION_PRIORITY = "notifications_priority";

    // NotificationManager Service
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
            builder.setVibrate(new long[] { 50, 50, 50, 50, 50, 50, 50, 50, 50 });   // Delay, on, off, on...
        }

        // Build the notification and issues it
        nm.notify(overdueNotificationId, builder.build());
    }

}