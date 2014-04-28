package com.dataedge.android.pc;

public final class Codes {

    // date format
    public static final String DATE_FORMAT = "MM/dd/yy hh:mm a";

    // random parameters
    public static final String LOCATOR_CODE_AVAILABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    public static final int LOCATOR_CODE_LENGTH = 8;

    // preference holding filenames
    //////public static final String FILENAME_LOCATOR_CODE_PREFS = "LocatorCodeFile";
    // public static final String FILENAME_PC_PREFS = "PCPrefsFile";
    public static final String FILENAME_PC_PREFS = "com.dataedge.android.pc_preferences";

    // select drop down filler
    public static final String OPTION_SELECT = "select...";

    // report status
    public static final String RPT_STATUS_CURRENT = "INITIATED";
    public static final String RPT_STATUS_QUEUED = "QUEUED";
    public static final String RPT_STATUS_TRANSMITTED = "TRANSMITTED";

    // report type
    public static final String RPT_TYPE_FLIGHT = "FLIGHT";
    public static final String RPT_TYPE_MAINTENANCE = "MAINTENANCE";

    //
    public static final String RPT_TITLE_NO_TITLE = "NO TITLE";
    public static final int RPT_TITLE_LIST_MAX_SIZE = 15;

    // report status label color
    public static final int RPT_STATUS_CURRENT_LABEL_COLOR = 0xffff8000; // orange
    // public static final int RPT_STATUS_QUEUED_LABEL_COLOR = 0xffffcc00; //
    // orange
    // public static final int RPT_STATUS_QUEUED_LABEL_COLOR = 0xfff88017; //
    // red
    public static final int RPT_STATUS_QUEUED_LABEL_COLOR = 0xff800000; // maroon
    public static final int RPT_STATUS_TRANSMITTED_LABEL_COLOR = 0xff006400; // green

    // file extensions
    public static final String FILE_EXT_CURRENT = ".c";
    public static final String FILE_EXT_QUEUED = ".q";
    public static final String FILE_EXT_TRANSMITTED = ".t";
    public static final String FILE_EXT_AUDIO = ".mp4";
    // public static final String FILE_EXT_AUDIO = ".3gp";
    public static final String FILE_EXT_XML = ".xml";
    public static final String FILE_EXT_PHOTO = ".jpg";
    public static final String FILE_EXT_THUMB = ".png";

    // android file system
    public static final String DIR_APP_FILES = "data/data/com.dataedge.android.pc/files/";
    public static final String DIR_APP_SHARED_PREFS = "data/data/com.dataedge.android.pc/shared_prefs/";

    // PHP upload handler
    public static final String PHP_UPLOAD_HANDLER_URI = "http://192.168.1.109/dataedge/upload_handler.php";

    // misc
    public static final int MAX_NUMBER_IN_QUEUE = 12;
    public static final int MAX_NUMBER_PHOTO_FILES = 5;
    public static final String ZEROS_DEFAULT_TIME = "00:00";
    public static final String THUMBNAILS_INCLUDED = "INCLUDE";
    public static final String THUMBNAILS_ONLY = "ONLY";
    
    // media file manager
    public static final String MEDIA_FILE_QUEUE_LOCATION_NEITHER = "NEITHER";
    public static final String MEDIA_FILE_QUEUE_LOCATION_LOWEST = "LOWEST";
    public static final String MEDIA_FILE_QUEUE_LOCATION_HIGHEST = "HIGHEST";
    public static final String MEDIA_FILE_QUEUE_LOCATION_ONLY_ONE = "ONLY_ONE";

    // location file
    public static final String LOCATOR_FILE_REPORT_TYPE_KEY = "type";
    public static final String LOCATOR_FILE_EVENT_DATE_KEY = "eventDate";
    public static final String LOCATOR_FILE_EVENT_TIME_KEY = "eventTime";
    public static final String LOCATOR_FILE_MILLIS_KEY = "millis";
    public static final String LOCATOR_FILE_TITLE_KEY = "title";
    public static final String LOCATOR_FILE_FLIGHT_NUM_KEY = "flightNum";
    public static final String LOCATOR_FILE_TAIL_NUM_KEY = "tailNum";
    public static final String LOCATOR_FILE_BIRD_STRIKE_KEY = "birdStrike";
    public static final String LOCATOR_FILE_DANGEROUS_GOOD_KEY = "dangerousGood";
    public static final String LOCATOR_FILE_QUEUED_ISSUE_KEY = "queuedIssue";
    public static final String LOCATOR_FILE_LAST_SEND_ATTEMPT_KEY = "sendAttempt";
    
    // location based
    public static final String LOCATOR_FILE_LONGITUDE_KEY = "longitude";
    public static final String LOCATOR_FILE_LATITUDE_KEY = "latitude";
    public static final String LOCATOR_FILE_ALTITUDE_KEY = "altitude";
    public static final String LOCATOR_FILE_BEARING_KEY = "bearing";

    // transmission messages
    public static final String TRANS_MSG_CURRENT_SUCCESSFUL = "Report sent successfully";
    public static final String TRANS_MSG_NO_INTERNET = "Report was queued. No internet access detected";
    public static final String TRANS_MSG_NO_SERVER = "Report was queued. Server unavailable";
    public static final String TRANS_MSG_QUEUED_SUCCESSFUL = "Queued sent successfully";
    public static final String TRANS_MSG_QUEUED_NO_SERVER = "Server unavailable. Try again later";

    // queued issues
    public static final String TRANS_QUEUED_ISSUE_NO_INTERNET = "No Internet Access";
    public static final String TRANS_QUEUED_ISSUE_SERVER_UNAVAILABLE = "Server Issue";
    public static final String TRANS_QUEUED_ISSUE_SERVER_PROCESSING = "Transmitting ...";
    
    // report list
    public static final String REPORT_LIST_ONLY_QUEUED = "ONLY_QUEUED";
    public static final String REPORT_LIST_ONLY_UNEDITABLE = "ONLY_UNEDITABLE";
    
    // location stuff
    public static final String MAP_VIEW_TYPE_SATELLITE = "Sat";
    public static final String MAP_VIEW_TYPE_MAP = "Map";
}