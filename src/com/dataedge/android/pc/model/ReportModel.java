package com.dataedge.android.pc.model;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.Utils;

public class ReportModel {

    private static final String ELLIPSIS = "...";

    private String locatorCode;
    private String fileExt;
    private String editDate;
    private String recTime;
    private String reportStatus;
    private String eventDate;
    private String picCount;

    //
    private String reportType;
    private String reportTitle;

    public ReportModel(String locatorCode, String fileExt, String editDate, String recTime,
            String reportType, String reportTitle, String eventDate, String picCount) {
        this.locatorCode = locatorCode;
        this.fileExt = fileExt;
        this.editDate = editDate;
        this.recTime = recTime;
        if (fileExt.equals(Codes.FILE_EXT_CURRENT)) {
            this.reportStatus = Codes.RPT_STATUS_CURRENT;
        } else if (fileExt.equals(Codes.FILE_EXT_QUEUED)) {
            this.reportStatus = Codes.RPT_STATUS_QUEUED;
        } else {
            this.reportStatus = Codes.RPT_STATUS_TRANSMITTED;
        }
        //
        this.reportType = reportType;

        //
        this.reportTitle = Utils.getTitleForDisplay(reportTitle);

        //
        this.eventDate = eventDate;
        this.picCount = picCount;

    }

    public String getLocatorCode() {
        return locatorCode;
    }

    public void setLocatorCode(String locatorCode) {
        this.locatorCode = locatorCode;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public String getEditDate() {
        return editDate;
    }

    public void setEditDate(String editDate) {
        this.editDate = editDate;
    }

    public String getRecTime() {
        return recTime;
    }

    public void setRecTime(String recTime) {
        this.recTime = recTime;
    }

    public String getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(String reportStatus) {
        this.reportStatus = reportStatus;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventDate() {
        return eventDate;
    }
    
    public void setPicCount(String picCount) {
        this.picCount = picCount;
    }

    public String getPicCount() {
        return picCount;
    }
}