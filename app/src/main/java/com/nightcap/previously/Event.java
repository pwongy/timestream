package com.nightcap.previously;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Class representing each event in the log.
 */

public class Event extends RealmObject {
    @PrimaryKey
    private int id;

    @Index
    private String name;                        // Common
    private Date date;
    private int periodInDays;
    private Date nextDue;                       // Dependent on period
    private String notes;

    private boolean notifications;              // Common

//    private String category;                    // Common
//    private boolean isPinned;                   // Common
//    private boolean isPrivate;                  // Common
//    private boolean isArchived;                 // Common

    // Other ideas:
    //  - Category
    //  - Notifications >> Warning date (days before), on Due Date, post-reminders repeat period
    //  - Pinned events
    //  - Private events
    //  - Archived events

    public Event() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getPeriod() {
        return periodInDays;
    }

    public void setPeriod(int periodInDays) {
        this.periodInDays = periodInDays;
    }

    public boolean hasPeriod() {
        return (this.getPeriod() > 0);
    }

    public Date getNextDue() {
        return nextDue;
    }

    public void setNextDue(Date nextDue) {
        this.nextDue = nextDue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    boolean hasNotes() {
        return !notes.isEmpty();
    }

    /**
     * Customised comparison criteria for sorting a list of Events. See answer by runaros:
     * http://stackoverflow.com/questions/1421322/how-do-i-sort-a-list-by-different-parameters-at-different-timed
     */
    static Comparator<Event> getComparator(SortParameter... sortParameters) {
        return new EventComparator(sortParameters);
    }

    enum SortParameter {
        NAME_ASCENDING, NAME_DESCENDING, DATE_ASCENDING, DATE_DESCENDING,
        NEXT_DUE_ASCENDING, NEXT_DUE_DESCENDING
    }

    private static class EventComparator implements Comparator<Event> {
        private SortParameter[] parameters;
        DateHandler dh = new DateHandler();
        Calendar cal = Calendar.getInstance();
        Date distantFuture, distantPast;

        private EventComparator(SortParameter[] parameters) {
            this.parameters = parameters;
            cal.setTime(dh.getTodayDate());

            // For events without a next due date, use an arbitrary date in the distant future
            // or past to put them at the end of the list
            cal.set(Calendar.YEAR, 5000);
            distantFuture = cal.getTime();

            cal.set(Calendar.YEAR, 0);
            distantPast = cal.getTime();
        }

        public int compare(Event e1, Event e2) {
            int comparison;
            Date proxyDate1, proxyDate2;

            for (SortParameter parameter : parameters) {
                switch (parameter) {
                    // For sorting events by name.
                    // These cannot be duplicated so there should be no order conflicts.
                    case NAME_ASCENDING:
                        comparison = e1.name.compareTo(e2.name);
                        if (comparison != 0) return comparison;
                        break;
                    case NAME_DESCENDING:
                        comparison = e2.name.compareTo(e1.name);
                        if (comparison != 0) return comparison;
                        break;
                    // For sorting events by last completed date.
                    // These may be duplicated but this case has not been handled yet.
                    case DATE_ASCENDING:
                        comparison = e1.date.compareTo(e2.date);
                        if (comparison != 0) return comparison;
                        break;
                    case DATE_DESCENDING:
                        comparison = e2.date.compareTo(e1.date);
                        if (comparison != 0) return comparison;
                        break;
                    /* For sorting events by next due date.

                       The main issue here is events that do not have a set repeat period
                       (and hence no valid next due date).

                       For these non-repeating events, we instead pretend (via the proxy) that they
                       are due in the very distant future (ascending) or past (descending) to force
                       them to the bottom of the list.

                       When comparing two non-repeating events, we sort them alphabetically by name
                       to make them intuitive to find.
                    */
                    case NEXT_DUE_ASCENDING:
                        if (e1.hasPeriod()) {
                            proxyDate1 = e1.nextDue;
                        } else {
                            proxyDate1 = distantFuture;
                        }

                        if (e2.hasPeriod()) {
                            proxyDate2 = e2.nextDue;
                        } else {
                            proxyDate2 = distantFuture;
                        }

                        // If both events do not repeat, order by name instead
                        if (!e1.hasPeriod() && !e2.hasPeriod()) {
                            comparison = e1.name.compareTo(e2.name);
                        } else {
                            // Compare via the proxy dates
                            comparison = proxyDate1.compareTo(proxyDate2);
                        }

                        if (comparison != 0) return comparison;
                        break;
                    case NEXT_DUE_DESCENDING:
                        if (e1.hasPeriod()) {
                            proxyDate1 = e1.nextDue;
                        } else {
                            proxyDate1 = distantPast;
                        }

                        if (e2.hasPeriod()) {
                            proxyDate2 = e2.nextDue;
                        } else {
                            proxyDate2 = distantPast;
                        }

                        // If both events do not repeat, order by name instead
                        if (!e1.hasPeriod() && !e2.hasPeriod()) {
                            comparison = e1.name.compareTo(e2.name);
                        } else {
                            // Compare via the proxy dates
                            comparison = proxyDate2.compareTo(proxyDate1);
                        }

                        if (comparison != 0) return comparison;
                        break;
                }
            }
            return 0;
        }
    }

}
