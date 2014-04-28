package com.dataedge.android.pc.activity;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.manager.LocatorFileManager;
import com.dataedge.android.pc.model.ReportModel;

public class MainMenuActivity extends Activity {
    String TAG = MainMenuActivity.class.getName();
    
    LocatorFileManager lfm = null;
    List<ReportModel> listReports = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main_menu);
    }

    protected void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();
        
//        lfm = new LocatorFileManager(getApplicationContext());
//        listReports = lfm.getReports(null);       
    }
    
//    protected void onPause() {
//        Log.i(TAG, "onPause()");
//        super.onPause();
//        
//        lfm = null;
//        listReports = null;
//        
////        lfm = new LocatorFileManager(getApplicationContext());
////        listReports = lfm.getReports(null);       
//    }
    
    protected void onStart() {

        Log.i(TAG, "onStart()");
        super.onStart();

        // turn new report button on/off
        lfm = new LocatorFileManager(getApplicationContext());
        listReports = lfm.getReports(null);
        
        // button to report
        final Button btnMainMenuReport = (Button) findViewById(R.id.btnMainMenuReport);
        if (lfm.isCurrentAvailable()) {
            // there's a current report
            btnMainMenuReport.setText("Edit");
        }
        
        // *** debugging ***
        ReportModel reportModel = null;
        Iterator iterator = listReports.iterator();
        String reports = "";
        while (iterator.hasNext()) {
            reportModel = (ReportModel) iterator.next();
            reports = reports + reportModel.getLocatorCode() + reportModel.getFileExt() + "\n";
        }
        //Toast.makeText(MainMenuActivity.this, reports, Toast.LENGTH_SHORT).show();
        // *** ***
        
        // this is going to report
        btnMainMenuReport.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), ReportActivity.class);

                myIntent.putExtra("FILE_EXT", Codes.FILE_EXT_CURRENT);
                
                // Perform action on clicks
                if (lfm.isCurrentAvailable()) {
                    myIntent.putExtra("LOCATOR_CODE", lfm.getCurrentLocatorCodeId());
                    myIntent.putExtra("REPORT_TITLE", Utils.getTitleForDisplay(lfm.getCurrentTitle()));
                }
                startActivityForResult(myIntent, 0);
            }
        });

        // this is going to queue
        final Button btnMainMenuQueue = (Button) findViewById(R.id.btnMainMenuQueue);
        btnMainMenuQueue.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                Intent myIntent = new Intent(view.getContext(),
                        com.dataedge.android.pc.activity.FiledReportsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // this is going to settings
        final Button btnMainMenuSettings = (Button) findViewById(R.id.btnMainMenuSettings);
        btnMainMenuSettings.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                Intent myIntent = new Intent(view.getContext(),
                        com.dataedge.android.pc.activity.SettingsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });
    }

}
