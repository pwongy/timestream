package com.nightcap.previously;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for sending database events to RecyclerView.
 */

class EventLogAdapter extends RecyclerView.Adapter<EventLogAdapter.ViewHolder> {
    String TAG = "EventLogAdapter";
    private Context context;
    private ReceiveEventInterface eventListener;
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
                // Tick is clicked, so send the event to MainActivity
                eventListener.onReceiveEventFromAdapter(eventList.get(getAdapterPosition()));
            } else {
                // Intent to show event info
                Intent info = new Intent(view.getContext(), EventInfoActivity.class);
                info.putExtra("event_id", eventList.get(getAdapterPosition()).getId());
                view.getContext().getApplicationContext().startActivity(info);
            }
        }

    }

    // Constructor
    EventLogAdapter(MainActivity parent, List<Event> list) {
        this.eventList = list;
        eventListener = parent;
        context = parent.getApplicationContext();
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
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DateHandler dh = new DateHandler();
        Event event = eventList.get(position);

        // Set name
        holder.nameView.setText(event.getName());

        long relativeDays = dh.getDaysBetween(event.getNextDue(), dh.getTodayDate());

        // Mark overdue events
        if (event.hasPeriod()) {
            if (relativeDays <= 7) {
                holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorWarning));
            }
            if (relativeDays <= 0) {
                holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorOverdue));
            }
            // XXX: Recycled views have color applied [BUG]
        }

        // Build date string
        StringBuilder sb = new StringBuilder(dh.dateToString(event.getDate()));
        if (event.hasPeriod()) {
            sb.append("  ➡  ");
            sb.append(dh.dateToString(event.getNextDue()));
        }

        // Set date
        holder.dateView.setText(sb.toString());
    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        return eventList.size();
    }

}
