package com.dataedge.android.pc.activity;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.QueuedTransmissionService;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.manager.LocatorFileManager;
import com.dataedge.android.pc.manager.MediaFileManager;

public class ReportActivity extends Activity implements OnClickListener, Runnable {

    String TAG = ReportActivity.class.getName();

    static final int DATE_DIALOG_ID = 0;
    static final int TIME_DIALOG_ID = 1;

    String locatorCode = null;
    String dftFileExt = null;
    String reportStatus = null;
    String locatorFileExt = null;
    String reportTitle = null;

    // delete dialog
    AlertDialog deleteReportAlert = null;
    // transmit dialog
    AlertDialog transmitReportAlert = null;

    // boolean deleteReportDialogClicked = false;
    boolean reportBeingTransmitted = false;
    private ProgressDialog pd;
    private String transmissionMssg;

    //
    CheckBox cbFlightType = null;
    CheckBox cbMaintType = null;
    //
    EditText etTitle = null;
    EditText etFlightNumber = null;
    EditText etTailNumber = null;

    //
    CheckBox cbBirdStrike = null;
    CheckBox cbDangerGood = null;

    //
    EditText etEventDt = null;
    EditText etEventTime = null;

    // menu
    boolean disableMenuSendButton = true;

    //
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");

        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.report_detail);

    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();

        resetMemoDuration();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_report_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (MediaFileManager.isNoneMediaFiles(getBaseContext(), locatorCode)) {
            // change Photos to Camera
            menu.getItem(0).setIcon(R.drawable.ic_menu_camera);
            menu.getItem(0).setTitle("Camera");
        }

        if (!reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            // change to rec play to play only
            menu.getItem(1).setIcon(R.drawable.ic_lock_silent_mode_off);
            menu.getItem(1).setTitle("Voice Playback");
        }

        if (!Utils.fileExists(getApplicationContext(), locatorCode + Codes.FILE_EXT_AUDIO)
                && !reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            // not opened report without an audio message file, disable
            menu.getItem(1).setEnabled(false);
        }

        if (!disableMenuSendButton)
            // send report menu option
            menu.getItem(2).setEnabled(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // save state anytime user
        // goes away from this activity
        saveScreenFieldsState();

        switch (item.getItemId()) {
        // case R.id.menu_home:
        // reStartMainActivity(null);
        // break;
        case R.id.menu_media_list:
            Intent myIntent = null;
            if (MediaFileManager.isNoneMediaFiles(getApplicationContext(), locatorCode)) {
                // go straight to camera
                myIntent = new Intent(getApplicationContext(), CameraActivity.class);
            } else {
                // go to media list view
                myIntent = new Intent(getApplicationContext(), MediaListActivity.class);
            }
            myIntent.putExtra("LOCATOR_CODE", locatorCode);
            myIntent.putExtra("REPORT_STATUS", reportStatus);
            myIntent.putExtra("FILE_EXT", locatorFileExt);
            myIntent.putExtra("REPORT_TITLE", reportTitle);

            startActivityForResult(myIntent, 0);
            break;
        case R.id.menu_delete_report:
            this.deleteReportAlert.show();
            break;
        case R.id.menu_send_report:
            this.transmitReportAlert.show();
            break;
        case R.id.menu_rec_play:
            Intent myIntent3 = new Intent(getApplicationContext(), AudioActivity.class);
            // this one can be removed if new report gets submitted
            if (locatorCode != null) {
                myIntent3.putExtra("LOCATOR_CODE", locatorCode);
                myIntent3.putExtra("REPORT_STATUS", reportStatus);
            }
            startActivityForResult(myIntent3, 0);
            break;
        }
        return true;
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart()");

        super.onStart();

        // get
        if (getIntent().getStringExtra("FILE_EXT") != null)
            locatorFileExt = getIntent().getStringExtra("FILE_EXT");

        // if locatorCode empty generate new one
        if (getIntent().getStringExtra("LOCATOR_CODE") != null) {
            locatorCode = getIntent().getStringExtra("LOCATOR_CODE");
        } else {

            // generate
            locatorCode = Utils.generateLocatorCode(getApplicationContext());
            // put locatorCode as in extra in case of an orientation change
            getIntent().putExtra("LOCATOR_CODE", locatorCode);
            try {
                // create current file since the locator code
                // was just generated
                Utils.createFile(getApplicationContext(), locatorCode, Codes.FILE_EXT_CURRENT);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // enable transmit button
            enableTransmitButton();
        }
        // title
        reportTitle = getIntent().getStringExtra("REPORT_TITLE");
        TextView title = (TextView) findViewById(R.id.txtReportTitle);
        title.setText(reportTitle);

        // dynamically build the text views
        TextView status = (TextView) findViewById(R.id.status);

        // derive reportStatus
        // only two ways to get here. Both set the FILE_EXT accordingly
        Log.i(TAG, "----" + locatorFileExt);
        if (locatorFileExt != null) {
            if (locatorFileExt.contains(Codes.FILE_EXT_CURRENT)) {
                reportStatus = Codes.RPT_STATUS_CURRENT;
                // enable transmit button
                enableTransmitButton();
            } else if (locatorFileExt.contains(Codes.FILE_EXT_QUEUED)) {
                reportStatus = Codes.RPT_STATUS_QUEUED;
            } else {
                reportStatus = Codes.RPT_STATUS_TRANSMITTED;
            }
            status.setText(reportStatus);
            getIntent().putExtra("ORIENTATION_STATUS", reportStatus);
        }

        // home button
        final ImageButton btnHome = (ImageButton) findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                // restart Main activity
                reStartMainActivity(null);
            }
        });

        // delete button
        final ImageButton btnDelete = (ImageButton) findViewById(R.id.btnDeleteReport);
        btnDelete.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                deleteReportAlert.show();
            }
        });

        // send button
        final ImageButton btnSend = (ImageButton) findViewById(R.id.btnSendReport);

        // last send attempt
        String lastAttemptDate = LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_LAST_SEND_ATTEMPT_KEY);

        if (reportStatus != null && reportStatus.equals(Codes.RPT_STATUS_QUEUED)) {
            // show rlBanner
            RelativeLayout rlBanner = (RelativeLayout) findViewById(R.id.rlBanner);

            rlBanner.setVisibility(View.VISIBLE);
            // shows sending related message
            TextView txtLastAttemptDate = (TextView) findViewById(R.id.txtLastAttemptDate);
            if (lastAttemptDate == null || lastAttemptDate.equals("null")) {
                txtLastAttemptDate.setText(Codes.TRANS_QUEUED_ISSUE_SERVER_PROCESSING);
                // disable delete and send report
                btnSend.setEnabled(false);
                btnSend.setImageResource(R.drawable.send_off);

                btnDelete.setEnabled(false);
                btnDelete.setImageResource(R.drawable.trash_off);
            } else {
                txtLastAttemptDate.setText("Failed Send: " + lastAttemptDate);
            }
        }

        // do not enable send button if report is already transmitted or the
        // last attempt date
        // is null. Null attempt date signifies that report is being loaded
        if (reportStatus.equals(Codes.RPT_STATUS_TRANSMITTED)
                || (reportStatus.equals(Codes.RPT_STATUS_QUEUED) && lastAttemptDate == null)) {
            btnSend.setEnabled(false);
            btnSend.setImageResource(R.drawable.send_off);
        }

        btnSend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                transmitReportAlert.show();
            }
        });

        // Audio Recording Buttons OnClickLiteners
        final ImageButton btnRecPlay = (ImageButton) findViewById(R.id.btnRecPlay);
        if (!reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            // change to speaker if recording is not allowed based on status
            if (Utils.fileExists(getApplicationContext(), locatorCode + Codes.FILE_EXT_AUDIO)) {
                btnRecPlay.setImageResource(R.drawable.speaker);
            } else {
                btnRecPlay.setImageResource(R.drawable.speaker_off);
                btnRecPlay.setEnabled(false);
            }
        }
        btnRecPlay.setOnClickListener(this);

        // Camera Buttons OnClickLiteners
        final ImageButton btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        if (!MediaFileManager.isNoneMediaFiles(getBaseContext(), locatorCode)) {
            // camera to photo_list
            btnCamera.setImageResource(R.drawable.photo_list);
        } else {
            // no files. If ! current diable
            if (!reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
                btnCamera.setImageResource(R.drawable.photo_list_off);
                btnCamera.setEnabled(false);
            }
        }
        btnCamera.setOnClickListener(this);
        
        // location button
        final ImageButton btnLocation = (ImageButton) findViewById(R.id.btnLocationBase);
        btnLocation.setOnClickListener(this);
        

        //
        cbFlightType = (CheckBox) findViewById(R.id.cbFlightType);
        cbFlightType.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it's now
                // checked
                if (((CheckBox) v).isChecked()) {
                    cbMaintType.setChecked(false);
                } else {
                    cbMaintType.setChecked(true);
                }
            }
        });

        //
        cbMaintType = (CheckBox) findViewById(R.id.cbMaintType);
        cbMaintType.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Perform action on clicks, depending on whether it's now
                // checked
                if (((CheckBox) v).isChecked()) {
                    cbFlightType.setChecked(false);
                } else {
                    cbFlightType.setChecked(true);
                }
            }
        });

        // default - tw ones are already gone
        etTitle = (EditText) findViewById(R.id.etTitle);
        etFlightNumber = (EditText) findViewById(R.id.etFlightNum);
        // *** debug **** etFlightNumber.setHint(locatorCode);
        etTailNumber = (EditText) findViewById(R.id.etTailNum);

        cbBirdStrike = (CheckBox) findViewById(R.id.cbBirdStrike);
        cbDangerGood = (CheckBox) findViewById(R.id.cbDangerGood);

        etEventDt = (EditText) findViewById(R.id.etEventDt);
        etEventDt.setClickable(true);
        etEventDt.setOnClickListener(this);

        etEventTime = (EditText) findViewById(R.id.etEventTime);
        etEventTime.setClickable(true);
        etEventTime.setOnClickListener(this);

        resetScreenFields();

        // dialog for Transmitting report
        String trbTitle = "Send";
        String trbMessage = "You will no longer be able to apply changes to this report. Continue?";

        if (reportStatus.equals(Codes.RPT_STATUS_QUEUED)) {
            trbTitle = "Resend";
            trbMessage = "Resending this report might find the same conditions encountered during last try. Continue?";
        }

        AlertDialog.Builder transmitReportBuilder = new AlertDialog.Builder(this);
        transmitReportBuilder.setTitle(trbTitle);
        transmitReportBuilder.setMessage(trbMessage).setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // save state
                        saveScreenFieldsState();

                        if (reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
                            // this is a current file. queue it
                            Log.i(TAG, "renaming to Q");
                            Utils.renameFile(Codes.DIR_APP_FILES + locatorCode
                                    + Codes.FILE_EXT_CURRENT, Codes.DIR_APP_FILES + locatorCode
                                    + Codes.FILE_EXT_QUEUED);

                            // change parameters
                            reportStatus = Codes.RPT_STATUS_QUEUED;
                            locatorFileExt = Codes.FILE_EXT_QUEUED;
                        }

                        // start intent service
                        Intent msgIntent = new Intent(getApplicationContext(),
                                QueuedTransmissionService.class);
                        startService(msgIntent);

                        // go back
                        reStartMainActivity(null);

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        transmitReportAlert = transmitReportBuilder.create();

        // dialog for deleting report
        AlertDialog.Builder deleteReportBuilder = new AlertDialog.Builder(this);
        deleteReportBuilder.setTitle("Delete");
        deleteReportBuilder
                .setMessage(
                        "This will delete any media files associated with this report. Continue?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        pd = ProgressDialog.show(ReportActivity.this, "", "Deleting " + locatorCode
                                + " ...", true);
                        Thread thread = new Thread(ReportActivity.this);
                        thread.start();

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // final AlertDialog deleteReportAlert = deleteReportBuilder.create();
        deleteReportAlert = deleteReportBuilder.create();

        resetMemoDuration();

        // status frame
        FrameLayout fl = (FrameLayout) findViewById(R.id.statusFrame);
        if (reportStatus != null && reportStatus.equals(Codes.RPT_STATUS_QUEUED)) {
            fl.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.queued_status_frame_background));
        } else if (reportStatus != null && reportStatus.equals(Codes.RPT_STATUS_TRANSMITTED)) {
            fl.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.transmitted_status_frame_background));
        } else {
            fl.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.current_status_frame_background));
        }

        // pic count
        TextView picCount = (TextView) findViewById(R.id.txtPicCount);
        picCount.setText("Pics ( "
                + MediaFileManager.getMediaFileCount(getApplicationContext(), locatorCode) + " )");

    }

    
    protected void onPause() {
        super.onPause();
        
        if (reportStatus != null
                && getIntent().getStringExtra("ORIENTATION_STATUS")
                        .equals(Codes.RPT_STATUS_CURRENT))
            saveScreenFieldsState(); // save state
    }

    // saves state on orientation change
    // protected void onDestroy() {
    // Log.i(TAG, "****** onDestroy()");
    // Log.i(TAG, "reportStatus=" +
    // getIntent().getStringExtra("ORIENTATION_STATUS"));
    // super.onDestroy();
    //
    // if (reportStatus != null
    // && getIntent().getStringExtra("ORIENTATION_STATUS")
    // .equals(Codes.RPT_STATUS_CURRENT))
    // saveScreenFieldsState(); // save state
    //
    // }

    // @Override
    // public void onConfigurationChanged(Configuration newConfig) {
    // super.onConfigurationChanged(newConfig);
    //
    //
    // }

    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar c = Calendar.getInstance();
        int cyear = c.get(Calendar.YEAR);
        int cmonth = c.get(Calendar.MONTH);
        int cday = c.get(Calendar.DAY_OF_MONTH);
        switch (id) {
        case DATE_DIALOG_ID: {
            return new DatePickerDialog(this, mDateSetListener, cyear, cmonth, cday);
        }
        case TIME_DIALOG_ID: {
            return new TimePickerDialog(this, mTimeSetListener, 0, 0, false);
        }
        }
        return null;
    }

    // DatePicker Dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        // onDateSet method
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String month = String.valueOf(monthOfYear + 1);
            String day = String.valueOf(dayOfMonth);
            String date_selected = (month.length() == 1 ? "0" + month : month) + "/"
                    + (day.length() == 1 ? "0" + day : day) + "/"
                    + String.valueOf(year).substring(2);
            etEventDt.setText(date_selected);
        }
    };

    // TimePicker Dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            String meridian = "PM";
            String hour = null;

            if (hourOfDay > 12) {
                hourOfDay = hourOfDay - 12;
                hour = String.valueOf(hourOfDay);
            } else if (hourOfDay == 12) {
                hour = new String("12");
            } else if (hourOfDay == 0) {
                hour = new String("12");
                meridian = "AM";
            } else {
                hour = String.valueOf(hourOfDay);
                meridian = "AM";
            }

            String min = String.valueOf(minute);
            String time_selected = (hour.length() == 1 ? "0" + hour : hour) + ":"
                    + (min.length() == 1 ? "0" + min : min) + " " + meridian;
            etEventTime.setText(time_selected);

        }
    };

    public void run() {

        // delete all related files
        Utils.deleteLocatorCodeRelatedFiles(getApplicationContext(), locatorCode);

        // null out locator code
        reportStatus = null;

        handler.sendEmptyMessage(0);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            pd.dismiss();

            //
            Log.i(TAG, "Trans Mssg:" + transmissionMssg);
            // restart Main activity
            reStartMainActivity("Report was deleted");

        }
    };

    @Override
    public void onClick(View v) {
        //
        switch (v.getId()) {
        case R.id.etEventDt: {
            showDialog(DATE_DIALOG_ID);
            break;
        }
        case R.id.etEventTime: {
            showDialog(TIME_DIALOG_ID);
            break;
        }
        case R.id.btnRecPlay: {
            Intent myIntent = new Intent(getApplicationContext(), AudioActivity.class);
            // this one can be removed if new report gets submitted
            if (locatorCode != null) {
                myIntent.putExtra("LOCATOR_CODE", locatorCode);
                myIntent.putExtra("REPORT_STATUS", reportStatus);
            }
            startActivityForResult(myIntent, 0);
            break;
        }
        case R.id.btnLocationBase: {
            Intent myIntent = new Intent(getApplicationContext(), LocationMapActivity.class);
            // this one can be removed if new report gets submitted
            if (locatorCode != null) {
                myIntent.putExtra("LOCATOR_CODE", locatorCode);
                myIntent.putExtra("REPORT_STATUS", reportStatus);
                myIntent.putExtra("FILE_EXT", locatorFileExt);
                myIntent.putExtra("REPORT_TITLE", reportTitle);
            }
            startActivityForResult(myIntent, 0);
            break;
        }
        case R.id.btnCamera:

            // save state
            saveScreenFieldsState();

            // figure out where to go
            Intent myIntent = null;
            if (!MediaFileManager.isNoneMediaFiles(getBaseContext(), locatorCode)) {
                // change to media list activity
                myIntent = new Intent(getApplicationContext(), MediaListActivity.class);
            } else {
                // go to camera
                myIntent = new Intent(getApplicationContext(), CameraActivity.class);
            }
            if (locatorCode != null) {
                myIntent.putExtra("LOCATOR_CODE", locatorCode);
                myIntent.putExtra("REPORT_STATUS", reportStatus);
                myIntent.putExtra("REPORT_TITLE", getIntent().getStringExtra("REPORT_TITLE"));
                myIntent.putExtra("FILE_EXT", locatorFileExt);
            }
            startActivityForResult(myIntent, 0);
            break;
        }
    }

    @Override
    public void onBackPressed() {

        // restart Main activity
        reStartMainActivity(null);
    }

    // restarts the Main activity
    // msg for whether a transmission or queuing took place
    private void reStartMainActivity(String msg) {

        // either back button, home button
        if (reportStatus != null && reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {

            saveScreenFieldsState();

        }

        // start Main Menu
        Intent myIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
        if (msg != null)
            myIntent.putExtra("TRANS_MSSG_SENT", msg);

        myIntent.removeExtra("FILE_EXT");
        startActivityForResult(myIntent, 0);

    }

    // saves fields value in the locator code file
    private void saveScreenFieldsState() {

        if (cbFlightType.isChecked()) {
            insertReportType(Codes.RPT_TYPE_FLIGHT);
        } else {
            insertReportType(Codes.RPT_TYPE_MAINTENANCE);
        }

        if (etTitle.getText() != null && !etTitle.getText().equals(""))
            insertTitle(etTitle.getText().toString());

        if (etEventDt.getText() != null && !etEventDt.getText().equals(""))
            insertEventDate(etEventDt.getText().toString());

        if (etEventTime.getText() != null && !etEventTime.getText().equals(""))
            insertEventTime(etEventTime.getText().toString());

        if (etFlightNumber.getText() != null && !etFlightNumber.getText().equals(""))
            insertFlightNumber(etFlightNumber.getText().toString());

        if (etTailNumber.getText() != null && !etTailNumber.getText().equals(""))
            insertTailNumber(etTailNumber.getText().toString());

        if (cbBirdStrike.isChecked()) {
            insertBirdStrike("true");
        } else {
            insertBirdStrike("false");
        }

        if (cbDangerGood.isChecked()) {
            insertDangerGood("true");
        } else {
            insertDangerGood("false");
        }
    }

    // resets all screen fields
    private void resetScreenFields() {

        String type = LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_REPORT_TYPE_KEY);
        if (type != null && type.equals(Codes.RPT_TYPE_FLIGHT)) {
            cbFlightType.setChecked(true);
            cbMaintType.setChecked(false);
        }
        if (type != null && type.equals(Codes.RPT_TYPE_MAINTENANCE)) {
            cbFlightType.setChecked(false);
            cbMaintType.setChecked(true);
        }

        etTitle.setText(LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_TITLE_KEY));

        String eventDt = LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_EVENT_DATE_KEY);

        if (eventDt != null) {
            // it was already set
            etEventDt.setText(eventDt);
            // by default time was already set as well
            etEventTime.setText(LocatorFileManager.getValue(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_EVENT_TIME_KEY));
        } else {
            // set the current date
            DateFormat dateFormat = new SimpleDateFormat(Codes.DATE_FORMAT);
            Date date = new Date(System.currentTimeMillis());
            etEventDt.setText(dateFormat.format(date).toString()
                    .substring(0, dateFormat.format(date).toString().indexOf(" ")));
            etEventTime.setText(dateFormat.format(date).toString()
                    .substring(dateFormat.format(date).toString().indexOf(" ") + 1));
        }
        etFlightNumber.setText(LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_FLIGHT_NUM_KEY));
        etTailNumber.setText(LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_TAIL_NUM_KEY));

        String birdStrike = LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_BIRD_STRIKE_KEY);
        if (birdStrike != null && birdStrike.equals("true")) {
            cbBirdStrike.setChecked(true);
        }

        String dangerGood = LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + locatorFileExt, Codes.LOCATOR_FILE_DANGEROUS_GOOD_KEY);
        if (dangerGood != null && dangerGood.equals("true")) {
            cbDangerGood.setChecked(true);
        }

        if (reportStatus != null && !reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            cbFlightType.setEnabled(false);
            etEventDt.setEnabled(false);
            etEventTime.setEnabled(false);
            cbMaintType.setEnabled(false);
            etTitle.setEnabled(false);
            etFlightNumber.setEnabled(false);
            etTailNumber.setEnabled(false);
            cbBirdStrike.setEnabled(false);
            cbDangerGood.setEnabled(false);

        }
    }

    // resets the memo duration
    private void resetMemoDuration() {
        // Log.i(TAG, "resetMemoDuration()");

        // Memo duration
        final TextView txtMemoDuration = (TextView) findViewById(R.id.txtMemoDuration);
        String tmd = LocatorFileManager.getMemoDuration(getApplicationContext(), locatorCode
                + locatorFileExt);
        if (tmd != null)
            txtMemoDuration.setText("Rec ( " + tmd + " )");

    }

    private void enableTransmitButton() {
        // enable transmit
        disableMenuSendButton = false;
    }

    private void insertReportType(String type) {
        // Log.i(TAG, "insertReportType(" + type + ")");

        // type
        if (reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_REPORT_TYPE_KEY, type);
    }

    private void insertTitle(String title) {
        // Log.i(TAG, "insertTitle(" + title + ")");

        // title
        if (title != null && reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_TITLE_KEY, title);
    }

    private void insertEventDate(String eventDate) {
        // Log.i(TAG, "insertEventDate(" + eventDate + ")");

        // event date
        if (eventDate != null && reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_EVENT_DATE_KEY, eventDate);
    }

    private void insertEventTime(String eventTime) {
        // Log.i(TAG, "insertEventTime(" + eventTime + ")");

        // event date
        if (eventTime != null && reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_EVENT_TIME_KEY, eventTime);
    }

    private void insertFlightNumber(String flightNumber) {
        // Log.i(TAG, "insertFlighNumber(" + flightNumber + ")");

        // flight number
        if (flightNumber != null && reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_FLIGHT_NUM_KEY, flightNumber);
    }

    private void insertTailNumber(String tailNumber) {
        // Log.i(TAG, "insertTailNumber(" + tailNumber + ")");

        // tail number
        if (tailNumber != null && reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_TAIL_NUM_KEY, tailNumber);
    }

    private void insertBirdStrike(String birdStrike) {
        // Log.i(TAG, "insertBirdStrike(" + birdStrike + ")");

        // bird strike
        if (reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_BIRD_STRIKE_KEY, birdStrike);
    }

    private void insertDangerGood(String dangerGood) {
        // Log.i(TAG, "insertDangerGood(" + dangerGood + ")");

        // danger good
        if (reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + locatorFileExt, Codes.LOCATOR_FILE_DANGEROUS_GOOD_KEY, dangerGood);
    }

}
