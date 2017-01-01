package com.nightcap.previously;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by Paul on 7/11/2016.
 */

public class EventInstance extends RealmObject {
    private long id;
    private Event type;
    private Date date;

    public EventInstance() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Event getType() {
        return type;
    }

    public void setType(Event type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
