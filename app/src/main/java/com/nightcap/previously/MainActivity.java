package com.nightcap.previously;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Main Activity. Displays existing database events.
 */

public class MainActivity extends AppCompatActivity implements ReceiveDateInterface, ReceiveEventInterface {
    private String TAG = "MainActivity";

    // A handler to access the Realm (database).
    // Any interactions with the Realm should go through this.
    private DatabaseHandler databaseHandler;

    // Variables to access user preferences, defined in the app settings.
    private SharedPreferences prefs;
    final String KEY_SORT_FIELD = "sort_primary_field";
    final String KEY_SORT_ORDER_ASCENDING = "sort_primary_ascending";
    final String KEY_NOTIFICATIONS = "notifications_toggle";

    // RecyclerView for displaying the log, and associated adapter.
    RecyclerView recyclerView;
    private List<Event> eventList = new ArrayList<>();
    private EventLogAdapter eventLogAdapter;

    // The currently selected event.
    Event selectedEvent;

    // Alarm type codes for scheduling the (background) notification service.
    final int ALARM_DEFAULT = 0;
    final int ALARM_TEST_DELAYED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Track crashes and usage stats using Fabric
        Fabric.with(this, new Crashlytics());
        Fabric.with(this, new Answers());

        // Get user settings
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Inflate xml layout
        setContentView(R.layout.activity_main);

        // Get the Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Set up the FAB
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start new Activity to add an event
                Intent addEvent = new Intent(getApplicationContext(), EditActivity.class);
                startActivity(addEvent);
            }
        });

        // Get a database handler
        // (Realm is initialised during its construction so no need to do that here.)
        databaseHandler = new DatabaseHandler(this);

        // Set up the RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        // LayoutManager must be set before adapter
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());                // Animator
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);    // Decorator line
        recyclerView.addItemDecoration(itemDecoration);

        // Adapter (must be set after LayoutManager)
        eventLogAdapter = new EventLogAdapter(this, eventList);
        recyclerView.setAdapter(eventLogAdapter);

        // Hide FAB on scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 10 && fab.isShown()) {
                    fab.hide();
                } else if (dy < -10 && !fab.isShown()) {
                    fab.show();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    fab.show();
                }

                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        // Notification (currently testing)
        scheduleNotification(ALARM_DEFAULT);

        // Insight tracking via Answers
        Answers.getInstance().logCustom(new CustomEvent("[TESTING] Opened app"));
        Log.i(TAG, "Logged app opening to Answers");

    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareData();
    }

    /**
     * Get data items from the Realm and apply to the adapter.
     */
    private void prepareData() {
        // Get data from Realm
        eventList = databaseHandler.getLatestDistinctEvents(prefs.getString(KEY_SORT_FIELD, "name"),
                prefs.getBoolean(KEY_SORT_ORDER_ASCENDING, true));

        // Send list to adapter
        eventLogAdapter.updateData(eventList);
    }

    /*
     * Here we create and handle actions from the overflow menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_sort:
                showSortDialog();
                break;
            case R.id.action_crash:
                throw new RuntimeException("This is a crash");
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingsActivity.class);

                // Extras to skip headers screen
                settings.putExtra(AppCompatPreferenceActivity.EXTRA_SHOW_FRAGMENT,
                        SettingsActivity.GeneralPreferenceFragment.class.getName());
                settings.putExtra(AppCompatPreferenceActivity.EXTRA_NO_HEADERS, true);

                startActivity(settings);
                break;
            case R.id.action_about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Dialog for setting event sort order.
     */
    public void showSortDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sort, null);
        dialogBuilder.setView(dialogView);

//        final Spinner spinner2 = (Spinner) dialogView.findViewById(R.id.spinner_sort_secondary);

        // Set initial spinner selection
        final Spinner spinner1 = (Spinner) dialogView.findViewById(R.id.spinner_sort_primary);
        String[] sortFields = getResources().getStringArray(R.array.pref_sort_field_values);
        int sortFieldIndex = -1;
        for (int i = 0; i < sortFields.length; i++) {
            if (sortFields[i].equalsIgnoreCase(prefs.getString(KEY_SORT_FIELD, "name"))) {
                sortFieldIndex = i;
                break;
            }
        }

        if (sortFieldIndex >= 0) {
            spinner1.setSelection(sortFieldIndex);
        } else {
            spinner1.setSelection(0);   // Default to event name
        }

        // Set initial sort order image from preferences
        final ImageButton ib1 = (ImageButton) dialogView.findViewById(R.id.image_button_1);
        if (prefs.getBoolean(KEY_SORT_ORDER_ASCENDING, true)) {
            ib1.setImageDrawable(getDrawable(R.drawable.ic_action_sort_ascending));
        } else {
            ib1.setImageDrawable(getDrawable(R.drawable.ic_action_sort_descending));
        }

        // Switch order on click
        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prefs.getBoolean(KEY_SORT_ORDER_ASCENDING, true)) {
                    // Currently set as ascending, so switch to descending
                    setBooleanPreference(KEY_SORT_ORDER_ASCENDING, false);
                    ib1.setImageDrawable(getDrawable(R.drawable.ic_action_sort_descending));
                } else {
                    // Currently set as descending, so switch to ascending
                    setBooleanPreference(KEY_SORT_ORDER_ASCENDING, true);
                    ib1.setImageDrawable(getDrawable(R.drawable.ic_action_sort_ascending));
                }
            }
        });

        // Sort order - button 2
