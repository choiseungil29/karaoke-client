package com.clogic.karaokeviewer.Activity;

import android.content.Context;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.clogic.karaokeviewer.Midi.MidiFile;
import com.clogic.karaokeviewer.Midi.event.MidiEvent;
import com.clogic.karaokeviewer.Midi.event.meta.Lyrics;
import com.clogic.karaokeviewer.Model.KSALyrics;
import com.clogic.karaokeviewer.R;
import com.clogic.karaokeviewer.Util.Logger;
import com.clogic.karaokeviewer.Util.Prefs;
import com.clogic.karaokeviewer.View.LyricsTextView;
import com.clogic.karaokeviewer.View.ScoreView;
import com.clogic.karaokeviewer.camera.CameraPreview;
import com.midisheetmusic.ClefSymbol;
import com.midisheetmusic.MidiPlayer;
import com.midisheetmusic.TimeSignatureSymbol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class TestActivity extends AppCompatActivity implements MusicListener {

    //녹화
    private Camera camera;
    private CameraPreview preview;
    private MediaRecorder recorder;
    private boolean is_holder_created;
    public boolean is_recording;

    private MidiFile midi = null;
    @Bind(R.id.sv_score)
    ScoreView scoreView;
    @Bind(R.id.tv_lyric)
    LyricsTextView tv_lyrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        ButterKnife.bind(this);

        //initRecodeView();

        ClefSymbol.LoadImages(this);
        TimeSignatureSymbol.LoadImages(this);
        MidiPlayer.LoadImages(this);

        Uri uri = this.getIntent().getData();

        try {
            AssetManager assetManager = getResources().getAssets();
            InputStream stream = assetManager.open(uri.getLastPathSegment());
            Logger.i("File name : " + uri.getLastPathSegment());
            scoreView.setMidiFile(new MidiFile(stream), getIntent().getStringExtra(Prefs.MIDI_FILE_NAME));
            scoreView.setFileUri(uri);
            scoreView.setListener(this);

            tv_lyrics.lyricsTrack = scoreView.lyricsTrack;
            tv_lyrics.lyricsArray = scoreView.lyricsArray;

            int lineIndex = 0;
            String lyricsLine = tv_lyrics.lyricsArray.get(lineIndex);
            StringBuilder line = new StringBuilder();
            MidiEvent first = null;
            MidiEvent last = null;
            for (MidiEvent event : tv_lyrics.lyricsTrack.getEvents()) {
                if (!(event instanceof Lyrics)) {
                    continue;
                }

                if (((Lyrics) event).getLyric().equals("\r")) {
                    continue;
                }
                if (((Lyrics) event).getLyric().equals("\n")) {
                    continue;
                }
                if (((Lyrics) event).getLyric().equals("")) {
                    continue;
                }
                /*if(((Lyrics) event).getLyric().equals("#") ||
                        ((Lyrics) event).getLyric().equals("@")) {
                    continue;
                }*/
                if (lyricsLine.equals("@") || lyricsLine.equals("#")) {
                    lineIndex++;
                    lyricsLine = tv_lyrics.lyricsArray.get(lineIndex);
                    first = null;
                    line = new StringBuilder();
                }

                if (first == null) {
                    first = event;
                }

                last = event;
                line.append(((Lyrics) event).getLyric());

                if (lyricsLine.replaceAll(" ", "").equals(line.toString())) {
                    tv_lyrics.KSALyricsArray.add(new KSALyrics(lyricsLine, first.getTick(), last.getTick()));

                    lineIndex++;
                    lyricsLine = tv_lyrics.lyricsArray.get(lineIndex);
                    first = null;
                    line = new StringBuilder();
                }
            }

            for (MidiEvent event : tv_lyrics.lyricsTrack.getEvents()) {
                if (!(event instanceof Lyrics)) {
                    continue;
                }
            }

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //scoreView.callOnDraw();
    }

    /**
     * @param nowMeasureLyrics
     * @param tick   실제 mills가 건너온다.
     */
    @Override
    public void notifyMeasureChanged(ArrayList<String> nowMeasureLyrics, long tick) {
        if (nowMeasureLyrics.size() == 0) {
            return;
        }

        for(int i=0; i<tv_lyrics.KSALyricsArray.size(); i++) {
            KSALyrics lyrics = tv_lyrics.KSALyricsArray.get(i);

            if(tick >= lyrics.startTick &&
                    tick <= lyrics.endTick) {
                String text = lyrics.lyricLine;
                String tail = "";
                String head = "";
                if(i%2 == 0) {
                    if(i < tv_lyrics.KSALyricsArray.size()-1) {
                        tail = tv_lyrics.KSALyricsArray.get(i+1).lyricLine;
                        text = text + "\n" + tail;
                    }
                } else {
                    head = tv_lyrics.KSALyricsArray.get(i+1).lyricLine;
                    text = head + "\n" + text;
                }

                final String finalText = text;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_lyrics.setText(finalText);
                    }
                });
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (camera == null) {
            camera = Camera.open(findBackFacingCamera());
            preview.refreshCamera(camera);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        //releaseCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //stopRecord();
    }


    private int findBackFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public String getNewFileName() {
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);
        int hh = c.get(Calendar.HOUR_OF_DAY);
        int mi = c.get(Calendar.MINUTE);
        int ss = c.get(Calendar.SECOND);

        return String.format(Locale.getDefault(), "%04d-%02d-%02d-%02d-%02d-%02d", yy, mm + 1, dd, hh, mi, ss);
    }

    public void stopRecord() {
        if (recorder != null) {
            recorder.stop();
            releaseMediaRecorder();
            Toast.makeText(getApplicationContext(), "녹화종료", Toast.LENGTH_LONG).show();
        }
    }

    public void startRecord() {
        Toast.makeText(getApplicationContext(), "녹화시작", Toast.LENGTH_LONG).show();
        if (!prepareMediaRecorder()) {
            Toast.makeText(getApplicationContext(), "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
            finish();
        }
        // work on UiThread for better performance
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    is_recording = true;
                    recorder.start();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean prepareMediaRecorder() {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/vpang/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recorder = new MediaRecorder();
        camera.unlock();
        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));

        recorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + "/vpang/" + getNewFileName() + ".mp4");
        recorder.setMaxDuration(600000 * 10); // Set max duration 60 sec.
        recorder.setMaxFileSize(50000000 * 20); // Set max file size 50M


        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
//            camera.lock();
        }
    }


    void initRecodeView() {
        AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);

        RelativeLayout layoutCamera = (RelativeLayout) findViewById(R.id.camera_layout);
        preview = new CameraPreview(this, getApplicationContext(), camera);
        preview.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layoutCamera.addView(preview);
        is_recording = false;
    }

}

