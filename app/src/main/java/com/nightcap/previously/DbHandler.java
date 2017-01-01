package com.nightcap.previously;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmResults;

class DbHandler {
    private String TAG = "DbHandler";
    private Context appContext;
    private Realm realm;    // A Realm is a database

    DbHandler(Context context) {
        appContext = context;

        // Initialise Realm
        Realm.init(appContext);
        realm = Realm.getDefaultInstance();
    }

    void saveEventType(Event event) {
        // First, see if event already added
        boolean isEventExists = false;
        final RealmResults<Event> existingEvents = realm.where(Event.class).equalTo("type", event.getType()).findAll();
        if (existingEvents.size() != 0) {
            isEventExists = true;
            Log.d(TAG, "Duplicate event...will not be added to DB.");
        }

        if (!isEventExists) {
            // Persist data via transaction
            realm.beginTransaction();
            realm.copyToRealm(event);
            realm.commitTransaction();

            Log.d(TAG, "Event added to DB.");
        }
    }

    String getEventTypes() {
        // Check for emptiness
        boolean isEmpty = realm.where(Event.class).findAll().isEmpty();
        Log.d(TAG, "Database is empty: " + isEmpty);

        final RealmResults<Event> events = realm.where(Event.class).findAll();
        return events.toString();
    }

    boolean resetDatabase() {
        boolean isDbDeleted = false;
        Realm db = realm;

        try {
            db.close();
            isDbDeleted = Realm.deleteRealm(realm.getConfiguration());
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
