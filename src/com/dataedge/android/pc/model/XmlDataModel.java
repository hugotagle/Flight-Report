package com.dataedge.android.pc.model;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.dataedge.android.pc.Base64;
import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.manager.MediaFileManager;

public class XmlDataModel {

    private Context context;

    // from settings
    private boolean notifyMe;
    private String myEmail;
    private boolean identifyMe;
    private String myEmpID;
    private String myName;
    private String myPosition;
    private String myPhone;

    // from flight safety report
    private String locatorCode;
    private String transDate;
    private String memoTime;
    private String reportType;
    private String title;
    private String eventDate;
    private String eventTime;
    private String flightNum;
    private String tailNum;
    private String birdStrike;
    private String dangerGood;

    // miscellaneous
    private String queuedIssue;
    private String encodedAudio;
    private List<String> encodedMediaFiles;

    // location based
    private String longitude;
    private String latitude;
    private String altitude;
    private String bearing;

    public XmlDataModel() {

    }

    public XmlDataModel(Context context) {
        this.context = context;
    }

    public boolean isNotifyMe() {
        return notifyMe;
    }

    public void setNotifyMe(boolean notifyMe) {
        this.notifyMe = notifyMe;
    }

    public String getMyEmail() {
        return myEmail;
    }

    public void setMyEmail(String myEmail) {
        this.myEmail = myEmail;
    }

    public boolean isIdentifyMe() {
        return identifyMe;
    }

    public void setIdentifyMe(boolean identifyMe) {
        this.identifyMe = identifyMe;
    }

    public String getMyEmpID() {
        return myEmpID;
    }

    public void setMyEmpID(String myEmpID) {
        this.myEmpID = myEmpID;
    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
    }

    public String getMyPosition() {
        return myPosition;
    }

    public void setMyPosition(String myPosition) {
        this.myPosition = myPosition;
    }

    public String getMyPhone() {
        return myPhone;
    }

    public void setMyPhone(String myPhone) {
        this.myPhone = myPhone;
    }

    public String getLocatorCode() {
        return locatorCode;
    }

    public void setLocatorCode(String locatorCode) {
        this.locatorCode = locatorCode;
    }

    public String getTransDate() {
        return transDate;
    }

    public void setTransDate(String transDate) {
        this.transDate = transDate;
    }

    public String getMemoTime() {
        return memoTime;
    }

