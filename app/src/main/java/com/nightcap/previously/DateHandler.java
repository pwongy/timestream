package com.nightcap.previously;

import android.text.format.DateUtils;

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

    /**
     * Provides a formatted string corresponding to date components.
     * @param year     Year
     * @param month    Month
     * @param day      Day of month
     * @return The corresponding date string.
     */
    String dateToString(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return DateFormat.getDateInstance().format(cal.getTime());
    }

    /**
     * Provides a formatted string corresponding to a Date.
     * @param date      Date
     * @return The corresponding date string.
     */
    String dateToString(Date date) {
        return DateFormat.getDateInstance().format(date);
    }

    /**
     * Provides a date corresponding to a formatted string.
     * @param dateString      The date string
     * @return The corresponding date object.
     */
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

    /**
     * Provides a date corresponding to specified date components.
     * @param year     Year
     * @param month    Month
     * @param day      Day of month
     * @return The corresponding date object.
     */
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
     * Convenience method of getting today's date, ignoring time fields.
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

    /**
     * Compute elapsed time between two days.
     *
     * @param relativeDate    The day of interest.
     * @param anchorDate      The fixed day for comparison.
     * @return The number of days in between the two specified days.
     */
    long getDaysBetween(Date relativeDate, Date anchorDate) {
        // Time difference in millis
        long difference = relativeDate.getTime() - anchorDate.getTime();
        long daysBetween = difference / DateUtils.DAY_IN_MILLIS;
//        long remainder = difference % DateUtils.DAY_IN_MILLIS;

//        Log.d(TAG, "Relative days: " + days);
//        Log.d(TAG, " -- Remainder: " + remainder);

        return daysBetween;
    }

    /**
     * Format the day difference into the desired string format.
     *
     * @param daysBetween    The number of days difference.
     * @return String corresponding to the difference.
     */
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
