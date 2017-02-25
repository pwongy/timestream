package com.nightcap.previously;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    private String TAG = "InfoActivity";
    private DatabaseHandler databaseHandler;
    private DateHandler dh = new DateHandler();
    private Event selectedEvent;
    private TextView periodView, nextDueView, notesView;
    private List<Event> historyList = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        // Get reference to ActionBar
        ActionBar actionBar = getSupportActionBar();

        // Floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.event_info_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to edit event
                Intent edit = new Intent(getApplicationContext(), EditActivity.class);
                edit.putExtra("edit_id", selectedEvent.getId());
                startActivity(edit);
            }
        });

        // Get a Realm handler
        databaseHandler = new DatabaseHandler(this);

        // Get selected event
        int eventId = getIntent().getIntExtra("event_id", 0);
        selectedEvent = databaseHandler.getEventById(eventId);

        // Configure ActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(selectedEvent.getName());
        }

        // Card 1 - Info
        periodView = (TextView) findViewById(R.id.card_info_period_value);
        nextDueView = (TextView) findViewById(R.id.card_info_next_due_value);
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
        databaseHandler.markEventDone(selectedEvent, date);
        prepareHistory();
    }

    public void onReceiveEventFromAdapter(Event event, String flag) {
        // Update info on cards to match the selected event from history
        selectedEvent = event;
        updateInfoCard();
    }

    private void updateInfoCard() {
        // Period and next due date
        if (selectedEvent.getPeriod() <= 0) {
            periodView.setText(getString(R.string.event_no_repeat));
            periodView.setTypeface(periodView.getTypeface(), Typeface.ITALIC);

            nextDueView.setText(getString(R.string.event_not_applicable));
            nextDueView.setTypeface(nextDueView.getTypeface(), Typeface.ITALIC);
        } else {
            String period = String.format(getString(R.string.event_period),
                    selectedEvent.getPeriod(), getString(R.string.unit_days));
            periodView.setText(period);
            periodView.setTypeface(null, Typeface.NORMAL);

            nextDueView.setText(dh.dateToString(selectedEvent.getNextDue()));
            nextDueView.setTypeface(null, Typeface.NORMAL);
        }

        // Notes
        notesView.setText(selectedEvent.getNotes());
        if (selectedEvent.getNotes().equalsIgnoreCase("")) {
            notesView.setText(getString(R.string.event_notes_blank));
            notesView.setTypeface(notesView.getTypeface(), Typeface.ITALIC);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        prepareHistory();
    }

    private void prepareHistory() {
        // Get data from Realm
        historyList = databaseHandler.getEventsByName(selectedEvent.getName());

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
            case R.id.action_mark_done:
                // Show date picker
                showDatePickerDialog(getCurrentFocus());
                break;
            case R.id.action_delete:
                databaseHandler.deleteEvent(selectedEvent.getId());
//                prepareHistory();
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