    public void setMemoTime(String memoTime) {
        this.memoTime = memoTime;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public void setFlightNum(String flightNum) {
        this.flightNum = flightNum;
    }

    public String getTailNum() {
        return tailNum;
    }

    public void setTailNum(String tailNum) {
        this.tailNum = tailNum;
    }

    public String getBirdStrike() {
        return birdStrike;
    }

    public void setBirdStrike(String birdStrike) {
        this.birdStrike = birdStrike;
    }

    public String getDangerGood() {
        return dangerGood;
    }

    public void setDangerGood(String dangerGood) {
        this.dangerGood = dangerGood;
    }

    public String getQueuedIssue() {
        return queuedIssue;
    }

    public void setQueuedIssue(String queuedIssue) {
        this.queuedIssue = queuedIssue;
    }

    public String getEncodedAudio() {
        return encodedAudio;
    }

    public void setEncodedAudio(String encodedAudio) {
        this.encodedAudio = encodedAudio;
    }

    public List<String> getEncodedMediaFiles() {
        return encodedMediaFiles;
    }

    public void setEncodedMediaFiles(List<String> encodedMediaFiles) {
        this.encodedMediaFiles = encodedMediaFiles;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String toXml() {

        // we create a XmlSerializer in order to write xml data
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
            // we set the FileOutputStream as output for the serializer, using
            // UTF-8 encoding
            serializer.setOutput(writer);
            // Write <?xml declaration with encoding (if encoding not null) and
            // standalone flag (if standalone not null)
            serializer.startDocument("UTF-8", true);
            // set indentation option
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            // start a TAG called "root"
            serializer.startTag(null, "FlightSafetyReport");

            // Device ID
            serializer.startTag(null, "DeviceReportId");
            serializer.text((this.getLocatorCode() != null) ? this.getLocatorCode() : "");
            serializer.endTag(null, "DeviceReportId");
            
            // NotifyMe
            serializer.startTag(null, "NotifyMe");
            serializer.text(this.isNotifyMe() ? "true" : "false");
            serializer.endTag(null, "NotifyMe");

            // MyEmail
            serializer.startTag(null, "MyEmail");
            serializer.text((this.getMyEmail() != null) ? this.getMyEmail() : "");
            serializer.endTag(null, "MyEmail");

            // IdentifyMe
            serializer.startTag(null, "IdentifyMe");
            serializer.text(this.isIdentifyMe() ? "true" : "false");
            serializer.endTag(null, "IdentifyMe");

            // MyEmpID
            serializer.startTag(null, "MyEmpID");
            serializer.text((this.getMyEmpID() != null) ? this.getMyEmpID() : "");
            serializer.endTag(null, "MyEmpID");

            // MyName
            serializer.startTag(null, "MyName");
            serializer.text((this.getMyName() != null) ? this.getMyName() : "");
            serializer.endTag(null, "MyName");

            // MyPosition
            serializer.startTag(null, "MyPosition");
            serializer.text((this.getMyPosition() != null) ? this.getMyPosition() : "");
            serializer.endTag(null, "MyPosition");

            // MyPhone
            serializer.startTag(null, "MyPhone");
            serializer.text((this.getMyPhone() != null) ? this.getMyPhone() : "");
            serializer.endTag(null, "MyPhone");

            // TransDate
            serializer.startTag(null, "TransDate");
            serializer.text((this.getTransDate() != null) ? this.getTransDate() : "");
            serializer.endTag(null, "TransDate");

            // Type
            serializer.startTag(null, "Type");
            serializer.text((this.getReportType() != null) ? this.getReportType() : "");
            serializer.endTag(null, "Type");

            // Title
            serializer.startTag(null, "Title");
            serializer.text((this.getTitle() != null) ? this.getTitle() : "");
            serializer.endTag(null, "Title");

            // Event Date
            serializer.startTag(null, "EventDate");
            serializer.text((this.getEventDate() != null) ? this.getEventDate() : "");
            serializer.endTag(null, "EventDate");

            // Event Time
            serializer.startTag(null, "EventTime");
            serializer.text((this.getEventTime() != null) ? this.getEventTime() : "");
            serializer.endTag(null, "EventTime");

            // FlightNum
            serializer.startTag(null, "FlightNum");
            serializer.text((this.getFlightNum() != null) ? this.getFlightNum() : "");
            serializer.endTag(null, "FlightNum");

            // TailNum
            serializer.startTag(null, "TailNum");
            serializer.text((this.getTailNum() != null) ? this.getTailNum() : "");
            serializer.endTag(null, "TailNum");

            // bird strike
            serializer.startTag(null, "BirdStrike");
            serializer.text((this.getBirdStrike() != null) ? this.getBirdStrike() : "");
            serializer.endTag(null, "BirdStrike");

            // TailNum
            serializer.startTag(null, "DangerousGood");
            serializer.text((this.getDangerGood() != null) ? this.getDangerGood() : "");
            serializer.endTag(null, "DangerousGood");

            // // Memo Time
            // serializer.startTag(null, "MemoTime");
            // serializer.text((this.getMemoTime() != null) ? this.getMemoTime()
            // : "");
            // serializer.endTag(null, "MemoTime");

            // Queued Issue
            serializer.startTag(null, "QueuedIssue");
            serializer.text((this.getQueuedIssue() != null) ? this.getQueuedIssue() : "");
            serializer.endTag(null, "QueuedIssue");

            // longitude
            serializer.startTag(null, "Longitude");
            serializer.text((this.getLongitude() != null) ? this.getLongitude() : "");
            serializer.endTag(null, "Longitude");

            // latitude
            serializer.startTag(null, "Latitude");
            serializer.text((this.getLatitude() != null) ? this.getLatitude() : "");
            serializer.endTag(null, "Latitude");

            // altitude
            serializer.startTag(null, "Altitude");
            serializer.text((this.getAltitude() != null) ? this.getAltitude() : "");
            serializer.endTag(null, "Altitude");

            // baring
            serializer.startTag(null, "Bearing");
            serializer.text((this.getBearing() != null) ? this.getBearing() : "");
            serializer.endTag(null, "Bearing");

            // encoded audio file
            serializer.startTag(null, "AudioFiles");
            if (Utils.fileExists(context, locatorCode + Codes.FILE_EXT_AUDIO)) {
                serializer.attribute(null, "number", "1");
                serializer.startTag(null, "AudioFile");
                serializer.attribute(null, "format", "MPEG4");
                serializer.attribute(null, "duration",
                        (this.getMemoTime() != null) ? this.getMemoTime()
                                : Codes.ZEROS_DEFAULT_TIME);
                // serializer.text((this.getEncodedAudio() != null) ?
                // this.getEncodedAudio() : "");
                // serializer.text(Base64.encodeFromFile(Codes.DIR_APP_FILES +
                // locatorCode
                // + Codes.FILE_EXT_AUDIO));
                serializer.text(locatorCode + Codes.FILE_EXT_AUDIO);
                serializer.endTag(null, "AudioFile");
            }
            serializer.endTag(null, "AudioFiles");

            //
            // encoded media files
            serializer.startTag(null, "PictureFiles");

            MediaFileManager mfm = new MediaFileManager(context);
            List<MediaFileModel> mediaFiles = mfm.getMediaFiles(locatorCode, null);

            if (mediaFiles.size() > 0) {
                // get the number of files
                String fileNumber = new Integer(mediaFiles.size()).toString();
                // set the number of files
                serializer.attribute(null, "number", fileNumber);
                // iterate thru them
                Iterator<MediaFileModel> iterator = mediaFiles.iterator();
                while (iterator.hasNext()) {
                    serializer.startTag(null, "PictureFile");
                    serializer.attribute(null, "format", "JPEG");
                    MediaFileModel mediaFile = (MediaFileModel) iterator.next();
                    // serializer.text(Base64.encodeFromFile(Codes.DIR_APP_FILES
                    // + mediaFile.getFileName()));
                    serializer.attribute(null, "size", new Long(mediaFile.getFile().length()).toString());
                    serializer.text(mediaFile.getFileName());
                    serializer.endTag(null, "PictureFile");
                }
            }
            serializer.endTag(null, "PictureFiles");

            serializer.endTag(null, "FlightSafetyReport");
            serializer.endDocument();

            // write xml data into the FileOutputStream
            serializer.flush();

        } catch (Exception e) {
            Log.e("Exception", "error occurred while creating xml writer");
            return null;
        }

        return writer.toString();

    }

}
