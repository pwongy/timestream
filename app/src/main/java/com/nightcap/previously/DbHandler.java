package com.nightcap.previously;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

class DbHandler {
    private String TAG = "DbHandler";
    private Context appContext;
    private DateHandler dateHandler;
    private Realm eventLog;    // Realm = database
    private SharedPreferences dataStore;
    private String KEY_EVENT_COUNT = "event_count";

    DbHandler(Context appContext) {
        this.appContext = appContext;
        dateHandler = new DateHandler();

        // Initialise Realm
        Realm.init(appContext);

        // The Realm file will be located in Context.getFilesDir() with chosen name
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("event_log.realm")
                .schemaVersion(1)
                .build();
        // Use the config
        eventLog = Realm.getInstance(config);

        // Get SharedPreferences file to support data management (not for settings)
        String sharedPrefsFilename = "data";
        dataStore = appContext.getSharedPreferences(sharedPrefsFilename, Context.MODE_PRIVATE);
    }

    /**
     * Saves an unmanaged event to the app's Realm database. Will check for existing events with the
     * same name and date before saving (in this case, the event will not be saved).
     * @param event    The unmanaged event to be saved
     */
    void saveEvent(Event event) {
        // First, check if the event has already been added
        final RealmResults<Event> existingEvents = eventLog.where(Event.class)
                .equalTo("name", event.getName())
                .equalTo("date", event.getDate())
                .findAll();
//        Log.d(TAG, "Existing: " + existingEvents.toString());

        if ( (existingEvents.size() == 0)    // The event does not exist for this day
                || (event.getId() <= getEventCount()) ) {  // Editing existing event

            // Persist data via transaction
            eventLog.beginTransaction();
            eventLog.copyToRealmOrUpdate(event);
            eventLog.commitTransaction();
            Log.d(TAG, "Event logged");

            incrementEventCount();
        } else {
            Log.d(TAG, "Duplicate event - ignored");
            Toast.makeText(appContext, "Event already exists", Toast.LENGTH_SHORT).show();
        }
    }

    void markEventDone(Event existingEvent, Date doneDate) {
        // Unmanaged event
        Event event = new Event();

        // ID will be new
        event.setId(getEventCount() + 1);
        event.setName(existingEvent.getName());
        event.setDate(doneDate);
        event.setPeriod(existingEvent.getPeriod());
        event.setNextDue(dateHandler.nextDueDate(dateHandler.getTodayDate(), existingEvent.getPeriod()));
        event.setNotes("");
        saveEvent(event);
    }

    /**
     * Deletes a single event from the Realm.
     * @param deleteId    The ID of the event record that is to be deleted
     */
    void deleteEvent(int deleteId) {
        final RealmResults<Event> results = eventLog.where(Event.class)
                .equalTo("id", deleteId)
                .findAll();
        if (results.size() == 1) {  // Check for unique identifier
            // All changes to data must happen in a transaction
            eventLog.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Event deleteEvent = results.first();
                    deleteEvent.deleteFromRealm();
                    Log.d(TAG, "Entry deleted");
                }
            });
        } else {
            Log.d(TAG, "Error during delete query.");
        }
    }

    String getEventTypes() {
        // Check for emptiness
        boolean isEmpty = eventLog.where(Event.class).findAll().isEmpty();
        Log.d(TAG, "Database is empty: " + isEmpty);

        final RealmResults<Event> events = eventLog.where(Event.class).findAll();
        return events.toString();
    }

    /**
     * Gets all events logged in the app's Realm.
     * @return A list of all logged events
     */
    List<Event> getAllEvents() {
        // All events
        final RealmResults<Event> events = eventLog.where(Event.class).findAll();

        // TODO: Sorting
        events.sort("nextDue", Sort.ASCENDING);

        List<Event> copied = eventLog.copyFromRealm(events);
        Log.d(TAG, "All events: " + copied.toString());
        return copied;
    }

    /**
     * Gets the latest distinct events logged in the app's Realm.
     * @return A list of matching events
     */
    List<Event> getLatestDistinctEvents() {
        // Start with all distinct events
        final RealmResults<Event> distinctEvents = eventLog.where(Event.class)
                .distinct("name")
                .sort("name");      // If sorting alphabetically

        List<Event> latestDistinctEvents = new ArrayList<>();
        for (Event e : distinctEvents) {
            RealmResults<Event> result = eventLog.where(Event.class)
                    .equalTo("name", e.getName())
                    .findAllSorted("date", Sort.DESCENDING);
//            Log.d(TAG, result.first().getName() + ": "
//                    + new DateHandler().dateToString(result.first().getDate())
//                    + " (latest of " + result.size() + ")");
            latestDistinctEvents.add(result.first());
        }

        return latestDistinctEvents;
    }

    List<Event> getEventsByName(String name) {
        final RealmResults<Event> events = eventLog.where(Event.class)
                .equalTo("name", name)
                .findAllSorted("date", Sort.DESCENDING);

        List<Event> copied = eventLog.copyFromRealm(events);
//        Log.d(TAG, "Events by name: " + copied.toString());
        return copied;
    }

    Event getEventById(int id) {
        final RealmResults<Event> event = eventLog.where(Event.class).equalTo("id", id).findAll();
        // Add sorting logic here

        Event copied = eventLog.copyFromRealm(event).get(0);
        Log.d(TAG, "Event by ID: " + copied.getName());
        return copied;
    }

    int getEventCount() {
        String dateKey = KEY_EVENT_COUNT;
        return dataStore.getInt(dateKey, 0);
    }

    /**
     * Saves event count to a SharedPreferences object, providing a means of assigning unique
     * primary keys for the Realm.
     */
    private void incrementEventCount() {
        SharedPreferences.Editor editor = dataStore.edit();

        String dateKey = KEY_EVENT_COUNT;   // Key
        int count = getEventCount();
        count++;
        editor.putInt(dateKey, count);      // Value

        // Commit the edits
        editor.apply();
    }
}
