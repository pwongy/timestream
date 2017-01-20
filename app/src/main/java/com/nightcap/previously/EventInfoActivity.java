package com.nightcap.previously;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Activity for displaying event details.
 */

public class EventInfoActivity extends AppCompatActivity {
    private String TAG = "EventActivity";
    private DbHandler dbHandler;
    int eventId;
    Event selectedEvent;
    CoordinatorLayout cl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        cl = (CoordinatorLayout) findViewById(R.id.event_info_coord);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_info_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get a Realm handler
        dbHandler = new DbHandler(this);

        // Get selected event
        eventId = getIntent().getIntExtra("event_id", 0);
        selectedEvent = dbHandler.getEventById(eventId);

        getSupportActionBar().setTitle(selectedEvent.getName());

        // Info
        TextView periodView = (TextView) findViewById(R.id.card_info_period);
        if (selectedEvent.getPeriod() <= 0) {
            periodView.setText("N/A");
        } else {
            String period = String.valueOf(selectedEvent.getPeriod()) + " days";
            periodView.setText(period);
        }

        TextView notesView = (TextView) findViewById(R.id.card_info_notes);
        notesView.setText(selectedEvent.getNotes());

        // Floating action button
//        this.fab = (FloatingActionButton) findViewById(R.id.event_fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Start new Activity to add an event
//            }
//        });

    }

    @Override
    protected void onStart() {
        super.onStart();

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
//                markEventDone();
                break;
            case R.id.action_edit:
                // Intent to edit event
                Intent edit = new Intent(getApplicationContext(), EditActivity.class);
                edit.putExtra("edit_id", selectedEvent.getId());   // TODO: Default to latest instance?
                startActivity(edit);
                break;
            case R.id.action_delete:
                dbHandler.deleteEvent(eventId);
                Snackbar.make(cl, "Event deleted", Snackbar.LENGTH_LONG).show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
