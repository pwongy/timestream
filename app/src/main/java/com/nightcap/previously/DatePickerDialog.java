package com.nightcap.previously;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Dialog for selecting event date.
 */

public class DatePickerDialog extends DialogFragment implements android.app.DatePickerDialog.OnDateSetListener {
    private DateInterface dateListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new android.app.DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DateInterface) {
            dateListener = (DateInterface) context;
        }
    }

    @Override
    public void onDetach() {
        dateListener = null;
        super.onDetach();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        FragmentManager fragMan = getActivity().getSupportFragmentManager();

        if(fragMan.findFragmentByTag("datePickerEdit") != null) {
//            Log.d("DIALOG", "Edit Activity");

            // Update EditText in EditActivity
            EditText dateField = (EditText) getActivity().findViewById(R.id.event_date);
            dateField.setText(new DateHandler().dateToString(year, month, day));
        }

        if(fragMan.findFragmentByTag("datePickerDone") != null) {
//            Log.d("DIALOG", "Marking done");

            // Send date to calling Activity
            dateListener.onReceiveDateFromDialog(new DateHandler().dateFromComponents(year, month, day));
        }



    }

}
