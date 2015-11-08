/**
 * 
 */
package com.centauri.locus;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.centauri.locus.adapter.TaskAdapter;
import com.centauri.locus.geofence.GeofenceRemover;
import com.centauri.locus.provider.Locus;
import com.centauri.locus.util.BitmapCache;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mohitd2000
 * 
 */
public class TaskListFragment extends ListFragment implements AbsListView.MultiChoiceModeListener,
        OnItemLongClickListener, View.OnClickListener {

    private static final String TAG = TaskListFragment.class.getSimpleName();

    private static final String[] PROJECTION = { Locus.Task._ID, Locus.Task.COLUMN_TITLE,
        Locus.Task.COLUMN_DESCRIPTION, Locus.Task.COLUMN_LATITUDE, Locus.Task.COLUMN_LONGITUDE,
        Locus.Task.COLUMN_COMPLETED };

    public interface OnListItemClickedCallback {
        public void onListItemClicked(ListView listView, View view, int position, long id);
    }

    private OnListItemClickedCallback callbacks;
    private TaskAdapter adapter;

    private List<Long> ids;
    private GeofenceRemover geofenceRemover;
    private ActionMode actionMode;

    /**
     * @see android.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Cursor cursor = getActivity().getContentResolver()
                .query(Locus.Task.CONTENT_URI, PROJECTION, null, null, null);

        adapter = new TaskAdapter(getActivity(), cursor, 0);
        geofenceRemover = new GeofenceRemover(getActivity());
        setListAdapter(adapter);
    }

    /**
     * @see android.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(this);
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageResource(R.drawable.listview_empty_view);
        getListView().setEmptyView(imageView);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(this);
        getListView().setOnItemLongClickListener(this);
        getListView().setItemsCanFocus(false);
    }

    /**
     * @see android.app.Fragment#onStart()
     */
    @Override
    public void onStart() {
        super.onStart();
        Cursor cursor;
        if (getArguments().getInt(MainActivity.KEY_COMPLETED) == 1) {
            Log.i(TAG, "Completed tasks");
            cursor = getActivity().getContentResolver().query(Locus.Task.CONTENT_URI, PROJECTION,
                    Locus.Task.COLUMN_COMPLETED + "=1", null, null);
        } else {
            Log.i(TAG, "Not Completed tasks");
            cursor = getActivity().getContentResolver().query(Locus.Task.CONTENT_URI, PROJECTION,
                    Locus.Task.COLUMN_COMPLETED + "=0", null, null);
        }
        adapter.changeCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter = null;
        ids = null;
        geofenceRemover = null;
    }

    /**
     * @see android.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callbacks = (OnListItemClickedCallback) activity;
        } catch (ClassCastException e) {
            // TODO: handle exception
        }
    }

    /**
     * @see android.app.ListFragment#onListItemClick(android.widget.ListView,
     *      android.view.View, int, long)
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (callbacks != null) {
            callbacks.onListItemClicked(l, v, position, id);
        }
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(getActivity(), TaskEditActivity.class));
    }

    /**
     * @see android.view.ActionMode.Callback#onCreateActionMode(android.view.ActionMode,
     *      android.view.Menu)
     */
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.list_view_cab, menu);
        ids = new ArrayList<Long>();
        this.actionMode = actionMode;
        return true;
    }

    /**
     * @see android.view.ActionMode.Callback#onPrepareActionMode(android.view.ActionMode,
     *      android.view.Menu)
     */
    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        final int checked = getListView().getCheckedItemCount();
        CharSequence text = color(getResources().getColor(R.color.primary),
                getString(R.string.cab_title, checked));
        actionMode.setTitle(text);
        return true;
    }

    /**
     * @see android.view.ActionMode.Callback#onActionItemClicked(android.view.ActionMode,
     *      android.view.MenuItem)
     */
    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.menu_delete:
            List<String> geofenceIds = new ArrayList<String>();
            BitmapCache cache = BitmapCache.getInstance();
            for (Long id : ids) {
                Uri uri = ContentUris.withAppendedId(Locus.Task.CONTENT_URI, id);
                getActivity().getContentResolver().delete(uri, null, null);
                cache.deleteBitmapFromCache("ts" + id);
                geofenceIds.add(String.valueOf(id));
            }
            geofenceRemover.removeGeofencesById(geofenceIds);
            Log.i(TAG, "Removed geofences: " + geofenceIds.toString());

            Cursor cursor;
            if (getArguments().getInt(MainActivity.KEY_COMPLETED) == 1) {
                Log.i(TAG, "Completed tasks");
                cursor = getActivity().getContentResolver().query(Locus.Task.CONTENT_URI, PROJECTION,
                        Locus.Task.COLUMN_COMPLETED + "=1", null, null);
            } else {
                Log.i(TAG, "Not Completed tasks");
                cursor = getActivity().getContentResolver().query(Locus.Task.CONTENT_URI, PROJECTION,
                        Locus.Task.COLUMN_COMPLETED + "=0", null, null);
            }
            adapter.changeCursor(cursor);
            adapter.notifyDataSetChanged();
            ids.clear();
            actionMode.finish();
            return true;

        default:
            return false;
        }
    }

    /**
     * @see android.view.ActionMode.Callback#onDestroyActionMode(android.view.ActionMode)
     */
    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        actionMode = null;
    }


    /**
     * @see android.widget.AbsListView.MultiChoiceModeListener#onItemCheckedStateChanged(android.view.ActionMode,
     *      int, long, boolean)
     */
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id,
            boolean checked) {
        actionMode.invalidate();
        if (checked) {
            ids.add(id);
        } else {
            ids.remove(id);
        }
    }

    /**
     * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView,
     *      android.view.View, int, long)
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (actionMode != null) {
            return false;
        }

        getListView().setItemChecked(position, true);
        return true;
    }

    /**
     * Returns a CharSequence that concatenates the specified array of CharSequence
     * objects and then applies a list of zero or more tags to the entire range.
     *
     * @param content an array of character sequences to apply a style to
     * @param tags the styled span objects to apply to the content
     *        such as android.text.style.StyleSpan
     *
     */
    private static CharSequence apply(CharSequence[] content, Object... tags) {
        SpannableStringBuilder text = new SpannableStringBuilder();
        openTags(text, tags);
        for (CharSequence item : content) {
            text.append(item);
        }
        closeTags(text, tags);
        return text;
    }

    /**
     * Iterates over an array of tags and applies them to the beginning of the specified
     * Spannable object so that future text appended to the text will have the styling
     * applied to it. Do not call this method directly.
     */
    private static void openTags(Spannable text, Object[] tags) {
        for (Object tag : tags) {
            text.setSpan(tag, 0, 0, Spannable.SPAN_MARK_MARK);
        }
    }

    /**
     * "Closes" the specified tags on a Spannable by updating the spans to be
     * endpoint-exclusive so that future text appended to the end will not take
     * on the same styling. Do not call this method directly.
     */
    private static void closeTags(Spannable text, Object[] tags) {
        int len = text.length();
        for (Object tag : tags) {
            if (len > 0) {
                text.setSpan(tag, 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                text.removeSpan(tag);
            }
        }
    }

    /**
     * Returns a CharSequence that applies a foreground color to the
     * concatenation of the specified CharSequence objects.
     */
    public static CharSequence color(int color, CharSequence... content) {
        return apply(content, new ForegroundColorSpan(color));
    }
}
