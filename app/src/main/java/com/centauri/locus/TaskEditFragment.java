/**
 * 
 */
package com.centauri.locus;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.centauri.locus.provider.Locus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author mohitd2000
 * 
 */
public class TaskEditFragment extends Fragment implements OnClickListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private static final String TAG = TaskEditFragment.class.getSimpleName();
    private static final String[] PROJECTION = { Locus.Task._ID, Locus.Task.COLUMN_TITLE,
        Locus.Task.COLUMN_LATITUDE, Locus.Task.COLUMN_LONGITUDE,
        Locus.Task.COLUMN_DESCRIPTION, Locus.Task.COLUMN_TRANSITION, Locus.Task.COLUMN_DUE };

    public static final int REQUEST_GEOFENCE = 1;

    private Cursor taskCursor;
    private Uri taskUri;

    private EditText titleEditText;
    private EditText descEditText;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView locationTextView;
    private Spinner transitionSpinner;

    private int hourOfDay = 0, minute = 0, year = 0, month = 0, day = 0;

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

        Calendar cal = Calendar.getInstance();
        hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
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
        case android.R.id.home:
        case R.id.menu_save:
            saveData();
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
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
        locationTextView = (TextView) view.findViewById(R.id.locationTextView);
        titleEditText = (EditText) view.findViewById(R.id.titleEditText);
        descEditText = (EditText) view.findViewById(R.id.descriptionEditText);
        transitionSpinner = (Spinner) view.findViewById(R.id.transitionSpinner);

        dateTextView.setOnClickListener(this);
        timeTextView.setOnClickListener(this);
        locationTextView.setOnClickListener(this);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.transition_entries, android.R.layout.simple_spinner_dropdown_item);
        transitionSpinner.setAdapter(spinnerAdapter);

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
            int transition = taskCursor.getInt(taskCursor.getColumnIndexOrThrow(Locus.Task.COLUMN_TRANSITION));
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
                locationText = String.format("%s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(1) : "");
            }

            timeTextView.setText(getTime(due));
            dateTextView.setText(getDate(due));
            locationTextView.setText(locationText);

            titleEditText.setText(taskTitle);
            descEditText.setText(taskDescription);

            transitionSpinner.setSelection(transition);
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
            TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), this, this.hourOfDay, this.minute, false);
            timeDialog.show();
        } else if (view.getId() == R.id.locationTextView) {
            saveData();
            Intent intent = new Intent(getActivity(), GeofenceSelectorActivity.class);
            intent.putExtras(getArguments());
            startActivityForResult(intent, REQUEST_GEOFENCE);
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
        int transition = transitionSpinner.getSelectedItemPosition();

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(year, month, day, hourOfDay, minute);

        ContentValues values = new ContentValues();
        values.put(Locus.Task.COLUMN_TITLE, title);
        values.put(Locus.Task.COLUMN_DESCRIPTION, desc);
        values.put(Locus.Task.COLUMN_DUE, cal.getTimeInMillis());
        values.put(Locus.Task.COLUMN_TRANSITION, transition);

        getActivity().getContentResolver().update(taskUri, values, null, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GEOFENCE && resultCode == Activity.RESULT_OK) {

        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        this.year = year;
        this.month = monthOfYear;
        this.day = dayOfMonth;

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(year, monthOfYear, dayOfMonth);
        dateTextView.setText(getDate(cal.getTimeInMillis()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(this.year, this.month, this.day, hourOfDay, minute);
        timeTextView.setText(getTime(cal.getTimeInMillis()));
    }

    private String getDate(long millis) {
        StringBuilder builder = new StringBuilder();
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(millis);
        builder.append(getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK))).append(", ");
        builder.append(cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())).append(" ");
        builder.append(cal.get(Calendar.DAY_OF_MONTH)).append(", ");
        builder.append(cal.get(Calendar.YEAR));
        return builder.toString();
    }

    private String getTime(long millis) {
        StringBuilder builder = new StringBuilder();
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(millis);
        builder.append(cal.get(Calendar.HOUR)).append(":");

        if (cal.get(Calendar.MINUTE) == 0) builder.append("00 ");
        else builder.append(cal.get(Calendar.MINUTE)).append(" ");

        if (cal.get(Calendar.AM_PM) == 0) builder.append("AM");
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
