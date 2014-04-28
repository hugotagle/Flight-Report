package com.dataedge.android.pc.activity;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.ReportArrayAdapter;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.R.id;
import com.dataedge.android.pc.R.layout;
import com.dataedge.android.pc.R.menu;
import com.dataedge.android.pc.comparator.ReportsComparator;
import com.dataedge.android.pc.manager.LocatorFileManager;
import com.dataedge.android.pc.model.ReportModel;

public class FiledReportsActivity extends Activity {
    String TAG = FiledReportsActivity.class.getName();

    // delete dialog
    AlertDialog sentInfoAlert = null;
    // boolean queuedOnesExist = false;
    boolean queuedProcessingFailed = false;

    // list
    ListView listView = null;

    // reports
    List<ReportModel> listReports = null;

    // menu
    boolean disableMenuAddButton = false;
    boolean disableMenuSendQueuedButton = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        setContentView(R.layout.filedreports);

        AlertDialog.Builder sentInfoBuilder = new AlertDialog.Builder(this);
        sentInfoBuilder.setTitle("Info");
        sentInfoBuilder.setMessage(null).setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        //
        sentInfoAlert = sentInfoBuilder.create();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (disableMenuAddButton)
            menu.getItem(0).setEnabled(false);
        if (!disableMenuSendQueuedButton)
            menu.getItem(2).setEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = null;
        switch (item.getItemId()) {
        case R.id.menu_add_report:
            // Perform action on clicks
            myIntent = new Intent(getApplicationContext(), ReportActivity.class);
            myIntent.putExtra("FILE_EXT", Codes.FILE_EXT_CURRENT);

            startActivityForResult(myIntent, 0);
            break;
        case R.id.menu_settings:
            myIntent = new Intent(getApplicationContext(),
                    com.dataedge.android.pc.activity.SettingsActivity.class);
            startActivityForResult(myIntent, 0);
            break;

        case R.id.menu_send_queued:
            break;
        }
        return true;
    }

    protected void onStart() {

        Log.i(TAG, "onStart()");
        super.onStart();

        // open menu after a sec
        // new Handler().postDelayed(new Runnable() {
        // public void run() {
        // openOptionsMenu();
        // }
        // }, 1000);

        LocatorFileManager lfm = new LocatorFileManager(getApplicationContext());
        listReports = lfm.getReports(Codes.REPORT_LIST_ONLY_UNEDITABLE);

        if (listReports.size() > 0) {
            // no reports in queue. Info for new button should be enabled
            final TextView txtInfoNewButton = (TextView) findViewById(R.id.txtInfoNewButton);
            txtInfoNewButton.setVisibility(View.GONE);
        }

        // NO Longer needed since the Add is in the Main Menu
        /*
         * if (lfm.isCurrentAvailable()) { disableMenuAddButton = true; }
         */

        if (lfm.isQueuedAvailable() && Utils.isInternetAvailable(getApplicationContext())) {
            disableMenuSendQueuedButton = false;
        }

        // sort the reports
        Collections.sort(listReports, new ReportsComparator());

        // // populate the list view
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(new ReportArrayAdapter(this, listReports, getApplicationContext()));
        registerForContextMenu(listView);

        //
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // reset locator code
                // obtain the clicked item
                ReportModel rpt = (ReportModel) listReports.get(position);

                // new intent to this same activity
                Intent myIntent = new Intent(getApplicationContext(), ReportActivity.class);
                myIntent.putExtra("FILE_EXT", rpt.getFileExt());
                myIntent.putExtra("LOCATOR_CODE", rpt.getLocatorCode());
                myIntent.putExtra("REPORT_TITLE", rpt.getReportTitle());

                startActivityForResult(myIntent, 0);
            }
        });

        // home button
        final ImageButton btnHome = (ImageButton) findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                Intent myIntent = new Intent(getApplicationContext(),
                        com.dataedge.android.pc.activity.MainMenuActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // this is going to settings
        final ImageButton btnMyInformation = (ImageButton) findViewById(R.id.btnMyProfile);
        btnMyInformation.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                Intent myIntent = new Intent(view.getContext(),
                        com.dataedge.android.pc.activity.SettingsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // show toast if bundle contains information about a transmit or delete
        // from the reportActivity.
        if (getIntent().getStringExtra("TRANS_MSSG_SENT") != null) {
            Log.i(TAG, getIntent().getStringExtra("TRANS_MSSG_SENT"));
            if (getIntent().getStringExtra("TRANS_MSSG_SENT").equals(Codes.TRANS_MSG_NO_SERVER)
                    || getIntent().getStringExtra("TRANS_MSSG_SENT").equals(
                            Codes.TRANS_MSG_QUEUED_NO_SERVER)) {
                sentInfoAlert.setMessage(getIntent().getStringExtra("TRANS_MSSG_SENT"));
                sentInfoAlert.show();
            } else {
                // disable Send Queued button
                disableMenuSendQueuedButton = true;

                Toast.makeText(FiledReportsActivity.this,
                        getIntent().getStringExtra("TRANS_MSSG_SENT"), Toast.LENGTH_LONG).show();
            }

        }

        // location listener.
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // obtain best provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = lm.getBestProvider(criteria, true);

        lm.requestLocationUpdates(provider, 300000, 1000, new MyLocationListener());

    }

    class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {

            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart()");
        super.onRestart();

        if (getIntent().getStringExtra("TRANS_MSSG_SENT") != null)
            getIntent().removeExtra("TRANS_MSSG_SENT");

    }

    @Override
    public void onBackPressed() {

        Intent myIntent = new Intent(getApplicationContext(),
                com.dataedge.android.pc.activity.MainMenuActivity.class);
        startActivityForResult(myIntent, 0);
    }
}
