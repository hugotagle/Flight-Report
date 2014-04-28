package com.dataedge.android.pc.activity;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.manager.LocatorFileManager;

public class AudioActivity extends Activity implements OnClickListener {

    String TAG = AudioActivity.class.getName();

    private static final String AUDIO_STATUS_IDLE = "idle";
    private static final String AUDIO_STATUS_RECORDING = "recording";
    private static final String AUDIO_STATUS_PLAYBACK = "playback";

    String locatorCode = null;
    String reportStatus = null;
    MediaRecorder recorder;
    MediaPlayer player;

    //
    String audioStatus;

    //
    Chronometer chrono;
    long recordingMillis;

    //
    Button btnRecord = null;
    Button btnStop = null;
    Button btnPlay = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.audio);
    }

    @Override
    public void onStart() {
        super.onStart();
        // set extras
        locatorCode = getIntent().getStringExtra("LOCATOR_CODE");
        reportStatus = getIntent().getStringExtra("REPORT_STATUS");

        // set audio status
        audioStatus = AUDIO_STATUS_IDLE;

        final TextView title = (TextView) findViewById(R.id.audioTitle);
        title.setText("Voice Record");

        if (!reportStatus.equals(Codes.RPT_STATUS_CURRENT))
            title.setText("Playback");

        // send 
        final ImageButton btnClose = (ImageButton) findViewById(R.id.btnCloseAudioRecord);
        btnClose.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                stopRecorder();
                // stopping playback thread
                if (audioStatus.equals(AUDIO_STATUS_PLAYBACK))
                    audioStatus = AUDIO_STATUS_IDLE;
                stopPlayer();
                finish();
            }
        });

        // initialize the chronometer
        chrono = (Chronometer) findViewById(R.id.chronometer);

        // media buttons
        btnRecord = (Button) findViewById(R.id.btnRecord);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnPlay = (Button) findViewById(R.id.btnPlay);

        // initialize the click listeners for the media buttons
        btnRecord.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPlay.setOnClickListener(this);

        // setup record player buttons
        if (Utils.fileExists(getApplicationContext(), locatorCode + Codes.FILE_EXT_AUDIO)) {
            // a recording exists
            if (reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
                // recording can be changed
                //
                // record button
                btnRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_on_b, 0, 0);
                btnRecord.setEnabled(true);
                // stop button
                btnStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_off_b, 0, 0);
                btnStop.setEnabled(false);
                // play button
                btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play_on_b, 0, 0);
                btnPlay.setEnabled(true);
            } else {
                // no more changes. show only the player play green and disable
                // recording
                btnRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_off_b, 0, 0);
                btnRecord.setEnabled(false);
                // stop button
                btnStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_off_b, 0, 0);
                btnStop.setEnabled(false);
                // play button
                btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play_on_b, 0, 0);
                btnPlay.setEnabled(true);
            }
        }

    }

    // mimics processing as if the stop button was pressed.
    // If audio status is either playback or recording, it triggers
    // the same changes as the stop button would.
    protected void onPause() {
        super.onPause();

        if (!audioStatus.equals(AUDIO_STATUS_IDLE)) {
            stopButtonProcessing();
        }

    }

    // mimics processing as if the stop button was pressed.
    // If audio status is either playback or recording, it triggers
    // the same changes as the stop button would.
    protected void onDestroy() {
        super.onDestroy();

//        if (!audioStatus.equals(AUDIO_STATUS_IDLE)) {
//            stopButtonProcessing();
//        }

    }
    @Override
    public void onClick(View v) {
        //
        switch (v.getId()) {
        case R.id.btnRecord: {

            // disable close button
            final ImageButton btnClose = (ImageButton) findViewById(R.id.btnCloseAudioRecord);
            btnClose.setEnabled(false);
            btnClose.setImageResource(R.drawable.close_off);

            // set the recording event to true
            audioStatus = AUDIO_STATUS_RECORDING;

            chrono.setBase(SystemClock.elapsedRealtime());
            chrono.start();

            // record button
            btnRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_off_b, 0, 0);
            btnRecord.setEnabled(false);

            // stop button
            btnStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_on_b, 0, 0);
            btnStop.setEnabled(true);

            // play button
            btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play_off_b, 0, 0);
            btnPlay.setEnabled(false);

            if (recorder == null) {
                recorder = new MediaRecorder();
            }

            String pathForAppFiles = getFilesDir().getAbsolutePath();
            pathForAppFiles += "/" + locatorCode + Codes.FILE_EXT_AUDIO;

            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setOutputFile(pathForAppFiles);

            try {
                recorder.prepare();
                recorder.start();
            } catch (Exception e) {
                Log.e("Audio", "Failed to prepare and start audio recording", e);
            }

            break;
        }
        case R.id.btnStop: {

            stopButtonProcessing();

            break;
        }
        case R.id.btnPlay: {

            // so playback thread can start
            audioStatus = AUDIO_STATUS_PLAYBACK;

            // play button
            btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play_off_b, 0, 0);
            btnPlay.setEnabled(false);

            //
            chrono.setBase(SystemClock.elapsedRealtime());
            chrono.start();

            if (player == null) {
                player = new MediaPlayer();
            }
            try {
                // Fully qualified path name. In this case, we use the Files dir
                String audioFilePath = getFilesDir().getAbsolutePath();
                audioFilePath += "/" + locatorCode + Codes.FILE_EXT_AUDIO;
                Log.d("Audio filename:", audioFilePath);

                // player.setDataSource(audioFilePath);

                File file = new File(audioFilePath);
                FileInputStream fis = new FileInputStream(file);
                player.setDataSource(fis.getFD());
                // Log.i(TAG, "duration: " + player.getDuration());
                player.prepare();
                player.start();
            } catch (Exception e) {
                Log.e("Audio", "Playback failed.", e);
            }

            // stop button
            btnStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_on_b, 0, 0);
            btnStop.setEnabled(true);
            // record button
            btnRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_off_b, 0, 0);
            btnRecord.setEnabled(false);

            // wait for recording to finish playing
            // before turning play button again
            // TODO Must be able to interrupt this when user
            // presses the stop button
            final Handler handler1 = new Handler();
            new Thread(new Runnable() {
                long endMillis = System.currentTimeMillis() + getTimeMillis();

                public void run() {
                    // keep going until length of playback finishes or
                    // keepGoingOnPlayback gets sets to false upon
                    // pressing stop
                    while (audioStatus.equals(AUDIO_STATUS_PLAYBACK)
                            && endMillis >= System.currentTimeMillis()) {

                    }

                    handler1.post(new Runnable() {
                        public void run() {
                            btnPlay.setCompoundDrawablesWithIntrinsicBounds(0,
                                    R.drawable.play_on_b, 0, 0);
                            btnPlay.setEnabled(true);

                            btnStop.setCompoundDrawablesWithIntrinsicBounds(0,
                                    R.drawable.stop_off_b, 0, 0);
                            btnStop.setEnabled(false);

                            if (reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
                                btnRecord.setCompoundDrawablesWithIntrinsicBounds(0,
                                        R.drawable.record_on_b, 0, 0);
                                btnRecord.setEnabled(true);
                            }
                            chrono.stop();

                            stopPlayer();
                        }
                    });
                }
            }).start();

            break;
        }
        }

        chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer chronometer) {
                // TODO Auto-generated method stub
            }
        });

    }

    // stop button processing
    private void stopButtonProcessing() {

        // enable close button
        final ImageButton btnClose = (ImageButton) findViewById(R.id.btnCloseAudioRecord);
        btnClose.setEnabled(true);
        btnClose.setImageResource(R.drawable.close_on);

        if (audioStatus.equals(AUDIO_STATUS_RECORDING)) {

            recordingMillis = SystemClock.elapsedRealtime() - chrono.getBase();
            Log.i(TAG, "recording time: " + recordingMillis);

            LocatorFileManager.insertKeyValuePair(getApplicationContext(), locatorCode
                    + Codes.FILE_EXT_CURRENT, Codes.LOCATOR_FILE_MILLIS_KEY, new String(""
                    + recordingMillis));

            stopRecorder();

        } else if (audioStatus.equals(AUDIO_STATUS_PLAYBACK)) {
            stopPlayer();
            audioStatus = AUDIO_STATUS_IDLE;
        }

        //
        chrono.stop();

        // stop button
        btnStop.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.stop_off_b, 0, 0);
        btnStop.setEnabled(false);

        // record button
        // disable this if not a current record
        if (reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            btnRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_on_b, 0, 0);
            btnRecord.setEnabled(true);
        } else {
            btnRecord.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.record_off_b, 0, 0);
            btnRecord.setEnabled(false);
        }
        // play button
        btnPlay.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.play_on_b, 0, 0);
        btnPlay.setEnabled(true);

    }

    private long getTimeMillis() {

        String theExt = null;
        if (reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            theExt = Codes.FILE_EXT_CURRENT;
        } else if (reportStatus.equals(Codes.RPT_STATUS_QUEUED)) {
            theExt = Codes.FILE_EXT_QUEUED;
        } else {
            theExt = Codes.FILE_EXT_TRANSMITTED;
        }
        // get recording time
        String strMillis = LocatorFileManager.getValue(getApplicationContext(), locatorCode
                + theExt, Codes.LOCATOR_FILE_MILLIS_KEY);

        if (strMillis != null) {
            Long x = Long.valueOf(strMillis);
            return x.longValue();
        }

        return 0;
    }

    // releases recorder if still active
    private void stopRecorder() {

        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

    }

    // releases player if still active
    private void stopPlayer() {

        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);// this will not allow to go back

            stopRecorder();
            stopPlayer();
            finish();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
