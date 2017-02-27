package com.nightcap.previously;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter for sending database events to RecyclerView.
 */

class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    String TAG = "HistoryAdapter";
    private Context context;
    private ReceiveEventInterface eventListener;

    private List<Event> historyList;
    private int expandedPosition = 0;

    private RecyclerView recyclerView;
    private int recyclerViewHeight = 0;
    private boolean isResized = false;

    // ViewHolder pattern as required
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView dateView, notesView;
        ImageView notesIndicator;
        LinearLayout expandArea;

        ViewHolder(View view) {
            super(view);
            dateView = (TextView) view.findViewById(R.id.list_history_date);
            notesIndicator = (ImageView) view.findViewById(R.id.history_notes_indicator);
            expandArea = (LinearLayout) view.findViewById(R.id.history_expand_area);
            notesView = (TextView) view.findViewById(R.id.history_notes);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Send date to calling Activity (when the list view row is clicked)
            eventListener.onReceiveEventFromAdapter(historyList.get(getAdapterPosition()), "");

            // Check for an expanded view, collapse if you find one
            if (expandedPosition >= 0) {
                int prev = expandedPosition;
                notifyItemChanged(prev);
            }

            // Set the current position to "expanded"
            expandedPosition = getLayoutPosition();
            notifyItemChanged(expandedPosition);
        }
    }

    // Constructor
    HistoryAdapter(EventInfoActivity parent, List<Event> list) {
        this.eventListener = parent;
        this.historyList = list;
        context = parent.getApplicationContext();
        this.recyclerView = parent.historyRecyclerView;
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
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Event event = historyList.get(position);

        // Build and set date string
        DateHandler dh = new DateHandler();
        holder.dateView.setText(dh.dateToString(event.getDate()));

        // Force reset some view formatting
        holder.dateView.setTypeface(null, Typeface.NORMAL);
        holder.dateView.setTextColor(ContextCompat.getColor(context, R.color.colorDateText));
        holder.notesIndicator.setVisibility(View.GONE);
        holder.notesView.setTypeface(null, Typeface.NORMAL);

        if (!event.getNotes().equalsIgnoreCase("")) {
            holder.notesIndicator.setVisibility(View.VISIBLE);
        }

        // Handle click expansion
        if (position == expandedPosition) { // Initially 0
            holder.expandArea.setVisibility(View.VISIBLE);

            // Update highlighted view
            holder.dateView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            holder.dateView.setTypeface(holder.dateView.getTypeface(), Typeface.BOLD);

            if (event.getNotes().equalsIgnoreCase("")) {
                holder.notesView.setText(context.getString(R.string.event_notes_blank));
                holder.notesView.setTypeface(holder.notesView.getTypeface(), Typeface.ITALIC);
            } else {
                holder.notesView.setText(event.getNotes());
            }

        } else {
            holder.expandArea.setVisibility(View.GONE);
        }

        // Set RecyclerView height
        ViewTreeObserver vto = recyclerView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Set initial max height to that of screen
                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(metrics);
                int maxHeight = metrics.heightPixels * 100; // Long lists will exceed the screen height, so make it arbitrarily large

                // Total height of first few rows
                int rowsToShow = 5;
                if (!isResized && holder.getAdapterPosition() < rowsToShow) {
                    recyclerViewHeight += holder.itemView.getHeight();
                }

                // Get original height of recyclerView
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int rvHeight = recyclerView.getMeasuredHeight();

                // Cap the recyclerView's height
                // Should equal the height of the first 5 rows (including expanded row 1)
                if (holder.getAdapterPosition() == (rowsToShow-1)) {
                    maxHeight = recyclerViewHeight;
                }

                // Resize the view (just once)
                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                if (!isResized && rvHeight > maxHeight) {
                    params.height = maxHeight;
                    recyclerView.requestLayout();
                    isResized = true;
                }
            }
        });

    }

    // To determine the number of items
    @Override
    public int getItemCount() {
        return historyList.size();
    }

}
