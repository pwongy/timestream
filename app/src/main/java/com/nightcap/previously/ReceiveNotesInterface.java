package com.nightcap.previously;

/**
 * Interface for passing events from adapter to activity.
 */

public interface ReceiveNotesInterface {
    // Idea from:
    //    http://stackoverflow.com/questions/35091857/passing-object-from-fragment-to-activity
    void onReceiveNotesFromBottomSheet(String notes);
}
