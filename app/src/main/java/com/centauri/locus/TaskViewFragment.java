package com.centauri.locus;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.centauri.locus.provider.Locus;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by mohitd2000 on 12/23/14.
 */
public class TaskViewFragment extends Fragment {

    private static final String TAG = TaskEditFragment.class.getSimpleName();
    private static final String[] PROJECTION = { Locus.Task._ID, Locus.Task.COLUMN_TITLE,
            Locus.Task.COLUMN_DESCRIPTION, Locus.Task.COLUMN_DUE };

    private Cursor taskCursor;
    private Uri taskUri;

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

    }

    /**
     * @see android.app.Fragment#onCreateView(android.view.LayoutInflater,
     *      android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_view, container, false);
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
            taskCursor.close();

            TextView dateTextView = (TextView) getActivity().findViewById(R.id.dateTextView);
            TextView timeTextView = (TextView) getActivity().findViewById(R.id.timeTextView);
            TextView locationTextView = (TextView) getActivity().findViewById(R.id.locationTextView);
            TextView descriptionTextView = (TextView) getActivity().findViewById(R.id.descriptionTextView);
            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

            dateTextView.setText(getDate(due));
            timeTextView.setText(getTime(due));
            descriptionTextView.setText(taskDescription);
            toolbar.setTitle(taskTitle);
        }
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
