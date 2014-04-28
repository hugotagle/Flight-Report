package com.dataedge.android.pc;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dataedge.android.pc.manager.LocatorFileManager;
import com.dataedge.android.pc.model.ReportModel;

public class ReportArrayAdapter extends ArrayAdapter<ReportModel> {
    String TAG = ReportArrayAdapter.class.getName();

    private final Activity activity;
    private final List<ReportModel> reports;
    private final Context context;

    public ReportArrayAdapter(Activity activity, List<ReportModel> objects, Context context) {
        super(activity, R.layout.report_list_item, objects);
        this.activity = activity;
        this.reports = objects;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ReportView rptView = null;

        if (rowView == null) {
            // Get a new instance of the row layout view
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.report_list_item, null);

            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            rptView = new ReportView();
            // rptView.locatorCode = (TextView)
            // rowView.findViewById(R.id.locatorCode);
            rptView.status = (TextView) rowView.findViewById(R.id.status);
            // rptView.editDate = (TextView)
            // rowView.findViewById(R.id.editDate);
            rptView.recTime = (TextView) rowView.findViewById(R.id.recTime);
            rptView.reportType = (TextView) rowView.findViewById(R.id.reportType);
            rptView.reportTitle = (TextView) rowView.findViewById(R.id.reportTitle);
            rptView.eventDate = (TextView) rowView.findViewById(R.id.dateOfEvent);
            rptView.picCount = (TextView) rowView.findViewById(R.id.picCount);
            rptView.queuedStatusMssg = (TextView) rowView.findViewById(R.id.queuedStatusMssg);

            // Cache the view objects in the TAG,
            // so they can be re-accessed later
            rowView.setTag(rptView);
        } else {
            rptView = (ReportView) rowView.getTag();
        }

        // Transfer the report data from the data object
        // to the view objects
        ReportModel currentReport = (ReportModel) reports.get(position);

        // rptView.locatorCode.setText(currentReport.getLocatorCode()); //
        // obsolete

        // dynamically build the text views
        if (currentReport.getReportStatus().equals(Codes.RPT_STATUS_CURRENT)) {
            rptView.status.setText(Codes.RPT_STATUS_CURRENT);
            rptView.status.setTextColor(Codes.RPT_STATUS_CURRENT_LABEL_COLOR);
        } else if (currentReport.getReportStatus().equals(Codes.RPT_STATUS_QUEUED)) {
            rptView.status.setText(Codes.RPT_STATUS_QUEUED);
            rptView.status.setTextColor(Codes.RPT_STATUS_QUEUED_LABEL_COLOR);
        } else {
            rptView.status.setText(Codes.RPT_STATUS_TRANSMITTED);
            rptView.status.setTextColor(Codes.RPT_STATUS_TRANSMITTED_LABEL_COLOR);
        }

        // rptView.editDate.setText(currentReport.getEditDate()); // obsolete
        rptView.reportType.setText(currentReport.getReportType());
        rptView.reportTitle.setText(currentReport.getReportTitle());
        rptView.eventDate.setText(currentReport.getEventDate());

        if (currentReport.getRecTime() != null) {
            rptView.recTime.setText("( " + currentReport.getRecTime() + " )");
        } else {
            rptView.recTime.setText("( " + Codes.ZEROS_DEFAULT_TIME + " )");
        }
        rptView.picCount.setText("( " + currentReport.getPicCount() + " )");

        rptView.queuedStatusMssg.setText(null);

        if (currentReport.getReportStatus().equals(Codes.RPT_STATUS_QUEUED)) {
            // get the issue
            String queuedIssue = LocatorFileManager.getValue(this.context,
                    currentReport.getLocatorCode() + Codes.FILE_EXT_QUEUED,
                    Codes.LOCATOR_FILE_QUEUED_ISSUE_KEY);

            if (queuedIssue != null && !queuedIssue.equals("null")) {
                rptView.queuedStatusMssg.setText(queuedIssue);
            } else {
                // it must be transmitting
                rptView.queuedStatusMssg.setText(Codes.TRANS_QUEUED_ISSUE_SERVER_PROCESSING);
            }
        }

        return rowView;
    }

    protected static class ReportView {
        protected TextView locatorCode;
        protected TextView status;
        protected TextView editDate;
        protected TextView recTime;
        protected TextView reportType;
        protected TextView reportTitle;
        protected TextView eventDate;
        protected TextView picCount;
        protected TextView queuedStatusMssg;
    }

}
