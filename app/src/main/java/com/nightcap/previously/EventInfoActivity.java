package com.nightcap.previously;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity for displaying event details.
 */

public class EventInfoActivity extends AppCompatActivity implements ReceiveDateInterface, ReceiveEventInterface,
        ReceiveNotesInterface {
    private String TAG = "InfoActivity";

    private DatabaseHandler databaseHandler;
    private DateHandler dh = new DateHandler();
    private int eventId;
    private Event selectedEvent;

    private ActionBar actionBar;
    private FloatingActionButton fab;
    private TextView categoryView, periodView, nextDueView, countView, intervalView;

    RecyclerView historyRecyclerView;
    private List<Event> historyList = new ArrayList<>();
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        // Floating action button
        fab = (FloatingActionButton) findViewById(R.id.event_info_fab);
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
        eventId = getIntent().getIntExtra("event_id", 0);
        selectedEvent = databaseHandler.getEventById(eventId);

        // Get reference to ActionBar
        actionBar = getSupportActionBar();

        // Configure ActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // Card 1 - Info
        categoryView = (TextView) findViewById(R.id.card_info_category_value);
        periodView = (TextView) findViewById(R.id.card_info_period_value);
        nextDueView = (TextView) findViewById(R.id.card_info_next_due_value);

        // Card 2 - History
        historyRecyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.setItemAnimator(new DefaultItemAnimator());         // Animator
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);    // Decorator
        historyRecyclerView.addItemDecoration(itemDecoration);

        // Adapter (must be set after LayoutManager)
        historyAdapter = new HistoryAdapter(this, historyList);
        historyRecyclerView.setAdapter(historyAdapter);

        // Card 3 - Stats
        countView = (TextView) findViewById(R.id.card_stats_count_value);
        intervalView = (TextView) findViewById(R.id.card_stats_interval_value);
    }

    public void onReceiveDateFromDialog(Date date) {
        // Attempt to mark currently opened event as done
        databaseHandler.markEventDone(selectedEvent, date);
        prepareHistory();
    }

    public void onReceiveEventFromAdapter(Event event, String flag) {
        selectedEvent = event;

        if (flag.equalsIgnoreCase(HistoryAdapter.FLAG_ADD_NOTES)) {
            // Hide the FAB
            fab.hide();

            // Show the soft keyboard
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_FORCED);

            // Show the BottomSheet
            BottomSheetDialogFragment quickNotesFragment = new QuickNotesBottomSheetDialogFragment();
            quickNotesFragment.show(getSupportFragmentManager(), quickNotesFragment.getTag());
        } else if (flag.equalsIgnoreCase(HistoryAdapter.FLAG_EXPAND)) {
            // Update info on cards to match the selected event from history
            updateInfoCard();
        }
    }

    private void updateInfoCard() {
        // Category
        categoryView.setText(selectedEvent.getCategory());
        periodView.setTypeface(periodView.getTypeface(), Typeface.NORMAL);

        // Period and next due date
        if (selectedEvent.getPeriod() <= 0) {
            periodView.setText(getString(R.string.event_no_repeat));
            periodView.setTypeface(periodView.getTypeface(), Typeface.ITALIC);

            nextDueView.setText(getString(R.string.not_applicable));
            nextDueView.setTypeface(nextDueView.getTypeface(), Typeface.ITALIC);
        } else {
            String period = String.format(getString(R.string.event_period),
                    selectedEvent.getPeriod(), getString(R.string.unit_days));
            periodView.setText(period);
            periodView.setTypeface(null, Typeface.NORMAL);

            nextDueView.setText(dh.dateToString(selectedEvent.getNextDue()));
            nextDueView.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update event handle
        selectedEvent = databaseHandler.getEventById(eventId);
        actionBar.setTitle(selectedEvent.getName());

        prepareHistory();
        updateStatsCard();
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

    private void updateStatsCard() {
        // Event count
        int eventCount = databaseHandler.getEventCount(selectedEvent.getName());
        countView.setText(String.valueOf(eventCount));

        // Average interval
        double interval = databaseHandler.getAverageInterval(selectedEvent.getName());
        if (interval == 0) {
            intervalView.setText(getString(R.string.not_applicable));
            intervalView.setTypeface(null, Typeface.ITALIC);
        } else {
            DecimalFormat df = new DecimalFormat("##0.00");
            df.setRoundingMode(RoundingMode.HALF_UP);
            String intervalText = df.format(interval) + " " + getString(R.string.unit_days);
            intervalView.setText(intervalText);
            intervalView.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHandler.closeRealm();
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

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerDialog();
        newFragment.show(getSupportFragmentManager(), "datePickerDone");
    }

    @Override
    public void onReceiveNotesFromBottomSheet(String notes) {
        selectedEvent.setNotes(notes);
        databaseHandler.saveEvent(selectedEvent);
        prepareHistory();
    }

}
