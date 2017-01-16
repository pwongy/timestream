package com.nightcap.previously;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for sending database events to RecyclerView.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    String TAG = "EventAdapter";
    private List<Event> eventList;

    // ViewHolder pattern as required
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView, dateView;

        public ViewHolder(View view) {
            super(view);
            nameView = (TextView) view.findViewById(R.id.list_event_name);
            dateView = (TextView) view.findViewById(R.id.list_event_date);
        }
    }

    // Constructor
    public EventAdapter(List<Event> list) {
        this.eventList = list;
    }

    // Updating the list data
    public void updateData(List<Event> list) {
        if (eventList != null) {
            eventList.clear();
            eventList.addAll(list);
        }
        else {
            eventList = list;
        }
        notifyDataSetChanged();
    }

    // To inflate the item layout and create the ViewHolder
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.nameView.setText(event.getName());
        holder.dateView.setText(new DateHandler().dateToString(event.getDate()));
    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        return eventList.size();
    }

}
