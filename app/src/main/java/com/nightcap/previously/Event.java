package com.nightcap.previously;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Paul on 7/11/2016.
 */

public class Event extends RealmObject {
    private String name;
    private String date;    // To be moved to instance
    private String notes;   // To be moved to instance

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
