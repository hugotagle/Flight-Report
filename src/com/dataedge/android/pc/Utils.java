package com.dataedge.android.pc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.webkit.URLUtil;

import com.dataedge.android.pc.manager.LocatorFileManager;
import com.dataedge.android.pc.manager.MediaFileManager;
import com.dataedge.android.pc.model.LocationModel;
import com.dataedge.android.pc.model.MediaFileModel;

public final class Utils {
    static String TAG = Utils.class.getName();

    // clean up all locator code related files
    public static void deleteLocatorCodeRelatedFiles(Context context, String locatorCode) {
        //
        removeFile(context, locatorCode + Codes.FILE_EXT_CURRENT);
        removeFile(context, locatorCode + Codes.FILE_EXT_QUEUED);
        removeFile(context, locatorCode + Codes.FILE_EXT_TRANSMITTED);
        removeFile(context, locatorCode + Codes.FILE_EXT_AUDIO);
        removeFile(context, locatorCode + Codes.FILE_EXT_XML);

        // remove all media files including thumbnails
        MediaFileManager mfm = new MediaFileManager(context);
        List<MediaFileModel> files = mfm.getMediaFiles(locatorCode, Codes.THUMBNAILS_INCLUDED);

        // iterate to remove every media file
        Iterator<MediaFileModel> iterator = files.iterator();
        MediaFileModel mediaFile = null;
        while (iterator.hasNext()) {
            mediaFile = (MediaFileModel) iterator.next();
            removeFile(context, mediaFile.getFileName());
        }

    }

    //
    public static boolean isServerAvailable() {
        if (URLUtil.isValidUrl(Codes.PHP_UPLOAD_HANDLER_URI)) {
            Log.i(TAG, "Server Available");
            return true;
        } else {
            Log.i(TAG, "Server Unavailable");
            return false;
        }
    }

