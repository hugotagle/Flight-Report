package com.dataedge.android.pc;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import com.dataedge.android.pc.manager.LocatorFileManager;
import com.dataedge.android.pc.model.ReportModel;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class QueuedTransmissionService extends IntentService {

    String TAG = QueuedTransmissionService.class.getName();

    public QueuedTransmissionService() {
        super("QueuedTranmissionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent(Intent intent)");

        boolean isInternetAvailable = true;
        boolean isTransmitted = false;

        // see if internet is available
        if (!Utils.isInternetAvailable(getApplicationContext()))
            isInternetAvailable = false;

        LocatorFileManager lfm = new LocatorFileManager(getApplicationContext());

        List<ReportModel> queued = lfm.getReports(Codes.REPORT_LIST_ONLY_QUEUED);

        Iterator<ReportModel> iterator = queued.iterator();

        while (iterator.hasNext()) {
            ReportModel rm = iterator.next();

            // null out last send attempt date
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), rm.getLocatorCode()
                    + Codes.FILE_EXT_QUEUED, Codes.LOCATOR_FILE_LAST_SEND_ATTEMPT_KEY, null);

            // null out last transmission result (issue)
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), rm.getLocatorCode()
                    + Codes.FILE_EXT_QUEUED, Codes.LOCATOR_FILE_QUEUED_ISSUE_KEY, null);

            if (isInternetAvailable) {
                Log.i(TAG, "trying transmission");
                // try to transmit
                if (!Transmitter.transmit(getApplicationContext(), rm.getLocatorCode(),
                        Codes.RPT_STATUS_QUEUED)) {
                    // something went wrong
                    Log.i(TAG, "adding");
                    LocatorFileManager.insertKeyValuePair(getApplicationContext(),
                            rm.getLocatorCode() + Codes.FILE_EXT_QUEUED,
                            Codes.LOCATOR_FILE_QUEUED_ISSUE_KEY,
                            Codes.TRANS_QUEUED_ISSUE_SERVER_UNAVAILABLE);
                } else {
                    // all good
                    Log.i(TAG, "Turn into a Transmission File");
                    // set locator file to transmitted
                    Utils.renameFile(Codes.DIR_APP_FILES + rm.getLocatorCode()
                            + Codes.FILE_EXT_QUEUED, Codes.DIR_APP_FILES + rm.getLocatorCode()
                            + Codes.FILE_EXT_TRANSMITTED);

                    isTransmitted = true;
                }
            } else {
                // no internet
                LocatorFileManager.insertKeyValuePair(getApplicationContext(), rm.getLocatorCode()
                        + Codes.FILE_EXT_QUEUED, Codes.LOCATOR_FILE_QUEUED_ISSUE_KEY,
                        Codes.TRANS_QUEUED_ISSUE_NO_INTERNET);
            }

            // set last send attempt date-time
            DateFormat dateFormat = new SimpleDateFormat(Codes.DATE_FORMAT);
            Date date = new Date(System.currentTimeMillis());

            if (!isTransmitted) {
                Log.i(TAG, "Transmission Failed");
                // update the last send attempt in the queued file
                LocatorFileManager.insertKeyValuePair(getApplicationContext(), rm.getLocatorCode()
                        + Codes.FILE_EXT_QUEUED, Codes.LOCATOR_FILE_LAST_SEND_ATTEMPT_KEY,
                        dateFormat.format(date).toString());
            }
        }
    }

}
