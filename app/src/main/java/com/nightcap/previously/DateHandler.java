package com.nightcap.previously;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Support class for converting between Dates, Calendars, and Strings.
 */

class DateHandler {

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

}
