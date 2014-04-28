package com.dataedge.android.pc.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.comparator.ReportsComparator;
import com.dataedge.android.pc.model.ReportModel;

/*
 * LocatorFileManager
 * Manages the locator files used for navigation as well
 * as data containers. Static methods require the Context
 * as input. 
 * 
 * TODO There are several Utils methods that would have to be moved here since
 * they are used for creating and deleting these locator files
 * 
 */
public class LocatorFileManager {

    static String TAG = LocatorFileManager.class.getName();

    int excessRptNum = 0;

    private boolean currentAvailable;
    private String currentLocatorCodeId;
    private String currentTitle;
    private boolean queuedAvailable;

    private Context context;

    // constructor
    public LocatorFileManager() {

    }

    // constructor
    public LocatorFileManager(Context context) {

        this.context = context;
    }

    // returns list of locator codes being used
    public List<String> getLocatorCodes() {

        // all locator codes
        List<String> list = new ArrayList<String>();
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            // if file is not a driving file skip
            if (!(file.getName().contains(Codes.FILE_EXT_CURRENT)
                    || file.getName().contains(Codes.FILE_EXT_QUEUED) || file.getName().contains(
                    Codes.FILE_EXT_TRANSMITTED)))
                continue;
            //
            list.add(file.getName().substring(0, 8));
        }

