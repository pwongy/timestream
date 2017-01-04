package com.nightcap.previously;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 3/01/2017.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
    private List<Event> eventList = new ArrayList<>();

    // ViewHolder pattern as required
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, date;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.list_event_name);
            date = (TextView) view.findViewById(R.id.list_event_date);
        }
    }

    public EventAdapter(List<Event> el) {
        this.eventList = el;
    }

    // To inflate the item layout and create the ViewHolder
    @Override
    public EventAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_row, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    // To populate data into the view
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Log.d("Adapter", "onBind called");
        Event event = eventList.get(position);
        holder.name.setText(event.getName());
        holder.date.setText(event.getDate());

        Log.d("Adapter", "onBind called");
    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
