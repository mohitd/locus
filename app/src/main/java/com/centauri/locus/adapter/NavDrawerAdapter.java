package com.centauri.locus.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.centauri.locus.R;

/**
 * Created by mohitd2000 on 11/2/14.
 */
public class NavDrawerAdapter extends ArrayAdapter<String> {
    private String[] items;
    private Context context;

    public NavDrawerAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
        this.items = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Efficiency is not the issue since there's only going to be 6 items max in the Nav Drawer
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.nav_drawer_item, parent, false);
        TextView textView = (TextView) view.findViewById(R.id.title);
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);

        textView.setText(items[position]);
        Drawable icon;

        switch (position) {
            case 0:
                icon = context.getResources().getDrawable(R.drawable.ic_drawer_inbox);
                break;
            case 1:
                icon = context.getResources().getDrawable(R.drawable.ic_drawer_map);
                break;
            case 2:
                icon = context.getResources().getDrawable(R.drawable.ic_drawer_places);
                break;
            default:
                return null;
        }

        if (icon != null) imageView.setImageDrawable(icon);
        return view;
    }
}
