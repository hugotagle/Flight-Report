package com.dataedge.android.pc.manager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.content.Context;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.comparator.MediaFilesComparator;
import com.dataedge.android.pc.model.MediaFileModel;

public class MediaFileManager {

    private static final int DEFAULT_HIGHEST_NUMBER = 1000;

    private Context context;

    // constructor
    public MediaFileManager(Context context) {

        this.context = context;
    }

    // returns list of media file model objects built from
    // existing media files for a particular locator code
    public List<MediaFileModel> getMediaFiles(String locatorCode, String aboutThumbs) {

        // all files
        List<MediaFileModel> mediaFiles = new ArrayList<MediaFileModel>();
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            // if file is not a driving file skip
            if (isValidMediaFile(locatorCode, file, aboutThumbs)) {
                Date lastModDate = new Date(file.lastModified());
                DateFormat df = new SimpleDateFormat(Codes.DATE_FORMAT);

                //
                mediaFiles.add(new MediaFileModel(file.getName(),
                        df.format(lastModDate).toString(), file));
            }
        }

        // sort the reports
        Collections.sort(mediaFiles, new MediaFilesComparator());

        return mediaFiles;

    }

    // returns the next sequenced filename for a new file to be created
    public static String getNextAvailableFilename(Context context, String locatorCode) {

        // all files
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();

        int highestNumber = 0;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            // if file is not a driving file skip
            if (isValidMediaFile(locatorCode, file, null)) {

                //
                int x = new Integer(file.getName().substring(9, file.getName().indexOf(".")))
                        .intValue();
                if (x > highestNumber)
                    highestNumber = x;
            }
        }

        return locatorCode + "_" + (highestNumber + 1);

    }

    // returns the next filename from the one provided.
    // If highest filename is to be returned, i
    public static String getNextFilename(Context context, String currentFilename) {

        // all files
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();

        String locatorCode = currentFilename.substring(0, 8);
        int currentFileNum = new Integer(currentFilename.substring(9, currentFilename.indexOf(".")))
                .intValue();

        int tmpNum = DEFAULT_HIGHEST_NUMBER; // must set this to a high number
        //
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            //
            if (isValidMediaFile(locatorCode, file, null)) {
                int fileNum = new Integer(file.getName().substring(9, file.getName().indexOf(".")))
                        .intValue();
                if (fileNum > currentFileNum) {
                    // this one is greater than the current one
                    if (fileNum < tmpNum) {
                        // this one is the smallest so far. Just greater than
                        // the current one
                        tmpNum = fileNum;
                    }
                }

            }
        }

        // current filename is the the highest number
        if (tmpNum == DEFAULT_HIGHEST_NUMBER)
            return null;

        return locatorCode + "_" + tmpNum;
    }

    // returns the previous filename from the one provided
    public static String getPreviousFilename(Context context, String currentFilename) {

        // all files
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();

        String locatorCode = currentFilename.substring(0, 8);
        int currentFileNum = new Integer(currentFilename.substring(9, currentFilename.indexOf(".")))
                .intValue();

        int tmpNum = 0; // must set this to a lowest number
        //
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            //
            if (isValidMediaFile(locatorCode, file, null)) {
                int fileNum = new Integer(file.getName().substring(9, file.getName().indexOf(".")))
                        .intValue();
                if (fileNum < currentFileNum) {
                    // this one is smaller than the current one
                    if (fileNum > tmpNum) {
                        // this one is the highest so far. Just smaller than
                        // the current one
                        tmpNum = fileNum;
                    }
                }

            }
        }

        // current filename is the the lowest set number
        if (tmpNum == 0)
            return null;

        return locatorCode + "_" + tmpNum;
    }

    // returns the location in the queue.
    // If only file in queue, it will return ONLY_ONE.
    // If highest number, it will return HIGHEST.
    // If lowest number, it will return LOWEST.
    // otherwise NEITHER
    public static String getFilenameQueuedLocation(Context context, String currentFilename) {

        // all files
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();

        String locatorCode = currentFilename.substring(0, 8);
        int currentFileNum = new Integer(currentFilename.substring(9, currentFilename.indexOf(".")))
                .intValue();
        //
        int counter = 0;
        int higher = currentFileNum;
        int lower = currentFileNum;

        //
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            //
            if (isValidMediaFile(locatorCode, file, null)) {
                int fileNum = new Integer(file.getName().substring(9, file.getName().indexOf(".")))
                        .intValue();
                counter++;
                if (fileNum > higher)
                    higher = fileNum;
                if (fileNum < lower)
                    lower = fileNum;
            }

        }

        // decision time
        if (counter == 1)
            return Codes.MEDIA_FILE_QUEUE_LOCATION_ONLY_ONE;
        if (higher == currentFileNum)
            return Codes.MEDIA_FILE_QUEUE_LOCATION_HIGHEST;
        if (lower == currentFileNum)
            return Codes.MEDIA_FILE_QUEUE_LOCATION_LOWEST;

        return Codes.MEDIA_FILE_QUEUE_LOCATION_NEITHER;
    }

    // returns true if the report has MAX_NUMBER_PHOTO_FILES
    public static boolean isMediaFileQuotaMet(Context context, String locatorCode) {

        if (getMediaFileCount(context, locatorCode) >= Codes.MAX_NUMBER_PHOTO_FILES)
            return true;

        return false;
    }

    // returns true if the report has no media files
    public static boolean isNoneMediaFiles(Context context, String locatorCode) {

        if (getMediaFileCount(context, locatorCode) == 0)
            return true;

        return false;
    }

    // returns the media file number associated with this report
    public static int getMediaFileCount(Context context, String locatorCode) {

        // all files
        File dir = new File(context.getFilesDir().toString());
        File[] files = dir.listFiles();
        int counter = 0;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            // if file is not a driving file skip
            if (isValidMediaFile(locatorCode, file, null)) {
                counter++;
            }
        }

        return counter;

    }

    // helper method
    private static boolean isValidMediaFile(String locatorCode, File file, String aboutThumbs) {

        if (aboutThumbs == null) {
            if (file.getName().contains(locatorCode)
                    && file.getName().contains(Codes.FILE_EXT_PHOTO))
                return true;
        } else if (aboutThumbs.equals(Codes.THUMBNAILS_INCLUDED)) {
            if (file.getName().contains(locatorCode)
                    && (file.getName().contains(Codes.FILE_EXT_PHOTO) || file.getName().contains(
                            Codes.FILE_EXT_THUMB)))
                return true;
        } else if (aboutThumbs.equals(Codes.THUMBNAILS_ONLY)) {
            if (file.getName().contains(locatorCode)
                    && file.getName().contains(Codes.FILE_EXT_THUMB))
                return true;
        }

        return false;
    }
}
