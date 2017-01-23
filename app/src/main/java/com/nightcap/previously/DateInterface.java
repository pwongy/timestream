package com.nightcap.previously;

import java.util.Date;

/**
 * Interface for passing dates from fragment to activity.
 */

public interface DateInterface {
    void onReceiveDateFromDialog(Date date);
}
