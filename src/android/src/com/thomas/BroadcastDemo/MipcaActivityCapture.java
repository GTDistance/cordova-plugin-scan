package com.thomas.BroadcastDemo;

import java.io.IOException;
import java.util.Vector;


import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.thomas.view.ViewfinderView;
import com.thomas.zxing.camera.CameraManager;
import com.thomas.zxing.decoding.CaptureActivityHandler;
import com.thomas.zxing.decoding.InactivityTimer;


/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
public class MipcaActivityCapture extends Activity implements Callback {

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        int id = getResources().getIdentifier("activity_capture"
                , "layout", getPackageName());
//        setContentView(R.layout.activity_capture);
        setContentView(id);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        ;

//        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView = (ViewfinderView) findViewById(getId("viewfinder_view", "id"));
//        CheckBox flashlight = (CheckBox) findViewById(R.id.scan_flashlight);
        CheckBox flashlight = (CheckBox) findViewById(getId("scan_flashlight", "id"));
        flashlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    openLightOn();
                } else {
                    closeLightOff();
                }
            }
        });
        findViewById(getId("iv_back", "id")).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }


    private int getId(String idName,String type){
        return getResources().getIdentifier(idName, type, getPackageName());
    }
    @Override
    protected void onResume() {
        super.onResume();
//        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceView surfaceView = (SurfaceView) findViewById(getId("preview_view","id"));
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        closeLightOff();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {

        inactivityTimer.shutdown();
        super.onDestroy();
    }


    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        if (resultString.equals("")) {
            Toast.makeText(MipcaActivityCapture.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
            Intent resultIntent = new Intent();
            Bundle bundle = new Bundle();
            resultString = resultString.replaceAll("[^(a-zA-Z0-9\\u4e00-\\u9fa5)]", "");
            bundle.putString("result", resultString);
//            bundle.putParcelable("bitmap", barcode);
            resultIntent.putExtras(bundle);
            this.setResult(RESULT_OK, resultIntent);
        }
        MipcaActivityCapture.this.finish();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats,
                    characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

//            AssetFileDescriptor file = getResources().openRawResourceFd(
//                    R.raw.beep);
            AssetFileDescriptor file = getResources().openRawResourceFd(getId("beep","raw"));
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

    private Camera m_Camera = null;
    private boolean isflashlightOpen = false;

    private void openLightOn() {
        if (isflashlightOpen) return;
        m_Camera = CameraManager.get().getCamera();

        Camera.Parameters parameter = m_Camera.getParameters();

        parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        m_Camera.setParameters(parameter);
        isflashlightOpen = true;
    }

    private void closeLightOff() {
        if (isflashlightOpen && m_Camera != null) {
            Camera.Parameters parameter = m_Camera.getParameters();
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            m_Camera.setParameters(parameter);
            isflashlightOpen = false;
        }
    }
}
