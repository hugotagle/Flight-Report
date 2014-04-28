package com.dataedge.android.pc.activity;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PixelFormat;
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
import com.dataedge.android.pc.MediaFileArrayAdapter;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.R.id;
import com.dataedge.android.pc.R.layout;
import com.dataedge.android.pc.R.menu;
import com.dataedge.android.pc.manager.MediaFileManager;
import com.dataedge.android.pc.model.MediaFileModel;
import com.dataedge.android.pc.model.ReportModel;

public class MediaListActivity extends Activity {
    String TAG = MediaListActivity.class.getName();

    String locatorCode;
    String reportStatus;
    String locatorFileExt;
    String reportTitle;

    // list
    ListView listView = null;

    // reports
    List<MediaFileModel> listMediaFiles = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        setContentView(R.layout.media_files);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_media_file_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (MediaFileManager.isMediaFileQuotaMet(getApplicationContext(), locatorCode)
                || !reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            // camera
            menu.getItem(0).setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_camera_media_file_list:
            Intent myIntent1 = new Intent(getApplicationContext(), CameraActivity.class);
            if (locatorCode != null) {
                myIntent1.putExtra("LOCATOR_CODE", locatorCode);
                myIntent1.putExtra("REPORT_STATUS", reportStatus);
                myIntent1.putExtra("FILE_EXT", locatorFileExt);
                myIntent1.putExtra("REPORT_TITLE", reportTitle);
            }
            startActivityForResult(myIntent1, 0);
            break;

        }
        return true;

    }

    protected void onStart() {

        Log.i(TAG, "onStart()");
        super.onStart();

        locatorCode = getIntent().getExtras().getString("LOCATOR_CODE");
        reportStatus = getIntent().getExtras().getString("REPORT_STATUS");
        locatorFileExt = getIntent().getExtras().getString("FILE_EXT");
        reportTitle = getIntent().getExtras().getString("REPORT_TITLE");

        // open menu after a sec
        // new Handler().postDelayed(new Runnable() {
        // public void run() {
        // openOptionsMenu();
        // }
        // }, 1000);
        MediaFileManager mfm = new MediaFileManager(getApplicationContext());
        listMediaFiles = mfm.getMediaFiles(getIntent().getStringExtra("LOCATOR_CODE"), null);

        // populate the list view
        listView = (ListView) findViewById(R.id.media_list);
        listView.setAdapter(new MediaFileArrayAdapter(this, listMediaFiles));
        registerForContextMenu(listView);

        //
        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // reset locator code
                // obtain the clicked item
                MediaFileModel mf = (MediaFileModel) listMediaFiles.get(position);

                // new intent to this same activity
                Intent myIntent = new Intent(getApplicationContext(), PhotoViewerActivity.class);
                //
                myIntent.putExtra("PHOTO_POSITION", position);
                //
                myIntent.putExtra("LOCATOR_CODE", mf.getLocatorCode());
                myIntent.putExtra("MEDIA_FILE_NAME", mf.getFileName());
                myIntent.putExtra("FILE_EXT", getIntent().getStringExtra("FILE_EXT"));
                myIntent.putExtra("REPORT_STATUS", getIntent().getStringExtra("REPORT_STATUS"));
                myIntent.putExtra("REPORT_TITLE", getIntent().getStringExtra("REPORT_TITLE"));

                startActivityForResult(myIntent, 0);
            }
        });

        // camera button
        final ImageButton btnCamera = (ImageButton) findViewById(R.id.btnMediaFilesCamera);
        // only show if current status and quota hasn't been met
        if (MediaFileManager.isMediaFileQuotaMet(getApplicationContext(), locatorCode)
                || !reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            btnCamera.setVisibility(View.GONE);

        // camera button listener
        btnCamera.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                Intent myIntent = new Intent(getApplicationContext(), CameraActivity.class);
                if (locatorCode != null) {
                    myIntent.putExtra("LOCATOR_CODE", locatorCode);
                    myIntent.putExtra("REPORT_STATUS", reportStatus);
                    myIntent.putExtra("FILE_EXT", locatorFileExt);
                    myIntent.putExtra("REPORT_TITLE", reportTitle);
                }
                startActivityForResult(myIntent, 0);
            }
        });

        // home button
        final ImageButton btnHome = (ImageButton) findViewById(R.id.btnHome);
        btnHome.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                // restart Main activity
                Intent myIntent = new Intent(getApplicationContext(), MainMenuActivity.class);
                myIntent.putExtra("LOCATOR_CODE", locatorCode);
                myIntent.putExtra("REPORT_STATUS", reportStatus);
                myIntent.putExtra("FILE_EXT", locatorFileExt);
                myIntent.putExtra("REPORT_TITLE", reportTitle);

                startActivityForResult(myIntent, 0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // start report activity
        Intent myIntent = new Intent(getApplicationContext(), ReportActivity.class);
        myIntent.putExtra("LOCATOR_CODE", locatorCode);
        myIntent.putExtra("REPORT_STATUS", reportStatus);
        myIntent.putExtra("FILE_EXT", locatorFileExt);
        myIntent.putExtra("REPORT_TITLE", reportTitle);

        startActivityForResult(myIntent, 0);
    }

}
