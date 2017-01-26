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

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    String TAG = "HistoryAdapter";
    private ReceiveEventInterface eventListener;
    private List<Event> historyList;

    // ViewHolder pattern as required
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateView;

        ViewHolder(View view) {
            super(view);
            dateView = (TextView) view.findViewById(R.id.list_history_date);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Send date to calling Activity (when the list view row is clicked)
            eventListener.onReceiveEventFromAdapter(historyList.get(getAdapterPosition()));
        }
    }

    // Constructor
    HistoryAdapter(EventInfoActivity parent, List<Event> list) {
        this.historyList = list;
        eventListener = parent;
    }

    // Updating the list data
    void updateData(List<Event> list) {
        if (historyList != null) {
            historyList.clear();
            historyList.addAll(list);
        }
        else {
            historyList = list;
        }
        notifyDataSetChanged();
    }

    // To inflate the item layout and create the ViewHolder
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_history, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = historyList.get(position);

        // Build date string
        DateHandler dh = new DateHandler();
        holder.dateView.setText(dh.dateToString(event.getDate()));
    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        return historyList.size();
    }

}
