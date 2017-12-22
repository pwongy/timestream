package com.nightcap.previously;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
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

    static final String FLAG_MARK_DONE_PRIMARY = "event_done_primary";
    static final String FLAG_MARK_DONE_SECONDARY = "event_done_secondary";
    static final String FLAG_SHOW_EVENT_INFO = "event_info";

    // ViewHolder pattern as required
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView nameView, categoryView, previousDateView, nextDateView;
        RoundCornerProgressBar progressBar;
        ImageButton imageButton;
//        http://stackoverflow.com/questions/30284067/handle-button-click-inside-a-row-in-recyclerview

        ViewHolder(View view) {
            super(view);

            // Get relevant views
            nameView = view.findViewById(R.id.list_event_name);
            categoryView = view.findViewById(R.id.list_event_category);
            previousDateView = view.findViewById(R.id.list_event_previous_date);
            nextDateView = view.findViewById(R.id.list_event_next_date);
            progressBar = view.findViewById(R.id.list_item_progress);
            imageButton = view.findViewById(R.id.list_item_image_button);

            // Set listeners
            view.setOnClickListener(this);
            imageButton.setOnClickListener(this);
            imageButton.setOnLongClickListener(this);

            // Set text size
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.text_size_event_name));
            categoryView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.text_size_event_category));
            previousDateView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.text_size_event_dates));
            nextDateView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.text_size_event_dates));
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == imageButton.getId()) {
                // Tick image button was clicked, so get MainActivity to mark it as done
                eventListener.onReceiveEventFromAdapter(eventList.get(getAdapterPosition()), FLAG_MARK_DONE_PRIMARY);
            } else {
                // The list item body was clicked, so get MainActivity to show event info
                eventListener.onReceiveEventFromAdapter(eventList.get(getAdapterPosition()), FLAG_SHOW_EVENT_INFO);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == imageButton.getId()) {
                // Tick image button was long clicked, so get MainActivity to mark it as done
                eventListener.onReceiveEventFromAdapter(eventList.get(getAdapterPosition()), FLAG_MARK_DONE_SECONDARY);
            }
            return true;
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
        Event event = eventList.get(position);

        // Get preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int warningPeriod = Integer.parseInt(prefs.getString("warning_period", "7"));

        // Calculate relative days
        long relativeDaysPrevious = dh.getDaysBetween(event.getDate(), dh.getTodayDate());
        long relativeDaysNext = dh.getDaysBetween(event.getNextDue(), dh.getTodayDate());

        // Set name and previous date (always available)
        holder.nameView.setText(event.getName());
        holder.categoryView.setText(event.getCategory());
        holder.previousDateView.setText(dh.dateToString(event.getDate())
                + "\n(" + dh.getRelativeDaysString(relativeDaysPrevious) + ")");

        // Force reset some view formatting
        holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorText));
        holder.progressBar.setVisibility(View.GONE);
        holder.progressBar.setMax(100);
        holder.progressBar.setProgress(0);
        holder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.colorDateText));
        holder.progressBar.setSecondaryProgress(0);
        holder.progressBar.setSecondaryProgressColor(ContextCompat.getColor(context, R.color.colorOverdue));

        // These fields depend on whether an event is repeating
        if (event.hasPeriod()) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.nextDateView.setVisibility(View.VISIBLE);

            // Set progress bar
            holder.progressBar.setMax(event.getPeriod());
            holder.progressBar.setProgress(-relativeDaysPrevious);

            // Highlight upcoming and overdue events
            if (relativeDaysNext <= warningPeriod) {
                holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorWarning));
                holder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.colorWarning));
            }
            if (relativeDaysNext <= 0) {
                holder.nameView.setTextColor(ContextCompat.getColor(context, R.color.colorDue));
//                holder.progressBar.setMax(-relativeDaysPrevious);
                holder.progressBar.setProgress(event.getPeriod());
                holder.progressBar.setProgressColor(ContextCompat.getColor(context, R.color.colorDue));
//                holder.progressBar.setSecondaryProgress(-relativeDaysPrevious);
            }

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
