package com.nightcap.previously;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Support class for converting between Dates, Calendars, and Strings.
 */

public class DateHandler {

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
            System.out.println("Date ->" + date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }

}
