package com.dataedge.android.pc.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "applicationdata";

    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table report (_id integer primary key autoincrement, "
            + "report_type text not null, report_status text not null, title text not null, event_dt text, edit_dt text, flight_num text, "
            + "tail_num text, memo_time text, bird_strike_b integer, dangerous_good_b integer);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    // Method is called during an upgrade of the database, e.g. if you increase
    // the database version
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS report");
        onCreate(database);
    }
}
