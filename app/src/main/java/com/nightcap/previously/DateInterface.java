package com.nightcap.previously;

import java.util.Date;

/**
 * Interface for passing dates from fragment to activity.
 */

public interface DateInterface {

    // Idea from:
    //    http://stackoverflow.com/questions/35091857/passing-object-from-fragment-to-activity
    void onReceiveDateFromDialog(Date date);
}
