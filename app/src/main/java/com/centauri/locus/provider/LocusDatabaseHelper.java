/**
 * 
 */
package com.centauri.locus.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.centauri.locus.provider.Locus.Task;

/**
 * @author mohitd2000
 * 
 */
public class LocusDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "locus.db";
    private static final int DATABASE_VERSION = 1;

    public LocusDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + Task.TABLE_NAME + "(" + Task._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + Task.COLUMN_TITLE
                + " TEXT NOT NULL," + Task.COLUMN_DESCRIPTION + " TEXT,"
                + Task.COLUMN_LATITUDE + " REAL," + Task.COLUMN_LONGITUDE + " REAL,"
                + Task.COLUMN_RADIUS + " INTEGER," + Task.COLUMN_DUE + " REAL,"
                + Task.COLUMN_TRANSITION + " INTEGER," + Task.COLUMN_COMPLETED + " INTEGER" + ");");

        db.execSQL("INSERT INTO task (title, description, latitude, longitude, radius, due, transition, completed) VALUES('Do some task', 'This is some description that is hopefully much much much much longer than the description box', "
                + "40.7127, -74.0059, 50, 86400000, 0, 0);");
        db.execSQL("INSERT INTO task (title, latitude, longitude, radius, due, transition, completed) VALUES('Do some task that is hopefully much longer than the space provided for the title', "
                + "40.0000, -83.0145, 100, 86400000, 0, 0);");
        db.execSQL("INSERT INTO task (title, description, latitude, longitude, radius, due, transition, completed) VALUES('Do some task', 'Finished this task!', "
                + "40.7127, -74.0059, 50, 86400000, 0, 1);");
        db.execSQL("INSERT INTO task (title, latitude, longitude, radius, due, transition, completed) VALUES('Another finished task', "
                + "40.0000, -83.0145, 100, 86400000, 0, 1);");

    }

    /**
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
     *      int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