//        final ImageButton ib2 = (ImageButton) dialogView.findViewById(R.id.image_button_2);
//
//        if (prefs.getBoolean(SORT_SECONDARY_KEY, true)) {
//            ib2.setImageDrawable(getDrawable(R.drawable.ic_action_sort_ascending));
//        } else {
//            ib2.setImageDrawable(getDrawable(R.drawable.ic_action_sort_descending));
//        }
//
//        ib2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (prefs.getBoolean(SORT_SECONDARY_KEY, true)) {
//                    // Currently set as ascending, so switch to descending
//                    setBooleanPreference(SORT_SECONDARY_KEY, false);
//                    ib2.setImageDrawable(getDrawable(R.drawable.ic_action_sort_descending));
//                } else {
//                    // Currently set as descending, so switch to ascending
//                    setBooleanPreference(SORT_SECONDARY_KEY, true);
//                    ib2.setImageDrawable(getDrawable(R.drawable.ic_action_sort_ascending));
//                }
//            }
//        });

//        dialogBuilder.setTitle(getResources().getString(R.string.pref_title_sort_field));
        dialogBuilder.setPositiveButton(getString(R.string.dialog_sort_button_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get spinner position and set sort preference to corresponding value
                int spinnerPosition = spinner1.getSelectedItemPosition();
                setStringPreference(KEY_SORT_FIELD, getResources()
                        .getStringArray(R.array.pref_sort_field_values)[spinnerPosition]);

                // Update data
                prepareData();
            }
        });
        dialogBuilder.setNegativeButton(getString(R.string.dialog_cancel_button_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    // For sort field
    private void setStringPreference(String key, String pref) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, pref);
        editor.apply();
    }

    // For sort order
    private void setBooleanPreference(String key, boolean pref) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, pref);
        editor.apply();
    }

    /**
     * An event item has been pressed, and needs to be handled.
     * @param event The event that was pressed.
     * @param flag  A flag to indicate what action to take on the event.
     */
    @Override
    public void onReceiveEventFromAdapter(Event event, String flag) {
        selectedEvent = event;

        if (flag.equalsIgnoreCase(EventLogAdapter.FLAG_MARK_EVENT_DONE)) {
            String doneDatePref = prefs.getString("date_behaviour", "0");

            // Mark event as done
            // Need to consider date behaviour preference.
            if (doneDatePref.equalsIgnoreCase(getResources()
                    .getStringArray(R.array.pref_default_date_values)[0])) {
                // Show the date picker and save event after a date is selected and received
                // (See associated methods below).
                showDatePickerDialog(getCurrentFocus());
            } else if (doneDatePref.equalsIgnoreCase(getResources()
                    .getStringArray(R.array.pref_default_date_values)[1])) {
                // Mark currently opened event as done today
                databaseHandler.markEventDone(event, new DateHandler().getTodayDate());
                prepareData();
            }
        } else if (flag.equalsIgnoreCase(EventLogAdapter.FLAG_SHOW_EVENT_INFO)) {
            // Intent to show event info
            Intent info = new Intent(this, EventInfoActivity.class);
            info.putExtra("event_id", event.getId());
            startActivity(info);
        }

    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerDialog();
        newFragment.show(getSupportFragmentManager(), "datePickerDone");
    }

    /**
     * The tick button was pressed and the event to be marked done now has an associated done
     * date from the dialog.
     * @param date The date the event was done.
     */
    @Override
    public void onReceiveDateFromDialog(Date date) {
        // Attempt to mark currently opened event as done
        databaseHandler.markEventDone(selectedEvent, date);
        prepareData();
    }

    public void scheduleNotification(int alarmType) {
        // Get notifications preference
        boolean showNotifications = prefs.getBoolean(KEY_NOTIFICATIONS, true);

        if (showNotifications) {
            // Intent to schedule notifications
            Intent notifyIntent = new Intent(getApplicationContext(), NotificationService.class);
//        notifyIntent.putExtra(NotificationService.EXTRA_ALARM_TRIGGERED, true);
//        notifyIntent.putExtra(NotificationService.EXTRA_UPDATE_CITY, getCityName());
//        notifyIntent.putExtra(NotificationService.EXTRA_UPDATE_FUEL, getFuelType());
            PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Set the alarm time
            Calendar alarmTime = Calendar.getInstance();
            int alarmHour = 7;     // At 7:00 AM, or TODO: by preference

            if (alarmType == ALARM_DEFAULT) {
                alarmTime.set(Calendar.HOUR_OF_DAY, alarmHour);
                alarmTime.set(Calendar.MINUTE, 0);
                alarmTime.set(Calendar.SECOND, 0);

                // If current time is after today's alarm, set it for tomorrow
                Calendar now = Calendar.getInstance();
                if (now.get(Calendar.HOUR_OF_DAY) >= alarmHour) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1);
                }
            } else if (alarmType == ALARM_TEST_DELAYED) {
                alarmTime.setTimeInMillis(System.currentTimeMillis());
                alarmTime.add(Calendar.SECOND, 3);
            }

            Log.d(TAG, "Notifications scheduled for: " + alarmTime.getTime().toString());

            // With setInexactRepeating(), you have to use one of the AlarmManager interval
            // constants - in this case, AlarmManager.INTERVAL_DAY.
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // TODO: Switch this after testing phase
//            am.setInexactRepeating(AlarmManager.RTC, alarmTime.getTimeInMillis(),
//                    AlarmManager.INTERVAL_DAY, pendingIntent);
            am.setExact(AlarmManager.RTC, alarmTime.getTimeInMillis(), pendingIntent);
        }
    }
}
