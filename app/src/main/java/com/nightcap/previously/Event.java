package com.nightcap.previously;

import io.realm.RealmObject;

/**
 * Created by Paul on 7/11/2016.
 */

public class Event extends RealmObject {
    private String type;
    private String date;
    private String notes;

    public Event() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
