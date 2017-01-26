package com.nightcap.previously;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity for displaying event details.
 */

public class EventInfoActivity extends AppCompatActivity implements ReceiveDateInterface, ReceiveEventInterface {
    private String TAG = "EventActivity";
    private DbHandler dbHandler;
    int eventId;
    Event selectedEvent;
    TextView periodView;
    TextView notesView;
    private List<Event> historyList = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        // User settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String doneDatePref = prefs.getString("default_done_today", "0");

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_info_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.event_info_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, doneDatePref);

                if (doneDatePref.equalsIgnoreCase(getResources()
                        .getStringArray(R.array.pref_default_done_today_values)[0])) {
                    showDatePickerDialog(view);
                } else if (doneDatePref.equalsIgnoreCase(getResources()
                        .getStringArray(R.array.pref_default_done_today_values)[1])) {
                    // Mark currently opened event as done today
                    dbHandler.markEventDone(selectedEvent, new DateHandler().getTodayDate());
                    prepareHistory();
                }
            }
        });

        // Get a Realm handler
        dbHandler = new DbHandler(this);

        // Get selected event
        eventId = getIntent().getIntExtra("event_id", 0);
        selectedEvent = dbHandler.getEventById(eventId);

        getSupportActionBar().setTitle(selectedEvent.getName());

        // Card 1 - Info
        periodView = (TextView) findViewById(R.id.card_info_period);
        notesView = (TextView) findViewById(R.id.card_info_notes);

        // Card 2 - History
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());                // Animator
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);    // Decorator
        recyclerView.addItemDecoration(itemDecoration);

        // Adapter (must be set after LayoutManager)
        historyAdapter = new HistoryAdapter(this, historyList);
        recyclerView.setAdapter(historyAdapter);
    }

    public void onReceiveDateFromDialog(Date date) {
        // Attempt to mark currently opened event as done
        dbHandler.markEventDone(selectedEvent, date);
        prepareHistory();
    }

    public void onReceiveEventFromAdapter(Event event) {
        // Update info on cards to match the selected event from history
        selectedEvent = event;
        updateInfoCard();
    }

    private void updateInfoCard() {
        // Period
        if (selectedEvent.getPeriod() <= 0) {
            periodView.setText("N/A");
        } else {
            String period = String.valueOf(selectedEvent.getPeriod()) + " "
                    + getString(R.string.unit_suffix_days);
            periodView.setText(period);
        }

        // Notes
        notesView.setText(selectedEvent.getNotes());
    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareHistory();
    }

    private void prepareHistory() {
        // Get data from Realm
        historyList = dbHandler.getEventsByName(selectedEvent.getName());

        // Send to adapter
        historyAdapter.updateData(historyList);

        // Update info card for selected event
        selectedEvent = historyList.get(0);
        updateInfoCard();
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerDialog();
        newFragment.show(getSupportFragmentManager(), "datePickerDone");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If app icon in Action Bar clicked, go home
                finish();
                break;
            case R.id.action_edit:
                // Intent to edit event
                Intent edit = new Intent(getApplicationContext(), EditActivity.class);
                edit.putExtra("edit_id", selectedEvent.getId());
                startActivity(edit);
                break;
            case R.id.action_delete:
                dbHandler.deleteEvent(selectedEvent.getId());
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
