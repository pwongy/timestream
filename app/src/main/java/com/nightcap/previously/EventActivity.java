package com.nightcap.previously;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by Paul on 8/11/2016.
 */

public class EventActivity extends AppCompatActivity {
    private String TAG = "EventActivity";
//    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        // Floating action button
//        this.fab = (FloatingActionButton) findViewById(R.id.event_fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Start new Activity to add an event
//            }
//        });

        // Change editText underline colour
//        EditText nameEditText = (EditText) findViewById(R.id.event_name);
//        nameEditText.getBackground().mutate()
//                .setColorFilter(getResources().getColor(R.color.colorEditTextUnderline),
//                        PorterDuff.Mode.SRC_ATOP);

    }

    @Override
    protected void onStart() {
        super.onStart();

        RelativeLayout header = (RelativeLayout) findViewById(R.id.detail_head_space);
        Log.i(TAG, "Header height: " + header.getHeight());
//        fab.setY(header.getHeight());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
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
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void saveEvent() {
        // Get handler
        DbHandler dbHandler = new DbHandler(this);

        // Get data from fields
        EditText inputName = (EditText) findViewById(R.id.event_name);
        String eventName = inputName.getText().toString();

        EditText inputDate = (EditText) findViewById(R.id.event_date);
        String eventDate = inputDate.getText().toString();

        EditText inputNotes = (EditText) findViewById(R.id.event_notes);
        String eventNotes = inputNotes.getText().toString();

        if (eventName.length() == 0 || eventDate.length() == 0) {
            Log.d(TAG, "A required field is empty");
            Toast.makeText(getApplicationContext(), "Event name and date must be filled in", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "Attempting to save event");

            // Save new event
            Event event = new Event();
            event.setName(eventName);
            event.setDate(eventDate);
            event.setNotes(eventNotes);
            dbHandler.saveEvent(event);

            // Return to main screen
            Intent homeIntent = new Intent(this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
    }

}
