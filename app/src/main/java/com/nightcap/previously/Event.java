package com.nightcap.previously;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Class representing each event in the log.
 */

public class Event extends RealmObject {
    private String name;
    private Date date;
    private String notes;
    private int periodInDays;

    private Date nextDue;
    private boolean notifications;

    public Event() {

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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getPeriod() {
        return periodInDays;
    }

    public void setPeriod(int periodInDays) {
        this.periodInDays = periodInDays;
    }

    public boolean hasPeriod() {
        return !(this.getPeriod() == -1);
    }

    public Date getNextDue() {
        return nextDue;
    }

    public void setNextDue(Date nextDue) {
        this.nextDue = nextDue;
    }
}
