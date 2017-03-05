package com.nightcap.previously;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * Bottom sheet for quickly entering event notes.
 */

public class QuickNotesBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private FloatingActionButton fab;
    private EditText quickNotes;

    private BottomSheetBehavior.BottomSheetCallback bottomSheetBehaviorCallback
            = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            // This is for interacting with the sheet directly
            switch (newState) {
                case BottomSheetBehavior.STATE_DRAGGING:
                    Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_DRAGGING");
                    break;
                case BottomSheetBehavior.STATE_SETTLING:
                    Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_SETTLING");
                    break;
                case BottomSheetBehavior.STATE_EXPANDED:
                    Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_EXPANDED");
                    break;
                case BottomSheetBehavior.STATE_COLLAPSED:
                    Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_COLLAPSED");
                    break;
                case BottomSheetBehavior.STATE_HIDDEN:
                    Log.i("BottomSheetCallback", "BottomSheetBehavior.STATE_HIDDEN");
                    dismiss();
                    break;
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        // Inflate the view from XML
        View bottomSheetView = View.inflate(getContext(), R.layout.bottom_sheet_quick_notes, null);
        dialog.setContentView(bottomSheetView);

        // Get and set Behavior params
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) bottomSheetView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if(behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(bottomSheetBehaviorCallback);
        }

        // Get references to the views
        fab = (FloatingActionButton) getActivity().findViewById(R.id.event_info_fab);
        quickNotes = (EditText) bottomSheetView.findViewById(R.id.bottom_sheet_notes);
        ImageButton sendButton = (ImageButton) bottomSheetView.findViewById(R.id.bottom_sheet_send_button);

        // Detect when the dialog is actually shown on screen
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                // Show the soft keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(quickNotes, InputMethodManager.SHOW_IMPLICIT);

                // Set focus to the EditText
                quickNotes.requestFocus();
            }
        });

        // Configure save button
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quickNotes.getText().length() > 0) {
                    Toast.makeText(getDialog().getContext(), "Saving stuff", Toast.LENGTH_SHORT).show();
                    ReceiveNotesInterface parentActivity = (ReceiveNotesInterface) getActivity();
                    parentActivity.onReceiveNotesFromBottomSheet(quickNotes.getText().toString());

                    dialog.dismiss();
                } else {
                    Toast.makeText(getDialog().getContext(), "Empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // Show the FAB when the BottomSheet is dismissed
        if (!fab.isShown()) {
            fab.show();
        }
    }

}