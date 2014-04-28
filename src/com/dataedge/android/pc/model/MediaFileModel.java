package com.dataedge.android.pc.model;

import java.io.File;

public class MediaFileModel {

    private String fileName;
    private String locatorCode;
    private String sequenceNumber;
    private int intSqnc;
    private String editDate;
    private File file;

    /*
     * fileName: 787JH0SC_2.jpg
     */
    public MediaFileModel(String fileName, String editDate, File file) {
        this.fileName = fileName;
        this.locatorCode = fileName.substring(0, 8);
        this.sequenceNumber = fileName.substring(9, fileName.indexOf("."));
        this.intSqnc = new Integer(this.sequenceNumber).intValue();
        this.editDate = editDate;
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocatorCode() {
        return locatorCode;
    }

    public void setLocatorCode(String locatorCode) {
        this.locatorCode = locatorCode;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getEditDate() {
        return editDate;
    }

    public void setEditDate(String editDate) {
        this.editDate = editDate;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}