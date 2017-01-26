package com.nightcap.previously;

/**
 * Interface for passing events from adapter to activity.
 */

public interface ReceiveEventInterface {
    // Idea from:
    //    http://stackoverflow.com/questions/35091857/passing-object-from-fragment-to-activity
    void onReceiveEventFromAdapter(Event event);
}
