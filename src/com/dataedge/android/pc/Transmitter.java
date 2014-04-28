package com.dataedge.android.pc;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dataedge.android.pc.manager.LocatorFileManager;
import com.dataedge.android.pc.manager.MediaFileManager;
import com.dataedge.android.pc.model.LocationModel;
import com.dataedge.android.pc.model.MediaFileModel;
import com.dataedge.android.pc.model.XmlDataModel;

public class Transmitter {
    static String TAG = Transmitter.class.getName();

    public static boolean transmit(Context context, String locatorCode, String reportStatus) {
        Log.i(TAG, "transmit(context, " + locatorCode + ", " + reportStatus + ")");

        try {
//            // if already queued proceed
//            if (!reportStatus.equals(Codes.RPT_STATUS_QUEUED)) {
//                Log.i(TAG, "Turn into a Queued File");
//                // this is a current file. queue it
//                Utils.renameFile(Codes.DIR_APP_FILES + locatorCode + Codes.FILE_EXT_CURRENT,
//                        Codes.DIR_APP_FILES + locatorCode + Codes.FILE_EXT_QUEUED);
//            }

            //
            XmlDataModel xdm = getXmlData(context, locatorCode);
            // gather all data for transmission and upload
            if (!upload(xdm, locatorCode + Codes.FILE_EXT_XML,
                    Utils.getTitleForFilename(xdm.getTitle()) + Codes.FILE_EXT_XML, context))
                return false;

            if (Utils.fileExists(context, locatorCode + Codes.FILE_EXT_AUDIO))
                if (!upload(null, locatorCode + Codes.FILE_EXT_AUDIO,
                        Utils.getTitleForFilename(xdm.getTitle()) + Codes.FILE_EXT_AUDIO, context))
                    return false;

            // picture files - not thumbnails
            MediaFileManager mfm = new MediaFileManager(context);
            List<MediaFileModel> mediaFiles = mfm.getMediaFiles(locatorCode, null);
            Iterator<MediaFileModel> iterator = mediaFiles.iterator();

            MediaFileModel mediaFile = null;
            while (iterator.hasNext()) {
                mediaFile = iterator.next();
                if (!upload(null, mediaFile.getFileName(),
                        Utils.getTitleForFilename(xdm.getTitle())
                                + mediaFile.getFileName().substring(8), context))
                    return false;
            }

            // this is just for sanity. It does not need to be sent
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getBoolean("SendAllFiles", false)) {

                // send thumbnails
                MediaFileManager mfm2 = new MediaFileManager(context);
                List<MediaFileModel> mediaFiles2 = mfm2.getMediaFiles(locatorCode, Codes.THUMBNAILS_ONLY);
                Iterator<MediaFileModel> iterator2 = mediaFiles2.iterator();

                MediaFileModel mediaFile2 = null;
                while (iterator2.hasNext()) {
                    mediaFile2 = iterator2.next();
                    if (!upload(null, mediaFile2.getFileName(),
                            Utils.getTitleForFilename(xdm.getTitle())
                                    + mediaFile2.getFileName().substring(8), context))
                        return false;
                }

                if (!upload(null, locatorCode + Codes.FILE_EXT_QUEUED,
                        Utils.getTitleForFilename(xdm.getTitle()) + Codes.FILE_EXT_QUEUED, context))
                    return false;
            }

//            Log.i(TAG, "Turn into a Transmission File");
//            // set locator file to transmitted
//            Utils.renameFile(Codes.DIR_APP_FILES + locatorCode + Codes.FILE_EXT_QUEUED,
//                    Codes.DIR_APP_FILES + locatorCode + Codes.FILE_EXT_TRANSMITTED);

        } catch (Exception e) {
            Log.e(TAG, "Transmission error: " + e.toString());
            return false;
        }
        Log.i(TAG, "returning true");
        return true;
    }

    private static XmlDataModel getXmlData(Context context, String locatorCode) {
        Log.i(TAG, "getXmlData(context, " + locatorCode + ")");

        XmlDataModel xdm = new XmlDataModel(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // populate these from the preferences settings
        xdm.setNotifyMe(prefs.getBoolean("NotifyMe", false));
        xdm.setMyEmail(prefs.getString("MyEmail", ""));
        xdm.setIdentifyMe(prefs.getBoolean("IdentifyMe", false));
        xdm.setMyEmpID(prefs.getString("MyEmpID", ""));
        xdm.setMyName(prefs.getString("MyName", ""));
        xdm.setMyPosition(prefs.getString("MyPosition", ""));
        xdm.setMyPhone(prefs.getString("MyPhone", ""));

        //
        xdm.setLocatorCode(locatorCode);

        // transmission date
        DateFormat dateFormat = new SimpleDateFormat(Codes.DATE_FORMAT);
        Date date = new Date(System.currentTimeMillis());
        xdm.setTransDate(dateFormat.format(date).toString());

        // these are stored in the locator code file.
        // at this point, the QUEUED file is always the one needed
        String theFile = locatorCode + Codes.FILE_EXT_QUEUED;

        String type = LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_REPORT_TYPE_KEY);
        if (type != null)
            xdm.setReportType(type.toUpperCase());

        String title = LocatorFileManager.getValue(context, theFile, Codes.LOCATOR_FILE_TITLE_KEY);

        if (title != null)
            xdm.setTitle(title.toUpperCase());

        String eventDate = LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_EVENT_DATE_KEY);

        if (eventDate != null)
            xdm.setEventDate(eventDate.toUpperCase());

        String eventTime = LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_EVENT_TIME_KEY);

        if (eventTime != null)
            xdm.setEventTime(eventTime.toUpperCase());

        String flightNum = LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_FLIGHT_NUM_KEY);
        if (flightNum != null)
            xdm.setFlightNum(flightNum.toUpperCase());

        String tailNum = LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_TAIL_NUM_KEY);
        if (tailNum != null)
            xdm.setTailNum(tailNum.toUpperCase());

        xdm.setBirdStrike(LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_BIRD_STRIKE_KEY));

        xdm.setDangerGood(LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_DANGEROUS_GOOD_KEY));

        xdm.setMemoTime(LocatorFileManager.getMemoDuration(context, locatorCode
                + Codes.FILE_EXT_QUEUED));

        String queuedIssue = LocatorFileManager.getValue(context, theFile,
                Codes.LOCATOR_FILE_QUEUED_ISSUE_KEY);

        if (queuedIssue != null) {
            // this report had been queued
            xdm.setQueuedIssue(queuedIssue);

            // we want to use the location based from the original submit
            xdm.setLongitude(LocatorFileManager.getValue(context, theFile,
                    Codes.LOCATOR_FILE_LONGITUDE_KEY));
            xdm.setLatitude(LocatorFileManager.getValue(context, theFile,
                    Codes.LOCATOR_FILE_LATITUDE_KEY));
            xdm.setAltitude(LocatorFileManager.getValue(context, theFile,
                    Codes.LOCATOR_FILE_ALTITUDE_KEY));
            xdm.setBearing(LocatorFileManager.getValue(context, theFile,
                    Codes.LOCATOR_FILE_BEARING_KEY));
        } else {
            // add location based info to locator file in case this report gets
            // queued.
            LocationModel lm = Utils.getLocationAttributes(context);

            // use for truncating lat, long and bearing
            DecimalFormat df = new DecimalFormat("#0.######");

            if (lm != null) {

                LocatorFileManager.insertKeyValuePair(context, theFile,
                        Codes.LOCATOR_FILE_LATITUDE_KEY, df.format(lm.getLatitude()));
                LocatorFileManager.insertKeyValuePair(context, theFile,
                        Codes.LOCATOR_FILE_LONGITUDE_KEY, df.format(lm.getLongitude()));
                LocatorFileManager.insertKeyValuePair(context, theFile,
                        Codes.LOCATOR_FILE_ALTITUDE_KEY, Double.toString(lm.getAltitude()));
                LocatorFileManager.insertKeyValuePair(context, theFile,
                        Codes.LOCATOR_FILE_BEARING_KEY, df.format(lm.getBearing()));

                xdm.setLatitude(df.format(lm.getLatitude()));
                xdm.setLongitude(df.format(lm.getLongitude()));
                xdm.setAltitude(Double.toString(lm.getAltitude()));
                xdm.setBearing(df.format(lm.getBearing()));
            }
        }

        return xdm;

    }

    // uploads either a file or an array of bytes.
    // if xdm is not null, it will upload the array of bytes,
    // otherwise a file. it will throw exception and return false if that file
    // was never created
    public static boolean upload(XmlDataModel xdm, String filename, String namedAs, Context context) {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;

        String pathToOurFile = Codes.DIR_APP_FILES + filename;

        // fix this later
        // String urlServer = Codes.PHP_UPLOAD_HANDLER_URI;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String urlServer = "http://" + prefs.getString("localhost", "")
//                + "/dataedge/upload_handler.php";
        String urlServer = "http://" + prefs.getString("localhost", "")
        + "/catcher.php";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        try {
            InputStream inputStream = null;
            if (xdm != null) {
                inputStream = new ByteArrayInputStream(xdm.toXml().getBytes());
            } else {
                inputStream = new FileInputStream(new File(pathToOurFile));
            }

            URL url = new URL(urlServer);
            connection = (HttpURLConnection) url.openConnection();

            // Allow Inputs & Outputs
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(180000); // it should get a connection
                                                  // within 10 seconds

            // Enable POST method
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="
                    + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream
                    .writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                            + namedAs + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = inputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = inputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = inputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            inputStream.close();
            outputStream.flush();
            outputStream.close();
        } catch (SocketTimeoutException ste) {
            Log.e(TAG, ste.toString());
            return false;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return false;
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return true;
    }
}
