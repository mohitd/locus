/**
 * 
 */
package com.centauri.locus.adapter;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.centauri.locus.R;
import com.centauri.locus.provider.Locus;
import com.centauri.locus.util.BitmapCache;
import com.centauri.locus.util.StaticMapsLoader;
import com.centauri.locus.widget.CircularImageView;
import com.google.android.gms.maps.model.LatLng;

/**
 * @author mohitd2000
 * 
 */
public class TaskAdapter extends CursorAdapter {

    private BitmapCache cache;

    /**
     * @param context
     * @param c
     */
    public TaskAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        cache = BitmapCache.getInstance();
    }

    /**
     * @see android.widget.CursorAdapter#newView(android.content.Context,
     *      android.database.Cursor, android.view.ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item_task, parent, false);

        return view;
    }

    /**
     * @see android.widget.CursorAdapter#bindView(android.view.View,
     *      android.content.Context, android.database.Cursor)
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(Locus.Task._ID));
        String title = cursor.getString(cursor.getColumnIndex(Locus.Task.COLUMN_TITLE));
        double lat = cursor.getDouble(cursor.getColumnIndex(Locus.Task.COLUMN_LATITUDE));
        double lon = cursor.getDouble(cursor.getColumnIndex(Locus.Task.COLUMN_LONGITUDE));
        int completed = cursor.getInt(cursor.getColumnIndexOrThrow(Locus.Task.COLUMN_COMPLETED));

        final TextView titleTextView = (TextView) view.findViewById(R.id.title_textview);
        CircularImageView mapImageView = (CircularImageView) view.findViewById(R.id.location_imageview);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);

        titleTextView.setText(title);
        checkBox.setChecked(completed == 1);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox completed = (CheckBox) view;
                int selected = completed.isChecked() ? 1 : 0;
                Uri uri = ContentUris.withAppendedId(Locus.Task.CONTENT_URI, id);
                ContentValues values = new ContentValues();
                values.put(Locus.Task.COLUMN_COMPLETED, selected);
                context.getContentResolver().update(uri, values, null, null);
            }
        });

        String key = 't' + 's' + String.valueOf(id);
        Bitmap image = cache.getBitmapFromCache(key);
        if (image != null) {
            mapImageView.setImageBitmap(image);
        } else {
            StaticMapsLoader task = new StaticMapsLoader(mapImageView, id,
                    StaticMapsLoader.SIZE_SMALL, StaticMapsLoader.TABLE_TASK);
            task.execute(new LatLng(lat, lon));
        }

    }
}
