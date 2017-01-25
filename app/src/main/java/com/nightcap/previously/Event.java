package com.nightcap.previously;

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
    private String name;                            // Common
    private Date date;
    private int periodInDays;                       // Common
    private Date nextDue;                           // Common, dependent
    private String notes;

    private boolean notifications;                  // Common
    // Other ideas: Pinned events, private events   // Common

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
        return !(this.getPeriod() <= 0);
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

    static Comparator<Event> getComparator(SortParameter... sortParameters) {
        return new EventComparator(sortParameters);
    }

    enum SortParameter {
        NAME_ASCENDING, NAME_DESCENDING, DATE_ASCENDING, DATE_DESCENDING,
        NEXT_DUE_ASCENDING, NEXT_DUE_DESCENDING
    }

    private static class EventComparator implements Comparator<Event> {
        private SortParameter[] parameters;

        private EventComparator(SortParameter[] parameters) {
            this.parameters = parameters;
        }

        public int compare(Event e1, Event e2) {
            int comparison;

            for (SortParameter parameter : parameters) {
                switch (parameter) {
                    case NAME_ASCENDING:
                        comparison = e1.name.compareTo(e2.name);
                        if (comparison != 0) return comparison;
                        break;
                    case NAME_DESCENDING:
                        comparison = e2.name.compareTo(e1.name);
                        if (comparison != 0) return comparison;
                        break;
                    case DATE_ASCENDING:
                        comparison = e1.date.compareTo(e2.date);
                        if (comparison != 0) return comparison;
                        break;
                    case DATE_DESCENDING:
                        comparison = e2.date.compareTo(e1.date);
                        if (comparison != 0) return comparison;
                        break;
                    case NEXT_DUE_ASCENDING:
                        comparison = e1.nextDue.compareTo(e2.nextDue);
                        if (comparison != 0) return comparison;
                        break;
                    case NEXT_DUE_DESCENDING:
                        comparison = e2.nextDue.compareTo(e1.nextDue);
                        if (comparison != 0) return comparison;
                        break;
                }
            }
            return 0;
        }
    }

}
