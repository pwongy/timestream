package com.nightcap.previously;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Paul on 3/01/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    String TAG = "EVENT_ADAPTER";
    public List<Event> eventList;

    // ViewHolder pattern as required
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView, dateView;

        public ViewHolder(View view) {
            super(view);
            nameView = (TextView) view.findViewById(R.id.list_event_name);
            dateView = (TextView) view.findViewById(R.id.list_event_date);
        }
    }

    public EventAdapter(List<Event> el) {
        Log.i(TAG, "Constructor called");

        this.eventList = el;
        Log.i(TAG, "el: " + el.toString());
        Log.w(TAG, "eventlist: " + eventList.toString());
    }

    // To inflate the item layout and create the ViewHolder
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    // To populate data into the view
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.nameView.setText(event.getName());
        holder.dateView.setText(event.getDate());

        Log.d(TAG, "onBind called");
    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        Log.d(TAG, "Size: " + eventList.size());
        return eventList.size();

    }

}
