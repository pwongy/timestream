package com.nightcap.previously;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

class DatabaseHandler {
    private String TAG = "DatabaseHandler";
    private Context appContext;
    private DateHandler dateHandler;
    private Realm eventLog;    // Realm = database
    private SharedPreferences dataStore;
    private String KEY_EVENT_COUNT = "event_count";

    DatabaseHandler(Context appContext) {
        this.appContext = appContext;
        dateHandler = new DateHandler();
        Fabric.with(this.appContext, new Answers());

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

            String logMsg = "Event logged (%s)";
            logMsg = String.format(logMsg, event.getName());
            Log.d(TAG, logMsg);

            // Only increment event counter if it's a new instance (i.e. not editing)
            if (existingEvents.size() == 0) {
                incrementEventCount();
            }

        } else {
            Log.d(TAG, "Duplicate event - ignored");
            Toast.makeText(appContext, "Event already logged on " + dateHandler.dateToString(event.getDate()),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Marks an existing event as done via a new entry in the event log.
     * @param existingEvent    The event that was completed.
     * @param doneDate         The date the event was completed.
     */
    void markEventDone(Event existingEvent, Date doneDate) {
        // Create an unmanaged event.
        // This is like a variable. We need to set the event attributes before it is fully defined.
        // Only then should it be saved to the log.
        Event event = new Event();

        // Since this is a new entry in the log, the event ID should be new
        event.setId(getEventCount() + 1);

        // The event name is carried over from previous entries, while the date is set per user input
        event.setName(existingEvent.getName());
        event.setDate(doneDate);

        // The event period is also carried over
        event.setPeriod(existingEvent.getPeriod());
        // The next due date is then calculated relative to the last time the event was done
        event.setNextDue(dateHandler.nextDueDate(doneDate, existingEvent.getPeriod()));

        // Notes are blank until added manually
        event.setNotes("");

        // Once all the attribute fields are filled, we can save the event to the Realm
        saveEvent(event);

        // Insight tracking via Answers
        Answers.getInstance().logCustom(new CustomEvent("Logged an existing event")
                .putCustomAttribute("Event name", event.getName())
                .putCustomAttribute("Repeating event", String.valueOf(event.hasPeriod())));
        Log.i(TAG, "Logged event instance to Answers");
    }

    /**
     * Deletes a single event from the Realm.
     * @param deleteId    The ID of the event record that is to be deleted
     */
    void deleteEvent(int deleteId) {
        // Query the Realm for a matching event ID
        final RealmResults<Event> deleteQuery = eventLog.where(Event.class)
                .equalTo("id", deleteId)
                .findAll();
        if (deleteQuery.size() == 1) {  // Check for unique identifier
            // All changes to data must happen in a transaction
            eventLog.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Event deleteEvent = deleteQuery.first();
                    deleteEvent.deleteFromRealm();
                    Log.d(TAG, "Entry deleted");
                }
            });
        } else {
            Log.d(TAG, "Error during delete query.");
        }
    }

    /**
     * Gets all events logged in the app's Realm.
     * @return A list of all logged events
     */
    @Deprecated
    List<Event> getAllEvents() {
        // Query the Realm for all event instances (unsorted)
        final RealmResults<Event> queryResult = eventLog.where(Event.class)
                .findAll();
        List<Event> allEvents = eventLog.copyFromRealm(queryResult);
        Log.d(TAG, "All events: " + allEvents.toString());
        return allEvents;
    }

    /**
     * Gets the latest distinct events logged in the app's Realm.
     * @return A list of matching events
     */
    List<Event> getLatestDistinctEvents(String sortField, boolean isSortAscending) {
        // Get all distinct events
        final RealmResults<Event> distinctEvents = eventLog.where(Event.class)
                .distinct("name");

        // Find latest instance of each event
        List<Event> latestDistinctEvents = new ArrayList<>();
        for (Event e : distinctEvents) {
            // Get latest instance
            RealmResults<Event> result = eventLog.where(Event.class)
                    .equalTo("name", e.getName())
                    .findAllSorted("date", Sort.DESCENDING);
            latestDistinctEvents.add(result.first());
        }

        // Determine correct sort parameters
        Event.SortParameter order = Event.SortParameter.NAME_ASCENDING; // Default value
        if (sortField.equalsIgnoreCase("name")) {
            if (isSortAscending) order = Event.SortParameter.NAME_ASCENDING;
            else order = Event.SortParameter.NAME_DESCENDING;
        } else if (sortField.equalsIgnoreCase("date")) {
            if (isSortAscending) order = Event.SortParameter.DATE_ASCENDING;
            else order = Event.SortParameter.DATE_DESCENDING;
        } else if (sortField.equalsIgnoreCase("nextDue")) {
            if (isSortAscending) order = Event.SortParameter.NEXT_DUE_ASCENDING;
            else order = Event.SortParameter.NEXT_DUE_DESCENDING;
        }

        // Sort events
        Comparator<Event> cp = Event.getComparator(order);
        Collections.sort(latestDistinctEvents, cp);

        return latestDistinctEvents;
    }

    /**
     * Gets a list of overdue events.
     * @return The list of events that have passed their nextDue date.
     */
    List<Event> getOverdueEvents() {
        List<Event> overdueEvents = new ArrayList<>();
        List<Event> distinctEvents = getLatestDistinctEvents("name", true);

        for (Event e : distinctEvents) {
            // Get latest instance of each distinct event from event log
            RealmResults<Event> result = eventLog.where(Event.class)
                    .equalTo("name", e.getName())
                    .findAllSorted("nextDue", Sort.DESCENDING);
            Event candidate = result.first();
            // FIXME: Is this even necessary?

            // Check if event is overdue; if so, add it to the list
            if (candidate.hasPeriod() && !candidate.getNextDue().after(new DateHandler().getTodayDate())) {
                Log.d(TAG, candidate.getName() + " is overdue");
                overdueEvents.add(candidate);
            }
        }

        return overdueEvents;
    }

    /**
     * Method for propagating updated event name through to other records in the Realm.
     * @param oldName The old event name.
     * @param newName The new event name.
     */
    void updateNameField(String oldName, String newName) {
        // Get list of events matching the old name
        List<Event> sameEvents = getEventsByName(oldName);

        // Update each event with the new name
        for (Event e : sameEvents) {
            e.setName(newName);
            saveEvent(e);
        }
    }

    /**
     * Find all events in the log that match a particular name.
     * @param name    The event name for which to search.
     * @return The list of matching events.
     */
    List<Event> getEventsByName(String name) {
        final RealmResults<Event> queryResults = eventLog.where(Event.class)
                .equalTo("name", name)
                .findAllSorted("date", Sort.DESCENDING);

        List<Event> eventsMatchingName = eventLog.copyFromRealm(queryResults);
//        Log.d(TAG, "Events by name: " + eventsMatchingName.toString());
        return eventsMatchingName;
    }

    /**
     * Find all events in the log that match a particular ID.
     * @param id    The event id for which to search.
     * @return The matching event (should be singular).
     */
    Event getEventById(int id) {
        final RealmResults<Event> queryResults = eventLog.where(Event.class)
                .equalTo("id", id)
                .findAll();

        Event eventMatchingId = eventLog.copyFromRealm(queryResults).get(0);
//        Log.d(TAG, "Event by ID: " + eventMatchingId.getName());
        return eventMatchingId;
    }

    /**
     * Closes the realm.
     */
    void closeRealm() {
        eventLog.close();
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

        // Store this as a key-value pair
        String dateKey = KEY_EVENT_COUNT;   // Key
        int count = getEventCount();
        count++;
        editor.putInt(dateKey, count);      // Value

        // Commit the edits
        editor.apply();
    }
}
