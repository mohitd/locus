/**
 * 
 */
package com.centauri.locus;

import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.centauri.locus.provider.Locus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author mohitd2000
 * 
 */
public class TaskEditFragment extends Fragment implements OnClickListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener {
    private static final String TAG = TaskEditFragment.class.getSimpleName();
    private static final String[] PROJECTION = { Locus.Task._ID, Locus.Task.COLUMN_TITLE,
        Locus.Task.COLUMN_LATITUDE, Locus.Task.COLUMN_LONGITUDE,
        Locus.Task.COLUMN_DESCRIPTION, Locus.Task.COLUMN_DUE };

    private Cursor taskCursor;
    private Uri taskUri;

    private EditText titleEditText;
    private EditText descEditText;
    private TextView dateTextView;
    private TextView timeTextView;

    private int hour = 0, minute = 0, year = 0, month = 0, day = 0;

    /**
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long taskId = getArguments().getLong(MainActivity.KEY_TASK_ID);
            taskUri = ContentUris.withAppendedId(Locus.Task.CONTENT_URI, taskId);
            taskCursor = getActivity().getContentResolver().query(taskUri, PROJECTION, null, null,
                    null);
        }
        setHasOptionsMenu(true);

    }

    /**
     * @see android.app.Fragment#onCreateOptionsMenu(android.view.Menu,
     *      android.view.MenuInflater)
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_task, menu);
    }

    /**
     * @see android.app.Fragment#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_save:
            saveData();
            getActivity().finish();
            break;

        case R.id.menu_check:
            saveData();
            ContentValues values = new ContentValues();
            values.put(Locus.Task.COLUMN_COMPLETED, 1);
            getActivity().getContentResolver().update(taskUri, values, null, null);
            getActivity().finish();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     *      android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_edit, container, false);

        dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        timeTextView = (TextView) view.findViewById(R.id.timeTextView);
        titleEditText = (EditText) view.findViewById(R.id.titleEditText);
        descEditText = (EditText) view.findViewById(R.id.descriptionEditText);

        dateTextView.setOnClickListener(this);
        timeTextView.setOnClickListener(this);

        return view;
    }

    /**
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (taskCursor.getCount() > 0) {
            taskCursor.moveToFirst();
            String taskTitle = taskCursor.getString(taskCursor
                    .getColumnIndexOrThrow(Locus.Task.COLUMN_TITLE));
            String taskDescription = taskCursor.getString(taskCursor
                    .getColumnIndexOrThrow(Locus.Task.COLUMN_DESCRIPTION));
            long due = taskCursor.getLong(taskCursor.getColumnIndexOrThrow(Locus.Task.COLUMN_DUE));
            long lat = taskCursor.getLong(taskCursor.getColumnIndexOrThrow(Locus.Task.COLUMN_LATITUDE));
            long lon = taskCursor.getLong(taskCursor.getColumnIndexOrThrow(Locus.Task.COLUMN_LONGITUDE));
            taskCursor.close();

            String locationText = "No location.";
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = new ArrayList<Address>(0);
            try {
                addresses = geocoder.getFromLocation(lat, lon, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                locationText = String.format(
                        "%s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(1) : "");
            }

            EditText titleEditText = (EditText) getActivity().findViewById(R.id.titleEditText);
            EditText descEditText = (EditText) getActivity().findViewById(R.id.descriptionEditText);
            TextView locationTextView = (TextView) getActivity().findViewById(R.id.locationTextView);

            timeTextView.setText(getTime(due));
            dateTextView.setText(getDate(due));
            locationTextView.setText(locationText);

            titleEditText.setText(taskTitle);
            descEditText.setText(taskDescription);
        }

        // So apparently you can only use getSupportActionBar() immediately after setSupportActionBar()???
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle("Edit Task");
        ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {


        if (view.getId() == R.id.dateTextView) {
            DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), this, this.year, this.month, this.day);
            dateDialog.show();
        } else if (view.getId() == R.id.timeTextView) {
            TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), this, this.hour, this.minute, false);
            timeDialog.show();
        } else {
            dateTextView.setText("Set date");
            timeTextView.setText("Off");
        }
    }

    /**
     * @see android.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }


    private void saveData() {
        String title = titleEditText.getText().toString();
        String desc = descEditText.getText().toString();

        TimeZone tz = TimeZone.getDefault();
        Calendar cal = new GregorianCalendar(tz);
        cal.set(year, month, day, hour, minute);

        ContentValues values = new ContentValues();
        values.put(Locus.Task.COLUMN_TITLE, title);
        values.put(Locus.Task.COLUMN_DESCRIPTION, desc);
        values.put(Locus.Task.COLUMN_DUE, cal.getTimeInMillis());

        getActivity().getContentResolver().update(taskUri, values, null, null);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear;
        this.day = dayOfMonth;

        TimeZone tz = TimeZone.getDefault();
        Calendar cal = new GregorianCalendar(tz);
        cal.set(year, monthOfYear, dayOfMonth);

        String day = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
        String month = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());

        dateTextView.setText(day + ", " + month + " " + dayOfMonth + ", " + year);
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        this.hour = hourOfDay;
        this.minute = minute;

        String AMPM = hourOfDay <= 12 ? "AM" : "PM";
        int hour = hourOfDay <= 12 ? hourOfDay : hourOfDay - 12;

        timeTextView.setText(hour + ":" + String.format("%02d", minute) + " " + AMPM);
    }

    private String getDate(long millis) {
        StringBuilder builder = new StringBuilder();
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(millis);
        builder.append(getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)) + ", ");
        builder.append(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " ");
        builder.append(cal.get(Calendar.DAY_OF_WEEK) + ", ");
        builder.append(cal.get(Calendar.YEAR));
        return builder.toString();
    }

    private String getTime(long millis) {
        StringBuilder builder = new StringBuilder();
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(millis);
        builder.append(cal.get(Calendar.HOUR) + ":");
        if (cal.get(Calendar.MINUTE) == 0) builder.append("00 ");
        else builder.append(cal.get(Calendar.MINUTE) + " ");

        if (cal.get(Calendar.AM_PM) == 1) builder.append("AM");
        else builder.append("PM");
        return builder.toString();
    }

    private String getDayOfWeek(int constant) {
        switch (constant) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "";
        }
    }
}
