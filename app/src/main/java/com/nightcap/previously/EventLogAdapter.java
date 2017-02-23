package com.nightcap.previously;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import java.util.List;

/**
 * Adapter for sending database events to RecyclerView.
 */

class EventLogAdapter extends RecyclerView.Adapter<EventLogAdapter.ViewHolder> {
    String TAG = "EventLogAdapter";
    private Context context;
    private ReceiveEventInterface eventListener;
    private List<Event> eventList;
    private DateHandler dh = new DateHandler();

    // ViewHolder pattern as required
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameView, previousDateView, nextDateView;
        RoundCornerProgressBar progressBar;
        ImageButton imageButton;
//        http://stackoverflow.com/questions/30284067/handle-button-click-inside-a-row-in-recyclerview

        ViewHolder(View view) {
            super(view);

            // Get relevant views
            nameView = (TextView) view.findViewById(R.id.list_event_name);
            previousDateView = (TextView) view.findViewById(R.id.list_event_previous_date);
            nextDateView = (TextView) view.findViewById(R.id.list_event_next_date);
            progressBar = (RoundCornerProgressBar) view.findViewById(R.id.list_item_progress);
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
                context.startActivity(info);
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

//        for (Event e : list) {
//            if (e.hasPeriod()) {
//                View v = rv.getLayoutManager().findViewByPosition(list.indexOf(e)).getRootView();
//                TextView nameView = (TextView) v.findViewById(R.id.list_event_name);
//                long relativeDays = dh.getDaysBetween(e.getNextDue(), dh.getTodayDate());
//
//                if (relativeDays <= 7) {
//                    nameView.setTextColor(ContextCompat.getColor(context, R.color.colorWarning));
//                }
//                if (relativeDays <= 0) {
//                    nameView.setTextColor(ContextCompat.getColor(context, R.color.colorOverdue));
//                }
//            }
//        }
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
        Event event = eventList.get(position);

        // Get preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int warningPeriod = Integer.parseInt(prefs.getString("warning_period", "7"));

        // Set name and previous date (always available)
        holder.nameView.setText(event.getName());

        // Calculate relative days
        long relativeDaysPrevious = dh.getDaysBetween(event.getDate(), dh.getTodayDate());
        long relativeDaysNext = dh.getDaysBetween(event.getNextDue(), dh.getTodayDate());

        // Force resets
        holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorText));
        holder.progressBar.setVisibility(View.GONE);
        holder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.colorDateText));
        holder.progressBar.setProgress(0);
        holder.progressBar.setMax(100);

        holder.previousDateView.setText(dh.dateToString(event.getDate())
                + "\n(" + dh.getRelativeDaysString(relativeDaysPrevious) + ")");

        // These fields depend on whether an event is set to repeat
        if (event.hasPeriod()) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.nextDateView.setVisibility(View.VISIBLE);

            // Highlight upcoming and overdue events
            if (relativeDaysNext <= warningPeriod) {
                holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorWarning));
                holder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.colorWarning));
            }
            if (relativeDaysNext <= 0) {
                holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorOverdue));
                holder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.colorOverdue));
            }

            // Set progress bar
            holder.progressBar.setMax((float) event.getPeriod());
            holder.progressBar.setProgress(-relativeDaysPrevious);

            // Add next due date
            holder.nextDateView.setText(dh.dateToString(event.getNextDue())
                    + "\n(" + dh.getRelativeDaysString(relativeDaysNext) + ")");
        } else {
            holder.nextDateView.setVisibility(View.GONE);
            holder.nextDateView.setText(null);
        }

    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        return eventList.size();
    }

}
