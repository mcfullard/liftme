package fnm.wrmc.nmmu.liftme;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;

import java.sql.Timestamp;
import java.util.Calendar;

import fnm.wrmc.nmmu.liftme.Data_Objects.Trip;

/**
 * Created by minnaar on 2016/05/12.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public interface DatePickedListener {
        public void onDatePicked(Timestamp ts, View v);
    }

    private DatePickedListener datePickedListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            datePickedListener = (DatePickedListener) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        datePickedListener.onDatePicked(Trip.getTimestamp(0, 0, day, month, year), view);
    }
}