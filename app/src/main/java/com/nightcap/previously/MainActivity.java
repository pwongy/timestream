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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private DbHandler dbHandler;

    // RecyclerView
    private List<Event> eventList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate xml layout
        setContentView(R.layout.activity_main);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start new Activity to add an event
                Intent addEvent = new Intent(getApplicationContext(), EventActivity.class);
                startActivity(addEvent);
            }
        });

        // Get a data handler, which initialises Realm during construction
        dbHandler = new DbHandler(this);

        // Recycler view stuff
        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);

        // LayoutManager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Animator
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Decorator
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        // Adapter (must be set after LayoutManager)
        eventAdapter = new EventAdapter(eventList);
        recyclerView.setAdapter(eventAdapter);

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Toast.makeText(getApplicationContext(), "Clicked position: " + position,
                                Toast.LENGTH_SHORT).show();
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
        eventList = dbHandler.getEventList();

        // Send to adapter
        eventAdapter.updateData(eventList);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClearButtonClick(View view) {
        boolean isDbDeleted = dbHandler.resetDatabase();

        if (isDbDeleted){
            // Restart app
            Intent restartIntent = getIntent();
            restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(restartIntent);
        }
    }

    public void onGetButtonClick(View view) {

    }

}
