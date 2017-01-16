package com.nightcap.previously;

import android.content.Context;
import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

class DbHandler {
    private String TAG = "DbHandler";
    private Context appContext;
    private Realm eventLog;    // Realm = database

    DbHandler(Context context) {
        appContext = context;

        // Initialise Realm
        Realm.init(appContext);

        // The Realm file will be located in Context.getFilesDir() with chosen name
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("event_log.realm")
                .schemaVersion(1)
                .build();
        // Use the config
        eventLog = Realm.getInstance(config);
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
        eventLog.copyToRealm(event);
        eventLog.commitTransaction();
        Log.d(TAG, "Event added to DB.");

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
}
