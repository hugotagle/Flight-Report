package com.dataedge.android.pc.activity;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.R.id;
import com.dataedge.android.pc.R.layout;
import com.dataedge.android.pc.R.menu;
import com.dataedge.android.pc.manager.MediaFileManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoViewerActivity extends Activity {
    String TAG = PhotoViewerActivity.class.getName();
    String locatorCode = null;
    String mediaFile = null;
    ImageView view = null;
    String currentMediaFilename = null;
    String reportStatus = null;
    String locatorFileExt = null;
    String reportTitle = null;

    int pager = 0;
    int numberOfPhotos = 0;

    // navigation buttons
    ImageButton btnNextPic = null;;
    ImageButton btnPreviousPic = null;
    ImageButton btnDeletePic = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.photo_preview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_media_file, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            // disable menu buttons
            menu.getItem(0).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_delete_media_file:

            if (Utils.fileExists(getApplicationContext(), currentMediaFilename)) {
                Utils.removeFile(getApplicationContext(), currentMediaFilename);
                Toast.makeText(PhotoViewerActivity.this, "Picture file deleted", Toast.LENGTH_SHORT)
                        .show();
            }

            Intent myIntent = new Intent(getApplicationContext(), MediaListActivity.class);
            myIntent.putExtra("LOCATOR_CODE", locatorCode);
            myIntent.putExtra("REPORT_STATUS", reportStatus);
            myIntent.putExtra("FILE_EXT", locatorFileExt);
            myIntent.putExtra("REPORT_TITLE", reportTitle);
            startActivityForResult(myIntent, 0);
            break;

        }
        return true;

    }

    protected void onStart() {

        Log.i(TAG, "onStart()");
        super.onStart();

        // obtain the current filename
        currentMediaFilename = getIntent().getExtras().getString("MEDIA_FILE_NAME");
        locatorCode = getIntent().getExtras().getString("LOCATOR_CODE");
        locatorFileExt = getIntent().getExtras().getString("FILE_EXT");
        reportStatus = getIntent().getExtras().getString("REPORT_STATUS");
        reportTitle = getIntent().getExtras().getString("REPORT_TITLE");

        // number of photos for this report
        numberOfPhotos = MediaFileManager.getMediaFileCount(getApplicationContext(), locatorCode);
        pager = (getIntent().getExtras().getInt("PHOTO_POSITION") + 1);

        // display paging
        TextView picNum = (TextView) findViewById(R.id.txtPicNum);
        picNum.setText(pager + " of " + numberOfPhotos);

        // Toast.makeText(PhotoViewerActivity.this, "Pic #: " + pager,
        // Toast.LENGTH_SHORT).show();

        setNavigationButtons();

        // display the default pic
        displayImage(currentMediaFilename);

        //
        btnNextPic.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                pager++;
                currentMediaFilename = MediaFileManager.getNextFilename(getApplicationContext(),
                        currentMediaFilename);
                currentMediaFilename = currentMediaFilename + Codes.FILE_EXT_PHOTO;
                displayImage(currentMediaFilename);
            }
        });

        //
        btnPreviousPic.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                pager--;

                currentMediaFilename = MediaFileManager.getPreviousFilename(
                        getApplicationContext(), currentMediaFilename);
                currentMediaFilename = currentMediaFilename + Codes.FILE_EXT_PHOTO;
                displayImage(currentMediaFilename);
            }
        });

        if (!reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            btnDeletePic.setEnabled(false);
            btnDeletePic.setImageResource(R.drawable.trash_off);
        }

        //
        btnDeletePic.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {

                if (Utils.fileExists(getApplicationContext(), currentMediaFilename)) {
                    Utils.removeFile(getApplicationContext(), currentMediaFilename);
                    // removed png as well
                    Utils.removeFile(getApplicationContext(), currentMediaFilename.replace(
                            Codes.FILE_EXT_PHOTO, Codes.FILE_EXT_THUMB));
                    Toast.makeText(PhotoViewerActivity.this, "Picture file deleted",
                            Toast.LENGTH_SHORT).show();
                }

                Intent myIntent = new Intent(getApplicationContext(), MediaListActivity.class);
                myIntent.putExtra("LOCATOR_CODE", locatorCode);
                myIntent.putExtra("REPORT_STATUS", reportStatus);
                myIntent.putExtra("FILE_EXT", locatorFileExt);
                myIntent.putExtra("REPORT_TITLE", reportTitle);
                startActivityForResult(myIntent, 0);

            }
        });
    }

    private void displayImage(String mediaFile) {

        // Toast.makeText(PhotoViewerActivity.this, "Pic #: " + pager,
        // Toast.LENGTH_SHORT).show();

        // display paging
        TextView picNum = (TextView) findViewById(R.id.txtPicNum);
        picNum.setText(pager + " of " + numberOfPhotos);

        view = (ImageView) findViewById(R.id.test_image);
        String theFile = String.format(getFilesDir().getAbsolutePath() + "/%s", mediaFile);
        Bitmap bmImg = BitmapFactory.decodeFile(theFile);
        view.setImageBitmap(bmImg);

        // reset button
        setNavigationButtons();
    }

    // navigation button setup
    private void setNavigationButtons() {

        String fileQueuedLoc = MediaFileManager.getFilenameQueuedLocation(getApplicationContext(),
                currentMediaFilename);

        // default
        btnNextPic = (ImageButton) findViewById(R.id.btnNextPic);
        btnPreviousPic = (ImageButton) findViewById(R.id.btnPreviousPic);
        btnDeletePic = (ImageButton) findViewById(R.id.btnDeletePicture);

        if (fileQueuedLoc.equals(Codes.MEDIA_FILE_QUEUE_LOCATION_ONLY_ONE)) {
            btnPreviousPic.setImageResource(R.drawable.left_arrow_off);
            btnPreviousPic.setEnabled(false);
            btnNextPic.setImageResource(R.drawable.right_arrow_off);
            btnNextPic.setEnabled(false);

        } else if (fileQueuedLoc.equals(Codes.MEDIA_FILE_QUEUE_LOCATION_HIGHEST)) {
            btnPreviousPic.setImageResource(R.drawable.left_arrow_on);
            btnPreviousPic.setEnabled(true);
            btnNextPic.setImageResource(R.drawable.right_arrow_off);
            btnNextPic.setEnabled(false);

        } else if (fileQueuedLoc.equals(Codes.MEDIA_FILE_QUEUE_LOCATION_LOWEST)) {
            btnPreviousPic.setImageResource(R.drawable.left_arrow_off);
            btnPreviousPic.setEnabled(false);
            btnNextPic.setImageResource(R.drawable.right_arrow_on);
            btnNextPic.setEnabled(true);

        } else {
            btnPreviousPic.setImageResource(R.drawable.left_arrow_on);
            btnPreviousPic.setEnabled(true);
            btnNextPic.setImageResource(R.drawable.right_arrow_on);
            btnNextPic.setEnabled(true);
        }
    }
}