        return list;

    }

    // returns list of report model objects built from
    // existing locator files. filterBy can be null for all reports,
    // ONLY_QUEUED for queued ones, ONLY_UNEDITABLE for all but INITIATED
    public List<ReportModel> getReports(String filterBy) {

        // all reports
        List<ReportModel> reports = new ArrayList<ReportModel>();
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            // if file is not a driving file skip
            if (!(file.getName().contains(Codes.FILE_EXT_CURRENT)
                    || file.getName().contains(Codes.FILE_EXT_QUEUED) || file.getName().contains(
                    Codes.FILE_EXT_TRANSMITTED)))
                continue;
            /*
             * Date lastModDate = new Date(file.lastModified()); DateFormat df =
             * new SimpleDateFormat(Codes.DATE_FORMAT);
             */
            //
            if (file.getName().contains(Codes.FILE_EXT_CURRENT)) {
                currentAvailable = true;
                currentLocatorCodeId = file.getName().substring(0, 8);
                currentTitle = LocatorFileManager.getValue(context, file.getName(),
                        Codes.LOCATOR_FILE_TITLE_KEY);
            } else if (file.getName().contains(Codes.FILE_EXT_QUEUED)) {
                queuedAvailable = true;
            }

            if (filterBy != null && filterBy.equals(Codes.REPORT_LIST_ONLY_QUEUED)
                    && !file.getName().contains(Codes.FILE_EXT_QUEUED))
                continue; // skip if not queued

            if (filterBy != null && filterBy.equals(Codes.REPORT_LIST_ONLY_UNEDITABLE)
                    && file.getName().contains(Codes.FILE_EXT_CURRENT))
                continue; // skip if current one

            // report: file name, file ext, edit date, memo duration, report
            // type,
            // title, event date + event time, and pic count

            Date lastModDate = new Date(file.lastModified());
            DateFormat df = new SimpleDateFormat(Codes.DATE_FORMAT);

            reports.add(new ReportModel(file.getName().substring(0, 8),
                    file.getName().substring(8), df.format(lastModDate).toString(),
                    LocatorFileManager.getMemoDuration(context, file.getName()), LocatorFileManager
                            .getValue(context, file.getName(), Codes.LOCATOR_FILE_REPORT_TYPE_KEY),
                    LocatorFileManager.getValue(context, file.getName(),
                            Codes.LOCATOR_FILE_TITLE_KEY), LocatorFileManager.getValue(context,
                            file.getName(), Codes.LOCATOR_FILE_EVENT_DATE_KEY)
                            + " "
                            + LocatorFileManager.getValue(context, file.getName(),
                                    Codes.LOCATOR_FILE_EVENT_TIME_KEY), new Integer(
                            MediaFileManager.getMediaFileCount(context,
                                    file.getName().substring(0, 8))).toString()));
        }

        // sort the reports
        Collections.sort(reports, new ReportsComparator());

        // check that reports in list do not exceed the
        // max set by user in settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String maxRptNum = prefs.getString("MaxRptNum", "" + Codes.MAX_NUMBER_IN_QUEUE);

        // # reports exceeding the max allowed
        excessRptNum = reports.size() - Integer.parseInt(maxRptNum);

        if (reports.size() > Integer.parseInt(maxRptNum)) {
            // more reports that the ones selected in the settings
            for (int i = 0; i < excessRptNum; i++) {
                // remove from LIST
                ReportModel rm = (ReportModel) reports.get(reports.size() - 1);
                reports.remove(rm);
                // remove report files from file disk
                Utils.deleteLocatorCodeRelatedFiles(context, rm.getLocatorCode());
            }

        }

        return reports;

    }

    // return only the value for a particular key
    public static String getValue(Context context, String theFile, String key) {
        // Log.i(TAG, "getValue(context, " + theFile + ", " + key + ")");

        String keyValue = getKeyValuePair(context, theFile, key);

        if (keyValue != null)
            return keyValue.substring(keyValue.indexOf("=") + 1);

        return null;

    }

    // returns value pair from input file if it exists
    private static String getKeyValuePair(Context context, String theFile, String key) {
        // Log.i(TAG, "getKeyValuePair(context, " + theFile + ", " + key + ")");

        InputStream inStream = null;

        try {
            // open the file for reading
            inStream = context.openFileInput(theFile);

            if (inStream != null) {
                // prepare the file for reading

                InputStreamReader inputReader = new InputStreamReader(inStream);
                BufferedReader bufferReader = new BufferedReader(inputReader);

                String line;
                String tmpLine = null;
                while ((line = bufferReader.readLine()) != null) {
                    if (line.contains(key)) {
                        // if the value gets entered more than
                        // once, the last one will be the current one
                        tmpLine = line;
                    }
                }

                return tmpLine;
            }

            return null;
        } catch (java.io.FileNotFoundException e) {
            // do something if the myfilename.txt does not exist
            Log.w(TAG, "FileNotFound: " + theFile + " might not have been created at this point");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "IOException: " + e.toString());
        } finally {
            if (inStream != null)
                try {
                    inStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.toString());
                }
        }

        return null;
    }

    // inserts key value pair into input file
    public static void insertKeyValuePair(Context context, String theFile, String key, String value) {
        Log.i(TAG, "insertKeyValuePair(context, " + theFile + ", " + key + ", " + value + ")");

        try {
            FileOutputStream fos = context.openFileOutput(theFile, Context.MODE_APPEND);
            fos.write((key + "=" + value + "\n").getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        }
    }

    // returns the formatted time from the inFile
    public static String getMemoDuration(Context context, String inFile) {
        // Log.i(TAG, "getMemoDuration(context, " + inFile + ")");

        // time is a millis string at this point
        String strMillis = getValue(context, inFile, Codes.LOCATOR_FILE_MILLIS_KEY);

        if (strMillis != null) {
            Long l = Long.valueOf(strMillis);
            return Utils.getTimeFormatted(l.longValue());
        }

        return null;
    }

    public String getCurrentLocatorCodeId() {
        return currentLocatorCodeId;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    public boolean isCurrentAvailable() {
        return currentAvailable;
    }

    public void setCurrentAvailable(boolean currentAvailable) {
        this.currentAvailable = currentAvailable;
    }

    public boolean isQueuedAvailable() {
        return queuedAvailable;
    }

    public void setQueuedAvailable(boolean queuedAvailable) {
        this.queuedAvailable = queuedAvailable;
    }

}
