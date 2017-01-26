package com.nightcap.previously;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main Activity. Displays existing database events.
 */

public class MainActivity extends AppCompatActivity implements ReceiveDateInterface, ReceiveEventInterface {
    private String TAG = "MainActivity";

    // Realm database
    private DbHandler dbHandler;

    // User preferences
    private SharedPreferences prefs;
    final String KEY_SORT_FIELD = "sort_primary_field";
    final String KEY_SORT_ORDER_ASCENDING = "sort_primary_ascending";
//    final String SORT_SECONDARY_KEY = "sort_secondary_ascending";

    // RecyclerView
    private List<Event> eventList = new ArrayList<>();
    private EventLogAdapter eventLogAdapter;

    Event selectedEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // User settings
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Inflate xml layout
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // FAB
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start new Activity to add an event
                Intent addEvent = new Intent(getApplicationContext(), EditActivity.class);
                startActivity(addEvent);
            }
        });

        // Get a data handler, which initialises Realm during construction
        dbHandler = new DbHandler(this);

        // Recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());                // Animator
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);    // Decorator
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareData();
    }

    private void prepareData() {
        // Get data from Realm
        eventList = dbHandler.getLatestDistinctEvents(prefs.getString(KEY_SORT_FIELD, "name"),
                prefs.getBoolean(KEY_SORT_ORDER_ASCENDING, true));

        // Send list to adapter
        eventLogAdapter.updateData(eventList);
    }

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

        dialogBuilder.setTitle(getResources().getString(R.string.pref_title_sort_field));
        dialogBuilder.setPositiveButton("Sort", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Get spinner position and set sort preference to corresponding value
                int spinnerPosition = spinner1.getSelectedItemPosition();
                setStringPreference(KEY_SORT_FIELD, getResources()
                        .getStringArray(R.array.pref_sort_field_values)[spinnerPosition]);

                // Update data
                prepareData();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    @Override
    public void onReceiveEventFromAdapter(Event event) {
        selectedEvent = event;
        String doneDatePref = prefs.getString("default_done_today", "0");

        // Mark event as done
        if (doneDatePref.equalsIgnoreCase(getResources()
                .getStringArray(R.array.pref_default_done_today_values)[0])) {
            showDatePickerDialog(getCurrentFocus());
        } else if (doneDatePref.equalsIgnoreCase(getResources()
                .getStringArray(R.array.pref_default_done_today_values)[1])) {
            // Mark currently opened event as done today
            dbHandler.markEventDone(event, new DateHandler().getTodayDate());
            prepareData();
        }
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerDialog();
        newFragment.show(getSupportFragmentManager(), "datePickerDone");
    }

    @Override
    public void onReceiveDateFromDialog(Date date) {
        // Attempt to mark currently opened event as done
        dbHandler.markEventDone(selectedEvent, date);
        prepareData();
    }
}
