package com.nightcap.previously;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.Date;

import io.fabric.sdk.android.Fabric;

/**
 * Activity for editing events before storage to database.
 */

public class EditActivity extends AppCompatActivity {
    private String TAG = "EventActivity";

    // Get a Realm handler, since we are editing the event log
    DatabaseHandler databaseHandler;

    // Editing
    private boolean isEditExistingEvent;
    int editId;
    String oldName;
    int oldPeriod;

    // Input fields
    EditText inputName;
    EditText inputDate;
    EditText inputPeriod;
    EditText inputNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up views
        setContentView(R.layout.activity_edit);
        inputName = (EditText) findViewById(R.id.event_name);
        inputDate = (EditText) findViewById(R.id.event_date);
        inputPeriod = (EditText) findViewById(R.id.event_period);
        inputNotes = (EditText) findViewById(R.id.event_notes);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        // Get Realm handler
        databaseHandler = new DatabaseHandler(this);

        // Set FocusListener on date field EditText
        inputDate.setOnFocusChangeListener(focusListener);

        // Check if editing
        editId = getIntent().getIntExtra("edit_id", -1);
        if (editId > 0) {
            // Editing existing event
            isEditExistingEvent = true;

            // TODO: Keep track of old values
            oldName = databaseHandler.getEventById(editId).getName();

            // Pre-fill existing values
            inputName.setText(oldName);
            inputDate.setText(new DateHandler().dateToString(databaseHandler.getEventById(editId).getDate()));
            int period = databaseHandler.getEventById(editId).getPeriod();
            String periodStr;
            if (period <=0) {
                periodStr = "N/A";
            } else {
                periodStr = String.valueOf(period);
            }
            inputPeriod.setText(periodStr);
            inputNotes.setText(databaseHandler.getEventById(editId).getNotes());
        } else {
            // Creating new event
            isEditExistingEvent = false;

            // Default date to today if preferred
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final String doneDatePref = prefs.getString("default_done_today", "0");

            if (doneDatePref.equalsIgnoreCase(getResources()
                    .getStringArray(R.array.pref_default_date_values)[1])) {
                inputDate.setText(new DateHandler().dateToString(new Date()));
            } else {
                inputDate.setHint(new DateHandler().dateToString(new Date()));
            }
        }

        // Floating action button
//        this.fab = (FloatingActionButton) findViewById(R.id.event_fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Start new Activity to add an event
//            }
//        });

        // Initialise Answers
        Fabric.with(this, new Answers());
    }

    @Override
    protected void onStart() {
        super.onStart();

//        RelativeLayout header = (RelativeLayout) findViewById(R.id.detail_head_space);
//        Log.i(TAG, "Header height: " + header.getHeight());
//        fab.setY(header.getHeight());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                break;
            case R.id.action_save_event:
                saveEvent();
                break;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerDialog();
        newFragment.show(getSupportFragmentManager(), "datePickerEdit");
    }

    private void saveEvent() {
        // Get data from fields
        // Name and date
        String eventName = inputName.getText().toString();
        String eventDate = inputDate.getText().toString();

        // Period
        int eventPeriod;
        try {
            eventPeriod = Integer.parseInt(inputPeriod.getText().toString());
        } catch (NumberFormatException e) {
            eventPeriod = 0;   // Use a zero value to indicate no repeats
        }

        // Notes
        String eventNotes = inputNotes.getText().toString();

        // Check inputs here
        if (eventName.length() == 0 || eventDate.length() == 0) {
            Log.d(TAG, "A required field is empty");
            Toast.makeText(getApplicationContext(), getString(R.string.toast_check_inputs),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Attempting to save event");
            DateHandler dh = new DateHandler();

            // Create unmanaged event
            Event event = new Event();

            // ID depends on whether we are creating a new event or editing an existing one.
            // Existing event should reuse their allocated ID.
            // New events should start a new ID (hence the increment).
            int id;
            if (isEditExistingEvent) {
                id = databaseHandler.getEventById(editId).getId();
            } else {
                id = databaseHandler.getEventCount() + 1;
            }
            event.setId(id);

            event.setName(eventName);
            event.setDate(dh.stringToDate(eventDate));
            event.setPeriod(eventPeriod);
            event.setNextDue(dh.nextDueDate(dh.stringToDate(eventDate), eventPeriod));
            event.setNotes(eventNotes);

            // Save the unmanaged event to Realm
            databaseHandler.saveEvent(event);
            if (!isEditExistingEvent) { // Must be a new event
                // Insight tracking via Answers
                Answers.getInstance().logCustom(new CustomEvent("Added a new event")
                        .putCustomAttribute("Event name", event.getName())
                        .putCustomAttribute("Repeating event", String.valueOf(event.hasPeriod())));
                Log.i(TAG, "Logged new event to Answers");
            }

            // TODO: Set common values for similar events
            if (!eventName.equals(oldName)) {
                Log.d(TAG, "Event name has changed.");
                databaseHandler.updateNameField(oldName, eventName);
            }

            // Return to main screen
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
    }

    private View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus){
                showDatePickerDialog(v);
            }
        }
    };

}
