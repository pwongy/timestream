package com.nightcap.previously;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Service for scheduling notifications.
 */

public class NotificationService extends IntentService {
    private String TAG = "NotificationService";
    DateHandler dateHandler = new DateHandler();

    // Preferences
    SharedPreferences prefs;
    final String KEY_VIBRATE = "notifications_vibrate";
    final String KEY_RINGTONE = "notifications_ringtone";

    // Set an ID for the notification
    static final int overdueNotificationId = 001;

    // NotificationManager Service
    NotificationManager nm;

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "NotificationService intent triggered");
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Check database for overdue tasks
        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        List<Event> overdueList = databaseHandler.getOverdueEvents();

        // Set a notification if there are overdue events
        if (overdueList.size() > 0) {
            Log.d(TAG, "There are overdue events; setting notification");

            // Sort events
            Event.SortParameter order = Event.SortParameter.NEXT_DUE_ASCENDING;
            Comparator<Event> cp = Event.getComparator(order);
            Collections.sort(overdueList, cp);

            setNotification(overdueList);
        } else {
            Log.d(TAG, "No more overdue events; removing notification");
            nm.cancel(overdueNotificationId);
        }

        // Close realm when finished
        databaseHandler.closeRealm();
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
                new NotificationCompat.Builder(this, "overdue_events_channel")
                        .setSmallIcon(R.drawable.ic_stat_previously)
                        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setContentIntent(resultPendingIntent)
                        .setPriority(Notification.PRIORITY_LOW);

        // Add overdue events text, and handle plurals
        String overdueText;
        if (overdue.size() == 1) {
            overdueText = overdue.size() + " overdue item";
        } else {
            overdueText = overdue.size() + " overdue items";
        }

        builder.setSubText(overdueText);
        builder.setContentTitle("You have " + overdueText);
        builder.setContentText("Just pick one and get it done");

        // Big notification
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        StringBuilder overdueBigTextBuilder = new StringBuilder();
        for (int i = 0; i < overdue.size(); i++) {
            overdueBigTextBuilder.append(dateHandler.getRelativeDaysString(dateHandler.getDaysBetween(
                    overdue.get(i).getNextDue(), dateHandler.getTodayDate())));
            overdueBigTextBuilder.append("    " + overdue.get(i).getName());
            if (i < overdue.size() - 1) {
                overdueBigTextBuilder.append("\n");
            }
        }

        bigText.bigText(overdueBigTextBuilder.toString());
        bigText.setBigContentTitle("Here's what's left on your list:");
        builder.setStyle(bigText);

        // Account for preferences:
        // Ringtone preference
        String ringtoneStr = prefs.getString(KEY_RINGTONE, "content://settings/system/notification_sound");
        builder.setSound(Uri.parse(ringtoneStr));

        // Vibration preference
        if (prefs.getBoolean(KEY_VIBRATE, true)) {  // Vibration preference
            builder.setVibrate(new long[] { 50, 100, 50, 50, 50, 50, 50, 50 });   // Delay, on, off, on...
        }

        // Build the notification and issues it
        nm.notify(overdueNotificationId, builder.build());
    }

}