package com.nightcap.previously;

import android.text.format.DateUtils;
import android.util.Log;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper class for converting between Dates, Calendars, and Strings.
 */

class DateHandler {
    String TAG = "DateHandler";

    DateHandler() {

    }

    String dateToString(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return DateFormat.getDateInstance().format(cal.getTime());
    }

    String dateToString(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    Date stringToDate(String dateString) {
        DateFormat df = DateFormat.getDateInstance();
        Date date = new Date();

        try {
            date = df.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }

    Date dateFromComponents(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        // Reset hour, minutes, seconds, and millis
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Gets today's date, ignoring time fields.
     * @return Today's date.
     */
    Date getTodayDate() {
        Calendar cal = Calendar.getInstance();

        // Reset hour, minutes, seconds, and millis
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * Calculates the next due date given when the event was last performed and the desired repeat
     * period.
     * @param lastTime    The last date event was performed
     * @param period      The repeat period (assumed in days)
     * @return The next date on which event should be performed
     */
    Date nextDueDate(Date lastTime, int period) {
        Date nextTime;

        // Convert lastTime to Calendar
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastTime);

        // Increment by period
        cal.add(Calendar.DATE, period);

        nextTime = cal.getTime();
        return nextTime;
    }

    long getDaysBetween(Date relativeDate, Date anchorDate) {
        // Compute elapsed time since the latest data record
        String relativeDays = DateUtils.getRelativeTimeSpanString(relativeDate.getTime(),
                anchorDate.getTime(), DateUtils.DAY_IN_MILLIS).toString();
        Log.d(TAG, "String: " + relativeDays);

//        if (relativeDays.substring(relativeDays.length() - 4).equalsIgnoreCase("days")) {
//            Log.d(TAG, "")
//        }

        // Time difference in millis
        long difference = relativeDate.getTime() - anchorDate.getTime();
        long days = difference / DateUtils.DAY_IN_MILLIS;
//        long remainder = difference % DateUtils.DAY_IN_MILLIS;

//        Log.d(TAG, "Relative days: " + days);
//        Log.d(TAG, " -- Remainder: " + remainder);

//        if (relativeDays.equalsIgnoreCase("Yesterday")) {
//            daysBetween = 1;
//        } else {
//            // Get the number of days since the latest stored data.
//            // Smallest number expected is 2, representing the day before yesterday.
//            // Use this to determine the number of new data points to shift and add.
//            int firstSpaceIndex = relativeDays.indexOf(' ');
//            daysBetween = Integer.parseInt(relativeDays.substring(0, firstSpaceIndex));
//        }
        return days;
    }

    String getRelativeDaysString(long daysBetween) {
        String relativeDays = "";

        if (daysBetween > 1) {
            relativeDays = "In " + daysBetween + " days";
        } else if (daysBetween == 1) {
            relativeDays = "Tomorrow";
        } else if (daysBetween == 0) {
            relativeDays = "Today";
        } else if (daysBetween == -1) {
            relativeDays = "Yesterday";
        } else if (daysBetween < -1) {
            relativeDays = -daysBetween + " days ago";
        }

        return relativeDays;
    }

}
