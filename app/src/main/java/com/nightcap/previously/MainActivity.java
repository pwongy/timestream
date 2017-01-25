package com.nightcap.previously;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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
import java.util.Arrays;
import java.util.List;

/**
 * Main Activity. Displays existing database events.
 */

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";

    // Realm database
    private DbHandler dbHandler;

    // User preferences
    private SharedPreferences prefs;
    final String SORT_FIELD_KEY = "sort_primary_field";
    final String SORT_ORDER_ASCENDING_KEY = "sort_primary_ascending";
//    final String SORT_SECONDARY_KEY = "sort_secondary_ascending";

    // RecyclerView
    private List<Event> eventList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private EventLogAdapter eventLogAdapter;

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
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
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
        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        layoutManager = new LinearLayoutManager(this);                          // LayoutManager
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());                // Animator
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);    // Decorator
        recyclerView.addItemDecoration(itemDecoration);

        // Adapter (must be set after LayoutManager)
        eventLogAdapter = new EventLogAdapter(eventList);
        recyclerView.setAdapter(eventLogAdapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // Intent to show event info
                        Intent info = new Intent(getApplicationContext(), EventInfoActivity.class);
                        info.putExtra("event_id", eventList.get(position).getId());
                        startActivity(info);
                    }
                }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareData();
    }

    private void prepareData() {
        // Get data from Realm
//        eventList = dbHandler.getAllEvents();
        eventList = dbHandler.getLatestDistinctEvents(prefs.getString(SORT_FIELD_KEY, "name"),
                prefs.getBoolean(SORT_ORDER_ASCENDING_KEY, true));

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

    public void showSortDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sort, null);
        dialogBuilder.setView(dialogView);

        final Spinner spinner1 = (Spinner) dialogView.findViewById(R.id.spinner_sort_primary);
//        final Spinner spinner2 = (Spinner) dialogView.findViewById(R.id.spinner_sort_secondary);

        // Set initial spinner selection
        int position1 = Arrays.asList(getResources().getStringArray(R.array.pref_sort_field_values))
                .indexOf(prefs.getString(SORT_FIELD_KEY, "name"));
        spinner1.setSelection(position1);

        // Set initial sort order image from preferences
        final ImageButton ib1 = (ImageButton) dialogView.findViewById(R.id.image_button_1);
        if (prefs.getBoolean(SORT_ORDER_ASCENDING_KEY, true)) {
            ib1.setImageDrawable(getDrawable(R.drawable.ic_action_sort_ascending));
        } else {
            ib1.setImageDrawable(getDrawable(R.drawable.ic_action_sort_descending));
        }

        // Switch order on click
        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (prefs.getBoolean(SORT_ORDER_ASCENDING_KEY, true)) {
                    // Currently set as ascending, so switch to descending
                    setBooleanPreference(SORT_ORDER_ASCENDING_KEY, false);
                    ib1.setImageDrawable(getDrawable(R.drawable.ic_action_sort_descending));
                } else {
                    // Currently set as descending, so switch to ascending
                    setBooleanPreference(SORT_ORDER_ASCENDING_KEY, true);
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
                setStringPreference(SORT_FIELD_KEY, getResources()
                        .getStringArray(R.array.pref_sort_field_values)[spinnerPosition]);

                // Update data
                prepareData();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
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


}
