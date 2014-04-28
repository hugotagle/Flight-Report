package com.dataedge.android.pc.activity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";

    SurfaceHolder mHolder;
    public Camera camera;
    private String locatorCode;
    private Context context;

    CameraPreview(Context context, String locatorCode) {
        super(context);

        this.context = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.locatorCode = locatorCode;
    }

    CameraPreview(Context context) {
        super(context);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        camera = Camera.open();
        try {
            camera.setPreviewDisplay(holder);

            camera.setPreviewCallback(new PreviewCallback() {

                public void onPreviewFrame(byte[] data, Camera arg1) {
//                    FileOutputStream outStream = null;
//                    try {
//                        String theFile = String.format(context.getFilesDir().getAbsolutePath()
//                                + "/%s.jpg", locatorCode);
//                        outStream = new FileOutputStream(theFile);
//                        outStream.write(data);
//                        outStream.close();
//                        Log.d(TAG, "onPreviewFrame - wrote bytes: " + data.length);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                    }
                    CameraPreview.this.invalidate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
//        camera.stopPreview();
        camera.release();
//        camera = null;
    }

    // public void surfaceChanged(SurfaceHolder holder, int format, int w, int
    // h) {
    // // Now that the size is known, set up the camera parameters and begin
    // // the preview.
    // Camera.Parameters parameters = camera.getParameters();
    // parameters.setPreviewSize(w, h);
    // camera.setParameters(parameters);
    // camera.startPreview();
    // }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        if (isPreviewRunning) {
//            camera.stopPreview();
//        }

        Camera.Parameters parameters = camera.getParameters();
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        //Context.getSystemService(Context.WINDOW_SERVICE) 

        if (display.getRotation() == Surface.ROTATION_0) {
            parameters.setPreviewSize(height, width);
            camera.setDisplayOrientation(90);
        }

        if (display.getRotation() == Surface.ROTATION_90) {
            parameters.setPreviewSize(width, height);
        }

        if (display.getRotation() == Surface.ROTATION_180) {
            parameters.setPreviewSize(height, width);
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            parameters.setPreviewSize(width, height);
            camera.setDisplayOrientation(180);
        }

        //camera.setParameters(parameters); // TODO this was the fix for HTC.. 
        parameters.setPreviewSize(width, height);

        camera.startPreview();
        // previewCamera();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p = new Paint(Color.RED);
        Log.d(TAG, "draw");
        canvas.drawText("PREVIEW", canvas.getWidth() / 2, canvas.getHeight() / 2, p);
    }
}