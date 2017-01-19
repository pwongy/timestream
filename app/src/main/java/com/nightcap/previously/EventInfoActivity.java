package com.nightcap.previously;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Activity for displaying event details.
 */

public class EventInfoActivity extends AppCompatActivity {
    private String TAG = "EventActivity";
    private DbHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_info_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int eventId = getIntent().getIntExtra("event_id", 0);
        dbHandler = new DbHandler(this);
        Event selected = dbHandler.getEventById(eventId);
        Toast.makeText(getApplicationContext(), "Event in info: " + selected.getName(),
                Toast.LENGTH_SHORT).show();

        getSupportActionBar().setTitle(selected.getName());


        // Date field EditText
//        EditText dateEdit = (EditText) findViewById(R.id.event_date);
//        dateEdit.setOnFocusChangeListener(focusListener);

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
//                markEventDone();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
