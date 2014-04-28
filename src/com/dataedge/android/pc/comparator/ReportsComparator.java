package com.dataedge.android.pc.comparator;

import java.util.Comparator;
import java.util.Date;

import com.dataedge.android.pc.model.ReportModel;

// sorts ascending on the file extension or status letter, then on the
// event date in descending order
public class ReportsComparator implements Comparator<ReportModel> {
    @Override
    public int compare(ReportModel o1, ReportModel o2) {

        int result = o1.getFileExt().compareTo(o2.getFileExt());

        if (result == 0) {
            Date d1 = new Date(o1.getEditDate());
            Date d2 = new Date(o2.getEditDate());
            return d2.compareTo(d1);
        }

        return result;

    }
}
