package com.dataedge.android.pc.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ReportDbAdapter {

    // Database fields
    public static final String KEY_ROWID = "_id";
    public static final String KEY_TYPE = "report_type";
    public static final String KEY_STATUS = "report_status";
    public static final String KEY_TITLE = "title";
    public static final String KEY_EVENT_DT = "event_dt";
    public static final String KEY_EDIT_DT = "edit_dt";
    public static final String KEY_FLIGHT_NUM = "flight_num";
    public static final String KEY_TAIL_NUM = "tail_num";
    public static final String MEMO_TIME = "memo_time";
    public static final String KEY_BIRD_STRIKE = "bird_strike_b";
    public static final String KEY_DANGEROUS_GOOD = "dangerous_good_b";
    private static final String DATABASE_TABLE = "report";
    private Context context;
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public ReportDbAdapter(Context context) {
        this.context = context;
    }

    public ReportDbAdapter open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Create a new report If the report is successfully created return the new
     * rowId for that note, otherwise return a -1 to indicate failure.
     */
    public long createReport(String report_type, String report_status, String title,
            String event_dt, String edit_dt, String flight_num, String tail_num, String memo_time,
            int bird_strike_b, int dangerous_good_b) {
        ContentValues initialValues = createContentValues(report_type, report_status, title,
                event_dt, edit_dt, flight_num, tail_num, memo_time, bird_strike_b, dangerous_good_b);

        return database.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Update the report
     */
    public boolean updateReport(long rowId, String report_type, String report_status, String title,
            String event_dt, String edit_dt, String flight_num, String tail_num, String memo_time,
            int bird_strike_b, int dangerous_good_b) {
        ContentValues updateValues = createContentValues(report_type, report_status, title,
                event_dt, edit_dt, flight_num, tail_num, memo_time, bird_strike_b, dangerous_good_b);

        return database.update(DATABASE_TABLE, updateValues, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Deletes report
     */
    public boolean deleteReport(long rowId) {
        return database.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all reports in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllReports() {
        return database.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TYPE, KEY_STATUS,
                KEY_TITLE, KEY_EVENT_DT, KEY_EDIT_DT, KEY_FLIGHT_NUM, KEY_TAIL_NUM, MEMO_TIME,
                KEY_BIRD_STRIKE, KEY_DANGEROUS_GOOD }, null, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the defined report
     */
    public Cursor fetchReport(long rowId) throws SQLException {
        Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TYPE,
                KEY_STATUS, KEY_TITLE, KEY_EVENT_DT, KEY_EDIT_DT, KEY_FLIGHT_NUM, KEY_TAIL_NUM,
                MEMO_TIME, KEY_BIRD_STRIKE, KEY_DANGEROUS_GOOD }, KEY_ROWID + "=" + rowId, null,
                null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    private ContentValues createContentValues(String report_type, String report_status,
            String title, String event_dt, String edit_dt, String flight_num, String tail_num,
            String memo_time, int bird_strike_b, int dangerous_good_b) {
        ContentValues values = new ContentValues();

        values.put(KEY_TYPE, report_type);
        values.put(KEY_STATUS, report_status);
        values.put(KEY_TITLE, title);
        values.put(KEY_EVENT_DT, event_dt);
        values.put(KEY_EDIT_DT, edit_dt);
        values.put(KEY_FLIGHT_NUM, flight_num);
        values.put(KEY_TAIL_NUM, tail_num);
        values.put(MEMO_TIME, memo_time);
        values.put(KEY_BIRD_STRIKE, bird_strike_b);
        values.put(KEY_DANGEROUS_GOOD, dangerous_good_b);
        return values;
    }
}
