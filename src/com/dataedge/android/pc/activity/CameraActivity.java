package com.dataedge.android.pc.activity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.manager.MediaFileManager;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraDemo";
    Camera camera;
    CameraPreview preview;
    Button buttonClick;
    String locatorCode;
    String reportStatus;
    String locatorFileExt;
    String reportTitle;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.camera);
    }

    @Override
    public void onStart() {
        Log.i(TAG, "onStart()");

        super.onStart();
        locatorCode = getIntent().getExtras().getString("LOCATOR_CODE");
        reportStatus = getIntent().getExtras().getString("REPORT_STATUS");
        locatorFileExt = getIntent().getExtras().getString("FILE_EXT");
        reportTitle = getIntent().getExtras().getString("REPORT_TITLE");

        // preview = new Preview(this);
        preview = new CameraPreview(this, locatorCode);
        ((FrameLayout) findViewById(R.id.preview)).addView(preview);

        buttonClick = (Button) findViewById(R.id.buttonClick);
        buttonClick.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                buttonClick.setEnabled(false); // avoid

                Camera.Parameters parameters = preview.camera.getParameters();
                parameters.set("orientation", "portrait");
                parameters.set("rotation", 90);

                preview.camera.setParameters(parameters);
                preview.camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });

        Log.d(TAG, "onCreate'd");
    }

    protected void setDisplayOrientation(Camera camera, int angle) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        } catch (Exception e1) {
        }
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    /** Handles data for raw picture */
    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture */
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream outStream = null;
            try {
                // write file
                String filename = MediaFileManager.getNextAvailableFilename(
                        getApplicationContext(), locatorCode);
                filename = filename + Codes.FILE_EXT_PHOTO;
                String theFile = String.format(getFilesDir().getAbsolutePath() + "/%s", filename);
                outStream = new FileOutputStream(theFile);
                outStream.write(data);
                outStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);

                // try to write a thumbnail file for this
                // picture
                Bitmap bitmap = Utils.getThumbnail(theFile);
                String bitmapFilename = theFile.replace(Codes.FILE_EXT_PHOTO, Codes.FILE_EXT_THUMB);
                try {
                    FileOutputStream out = new FileOutputStream(bitmapFilename);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            Log.d(TAG, "onPictureTaken - jpeg");

            // wait a couple of seconds
            // try {
            // Thread.sleep(1000);
            // } catch (InterruptedException e) {
            // }

            if (MediaFileManager.isMediaFileQuotaMet(getApplicationContext(), locatorCode)) {
                // time to get out
                reStartMediaListActivity();
            } else {
                
                // do the camera again
                Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("LOCATOR_CODE", locatorCode);
                bundle.putString("REPORT_STATUS", reportStatus);
                bundle.putString("FILE_EXT", locatorFileExt);
                bundle.putString("REPORT_TITLE", reportTitle);
                i.putExtras(bundle);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                camera.stopPreview();
                camera.release();
                camera = null;

                startActivity(i);
            }

        }
    };

    @Override
    public void onBackPressed() {

        reStartMediaListActivity();
    }

    private void reStartMediaListActivity() {

        // start report activity
        Intent myIntent = new Intent(getApplicationContext(), MediaListActivity.class);
        myIntent.putExtra("LOCATOR_CODE", locatorCode);
        myIntent.putExtra("REPORT_STATUS", reportStatus);
        myIntent.putExtra("FILE_EXT", locatorFileExt);
        myIntent.putExtra("REPORT_TITLE", reportTitle);

        startActivityForResult(myIntent, 0);
    }
}