    // check internet connectivity
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            Log.i(TAG, "Internet Available");
            return true;
        }
        Log.i(TAG, "Internet Unavailable");
        return false;

    }

    // resets locator code
    public static void setLocatorCode(SharedPreferences myPrefs, String value) {
        Log.i(TAG, "setLocatorCode(myPrefs, " + value + ")");

        SharedPreferences.Editor editor = myPrefs.edit();
        if (value != null)
            value = value.replace(Codes.FILE_EXT_CURRENT, "").replace(Codes.FILE_EXT_QUEUED, "")
                    .replace(Codes.FILE_EXT_TRANSMITTED, "");
        editor.putString("locatorCode", value);
        editor.commit();
    }

    // returns true if file exists in the data/data/<package-name>/files
    // directory
    public static boolean fileExists(Context context, String filename) {
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File x = files[i];
            // if file is not a driving file skip
            if (x.getName().equals(filename))
                return true;
        }
        return false;

    }

    // creates the locatorCode file.
    public static void createFile(Context context, String locatorCode, String fileExt)
            throws Exception {

        FileOutputStream fos;
        fos = context.openFileOutput(locatorCode + fileExt, Context.MODE_PRIVATE);

        fos.close();
    }

    // deletes a file
    public static void removeFile(Context context, String theFile) {
        context.deleteFile(theFile);
    }

    // copies files. used mostly for copying the preferences file into
    // a locatorCode file that will be uploaded
    public static void copyFile(String inFile, String outFile) {
        Log.i(TAG, "copyFile(" + inFile + ", " + outFile + ")");
        File inputFile = new File(inFile);
        File outputFile = new File(outFile);
        try {
            FileReader in = new FileReader(inputFile);
            FileWriter out = new FileWriter(outputFile);
            int c;

            while ((c = in.read()) != -1)
                out.write(c);

            in.close();
            out.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    // renameFile - used for swapping application driving files
    // from .c to .q, etc ..
    public static void renameFile(String fromFile, String toFile) {
        Log.i(TAG, "renameFile(" + fromFile + ", " + toFile + ")");
        File file1 = new File(fromFile);
        File file2 = new File(toFile);

        file1.renameTo(file2);

    }

    // return millis as a 00:00 formatted string
    public static String getTimeFormatted(long millis) {

        String mins = "" + ((millis / 1000) / 60) % 60;
        String secs = "" + (millis / 1000) % 60;

        if (mins.length() == 1)
            mins = "0" + mins;
        if (secs.length() == 1)
            secs = "0" + secs;

        return mins + ":" + secs;
    }

    public static LocationModel getLocationAttributes(Context context) {

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // obtain best provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = lm.getBestProvider(criteria, true);

        // using best provider
        Location location = lm.getLastKnownLocation(provider);

        LocationModel locModel = null;

        if (location != null) {
            locModel = new LocationModel();
            locModel.setLongitude(location.getLongitude());
            locModel.setLatitude(location.getLatitude());
            locModel.setAltitude(location.getAltitude());
            locModel.setBearing(location.getBearing());
        }

        return locModel;
    }

    // generates the location code
    public static String generateLocatorCode() {
        Random rnd = new Random();

        char[] text = new char[Codes.LOCATOR_CODE_LENGTH];
        for (int i = 0; i < Codes.LOCATOR_CODE_LENGTH; i++) {
            text[i] = Codes.LOCATOR_CODE_AVAILABLE_CHARS.charAt(rnd
                    .nextInt(Codes.LOCATOR_CODE_AVAILABLE_CHARS.length()));
        }
        return new String(text);
    }

    // generates the location code.
    // makes sure locator code generated does not
    // alrady exist
    public static String generateLocatorCode(Context context) {

        // get all locator codes
        LocatorFileManager lfm = new LocatorFileManager(context);
        List<String> list = lfm.getLocatorCodes();
        char[] text;
        Random rnd = new Random();

        while (true) {
            text = new char[Codes.LOCATOR_CODE_LENGTH];
            for (int i = 0; i < Codes.LOCATOR_CODE_LENGTH; i++) {
                text[i] = Codes.LOCATOR_CODE_AVAILABLE_CHARS.charAt(rnd
                        .nextInt(Codes.LOCATOR_CODE_AVAILABLE_CHARS.length()));
            }

            String tmpLocatorCode = new String(text);
            if (!list.contains(tmpLocatorCode))
                return tmpLocatorCode;
        }
    }

    // return string with Codes.RPT_TITLE_LIST_MAX_SIZE + ELLIPSIS
    public static String getTitleForDisplay(String title) {

        final String ELLIPSIS = "...";

        if (title == null || title.equals(""))
            title = Codes.RPT_TITLE_NO_TITLE;
        if (title.length() > Codes.RPT_TITLE_LIST_MAX_SIZE) {
            // truncate and add ellipsis
            return title.substring(0, Codes.RPT_TITLE_LIST_MAX_SIZE) + ELLIPSIS;
        } else {
            // fill with spaces
            int maxChrs = Codes.RPT_TITLE_LIST_MAX_SIZE + ELLIPSIS.length();
            title = String.format("%s%" + maxChrs + "s", title, " ");
            return title.substring(0, Codes.RPT_TITLE_LIST_MAX_SIZE + ELLIPSIS.length());

        }
    }

    // return string with spaces replaced by underscores. Max return string is
    // still
    // Codes.RPT_TITLE_LIST_MAX_SIZE.
    // used for naming upload files
    public static String getTitleForFilename(String title) {

        final String SPACE_STRING = " ";
        final String UNDERSCORE = "_";

        if (title == null || title.equals(""))
            title = Codes.RPT_TITLE_NO_TITLE;
        if (title.length() > Codes.RPT_TITLE_LIST_MAX_SIZE) {
            // truncate and add ellipsis
            return title.substring(0, Codes.RPT_TITLE_LIST_MAX_SIZE).replaceAll(SPACE_STRING,
                    UNDERSCORE);
        } else {
            return title.replaceAll(SPACE_STRING, UNDERSCORE);

        }
    }

    // return bitmap of jpg file passed as paramter
    //
    public static Bitmap getThumbnail(String file) {

        // setup option
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        // decode file
        Bitmap decoded = BitmapFactory.decodeFile(file, options);
        // scaled bitmap
        Bitmap scaled = Bitmap.createScaledBitmap(decoded, 200, 240, true);
        // recycle
        decoded.recycle();
        // null out
        decoded = null;

        return scaled;
    }

    // check for numeric value in a string
    public static boolean isNumeric(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
