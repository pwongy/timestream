package com.nightcap.previously;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Adapter for sending database events to RecyclerView.
 */

class EventLogAdapter extends RecyclerView.Adapter<EventLogAdapter.ViewHolder> {
    String TAG = "EventLogAdapter";
    private List<Event> eventList;

    // ViewHolder pattern as required
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameView, dateView;
        ImageButton imageButton;
//        http://stackoverflow.com/questions/30284067/handle-button-click-inside-a-row-in-recyclerview

        ViewHolder(View view) {
            super(view);

            // Get relevant views
            nameView = (TextView) view.findViewById(R.id.list_event_name);
            dateView = (TextView) view.findViewById(R.id.list_event_date);
            imageButton = (ImageButton) view.findViewById(R.id.list_item_image_button);

            // Set listeners
            view.setOnClickListener(this);
            imageButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == imageButton.getId()) {
                String event = eventList.get(getAdapterPosition()).getName();
                Toast.makeText(view.getContext(), "Tick pressed: " + event, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(view.getContext(), "Row pressed: " + getAdapterPosition(), Toast.LENGTH_SHORT).show();

                // Intent to show event info
                Intent info = new Intent(view.getContext(), EventInfoActivity.class);
                info.putExtra("event_id", eventList.get(getAdapterPosition()).getId());
                view.getContext().getApplicationContext().startActivity(info);
            }
        }

    }

    // Constructor
    EventLogAdapter(List<Event> list) {
        this.eventList = list;
    }

    // Updating the list data
    void updateData(List<Event> list) {
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
    public EventLogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

        // Build date string
        DateHandler dh = new DateHandler();
        StringBuilder sb = new StringBuilder(dh.dateToString(event.getDate()));
        if (event.hasPeriod()) {
            sb.append("  ➡  ");
            sb.append(dh.dateToString(event.getNextDue()));
        }
        holder.dateView.setText(sb.toString());
    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        return eventList.size();
    }

}
