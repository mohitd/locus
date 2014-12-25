package com.centauri.locus;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.centauri.locus.provider.Locus;
import com.melnykov.fab.FloatingActionButton;

/**
 * Created by mohitd2000 on 12/23/14.
 */
public class TaskViewActivity extends ActionBarActivity {

    private static final String[] PROJECTION = { Locus.Task._ID, Locus.Task.COLUMN_TITLE,
            Locus.Task.COLUMN_DESCRIPTION, Locus.Task.COLUMN_DUE };

    /**
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_view);

        TaskViewFragment fragment = new TaskViewFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.task_view_fragment, fragment)
                .commit();

        // If the FAB was pressed, edit the task
        findViewById(R.id.fab_mini).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TaskViewActivity.this, TaskEditActivity.class)
                        .putExtras(getIntent().getExtras()));
            }
        });

        // Change the name of the toolbar to match the name of the task
        long taskId = getIntent().getExtras().getLong(MainActivity.KEY_TASK_ID);
        Uri taskUri = ContentUris.withAppendedId(Locus.Task.CONTENT_URI, taskId);
        Cursor taskCursor = getContentResolver().query(taskUri, PROJECTION, null, null,
                null);
        String title = "Locus";
        if (taskCursor.getCount() >= 1) {
            taskCursor.moveToFirst();
            title = taskCursor.getString(taskCursor.getColumnIndexOrThrow(Locus.Task.COLUMN_TITLE));
            taskCursor.close();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
