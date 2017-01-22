package com.nightcap.previously;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity. Displays existing database events.
 */

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
//        Log.d(TAG, "MainActivity created");

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
        eventAdapter = new EventAdapter(eventList);
        recyclerView.setAdapter(eventAdapter);

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
        eventList = dbHandler.getLatestDistinctEvents();

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
        final View dialogView = inflater.inflate(R.layout.dialog_sort, null);
        dialogBuilder.setView(dialogView);

        final Spinner spinner1 = (Spinner) dialogView.findViewById(R.id.spinner_sort_primary);
        final Spinner spinner2 = (Spinner) dialogView.findViewById(R.id.spinner_sort_secondary);

        dialogBuilder.setTitle(getResources().getString(R.string.pref_title_sort_first));
//        dialogBuilder.setMessage(getResources().getString(R.string.pref_title_sort_first));
        dialogBuilder.setPositiveButton("Sort", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int langpos = spinner1.getSelectedItemPosition();
                switch(langpos) {
                    case 0: //English
//                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                                .edit().putString("LANG", "en").commit();
//                        setLangRecreate("en");
                        return;
                    case 1: //Hindi
//                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                                .edit().putString("LANG", "hi").commit();
//                        setLangRecreate("hi");
                        return;
                    default: //By default set to english
//                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                                .edit().putString("LANG", "en").commit();
//                        setLangRecreate("en");
                        return;
                }
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

}
