package com.nightcap.previously;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class DbHandler {
    private String TAG = "DbHandler";
    private Realm eventLog;    // Realm = database

    private SharedPreferences dataStore;
    private String spDataFilename = "data";
    private String KEY_EVENT_COUNT = "event_count";

    DbHandler(Context appContext) {
        // Initialise Realm
        Realm.init(appContext);

        // The Realm file will be located in Context.getFilesDir() with chosen name
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("event_log.realm")
                .schemaVersion(1)
                .build();
        // Use the config
        eventLog = Realm.getInstance(config);

        // Get SharedPreferences file
        dataStore = appContext.getSharedPreferences(spDataFilename, Context.MODE_PRIVATE);
    }

    void saveEvent(Event event) {
        // First, check if the event has already been added
        boolean isEventExists = false;
        final RealmResults<Event> existingEvents = eventLog.where(Event.class)
                .equalTo("name", event.getName())
                .findAll();
        if (existingEvents.size() != 0) {
            Log.d(TAG, "Duplicate event.");
            isEventExists = true;
        } else {

        }

        // Persist data via transaction
        eventLog.beginTransaction();
        eventLog.copyToRealmOrUpdate(event);
        eventLog.commitTransaction();
        Log.d(TAG, "Event added to DB.");

        incrementEventCount();
    }

    String getEventTypes() {
        // Check for emptiness
        boolean isEmpty = eventLog.where(Event.class).findAll().isEmpty();
        Log.d(TAG, "Database is empty: " + isEmpty);

        final RealmResults<Event> events = eventLog.where(Event.class).findAll();
        return events.toString();
    }

    public List<Event> getAllEvents() {
        final RealmResults<Event> events = eventLog.where(Event.class).findAll();
        // Add sorting logic here

        List<Event> copied = eventLog.copyFromRealm(events);
        Log.d(TAG, "Copied data: " + copied.toString());
        return copied;
    }

    boolean resetDatabase() {
        boolean isDbDeleted = false;
        Realm db = eventLog;

        try {
            db.close();
            isDbDeleted = Realm.deleteRealm(eventLog.getConfiguration());
            if (isDbDeleted) {
                Log.d(TAG, "Realm file has been deleted.");
            }
        } catch (Exception ex){
            ex.printStackTrace();
            // No Realm file to remove.
        }

        return isDbDeleted;
    }

    int getEventCount() {
        String dateKey = KEY_EVENT_COUNT;
        return dataStore.getInt(dateKey, 0);
    }

    void incrementEventCount() {
        SharedPreferences.Editor editor = dataStore.edit();

        String dateKey = KEY_EVENT_COUNT;   // Key
        int count = getEventCount();
        count++;
        editor.putInt(dateKey, count);      // Value

        // Commit the edits
        editor.apply();
    }
}
