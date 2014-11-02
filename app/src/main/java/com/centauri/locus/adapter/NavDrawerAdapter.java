package com.centauri.locus.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.centauri.locus.R;

/**
 * Created by mohitd2000 on 11/2/14.
 */
public class NavDrawerAdapter extends ArrayAdapter<String> {
    private String[] items;
    private Context context;

    public NavDrawerAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
        this.items = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Efficiency is not the issue since there's only going to be 6 items max in the Nav Drawer
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.nav_drawer_item, parent, false);
        TextView textView = (TextView) view.findViewById(R.id.navigation_drawer_item);
        textView.setText(items[position]);
        Drawable icon = null;

        switch (position) {
            case 0:
                icon = context.getResources().getDrawable(R.drawable.ic_drawer_current);
                break;
            case 1:
                icon = context.getResources().getDrawable(R.drawable.ic_drawer_map);
                break;
            case 2:
                icon = context.getResources().getDrawable(R.drawable.ic_drawer_places);
                break;
        }

        if (icon != null) {
            int size = (int) context.getResources().getDimension(R.dimen.nav_drawer_icon_size);
            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
            Drawable scaledIcon = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, size, size, true));
            textView.setCompoundDrawablesWithIntrinsicBounds(scaledIcon, null, null, null);
        }
        return view;
    }
}
