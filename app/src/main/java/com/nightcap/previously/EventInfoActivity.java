package com.nightcap.previously;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for displaying event details.
 */

public class EventInfoActivity extends AppCompatActivity {
    private String TAG = "EventActivity";
    private DbHandler dbHandler;
    int eventId;
    Event selectedEvent;
    TextView periodView;
    TextView notesView;
    private List<Event> historyList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

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
                // Attempt to mark currently opened event as done
                dbHandler.markEventDoneToday(selectedEvent);
                prepareHistory();
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

        updateInfoCard();

        // Card 2 - History
        recyclerView = (RecyclerView) findViewById(R.id.history_recycler_view);

        layoutManager = new LinearLayoutManager(this);                          // LayoutManager
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());                // Animator
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);    // Decorator
        recyclerView.addItemDecoration(itemDecoration);

        // Adapter (must be set after LayoutManager)
        historyAdapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(historyAdapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Toast.makeText(getApplicationContext(),
                                "ID: " + historyList.get(position).getId()
                                        + ", Name: " + historyList.get(position).getName()
                                        + ", Date: " + historyList.get(position).getDate(),
                                Toast.LENGTH_LONG)
                                .show();

                        selectedEvent = historyList.get(position);
                        updateInfoCard();
                    }
                }
        );
    }

    private void updateInfoCard() {
        // Period
        if (selectedEvent.getPeriod() <= 0) {
            periodView.setText("N/A");
        } else {
            String period = String.valueOf(selectedEvent.getPeriod()) + " days";
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
                edit.putExtra("edit_id", selectedEvent.getId());   // TODO: Default to latest instance?
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
