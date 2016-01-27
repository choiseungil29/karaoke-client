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
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.clogic.karaokeviewer.Midi.MidiFile;
import com.clogic.karaokeviewer.Midi.event.MidiEvent;
import com.clogic.karaokeviewer.Midi.event.meta.Lyrics;
import com.clogic.karaokeviewer.Model.KSALyric;
import com.clogic.karaokeviewer.Model.KSALyrics;
import com.clogic.karaokeviewer.R;
import com.clogic.karaokeviewer.Util.Logger;
import com.clogic.karaokeviewer.Util.Prefs;
import com.clogic.karaokeviewer.View.LyricsTextView;
import com.clogic.karaokeviewer.View.OutlineTextView;
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
    //@Bind(R.id.sv_score)
    ScoreView scoreView;
    //@Bind(R.id.tv_lyric)
    OutlineTextView tv_lyrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_test);

        //ButterKnife.bind(this);

        tv_lyrics = (OutlineTextView) findViewById(R.id.tv_lyric);
        scoreView = (ScoreView) findViewById(R.id.sv_score);

        initRecodeView();

        ClefSymbol.LoadImages(this);
        TimeSignatureSymbol.LoadImages(this);
        MidiPlayer.LoadImages(this);

        Uri uri = this.getIntent().getData();

        try {
            AssetManager assetManager = getResources().getAssets();
            InputStream stream = assetManager.open(uri.getLastPathSegment());

            scoreView.setActivity(this);
            scoreView.setMidiFile(new MidiFile(stream), getIntent().getStringExtra(Prefs.MIDI_FILE_NAME));
            scoreView.setFileUri(uri);
            scoreView.setListener(this);

            tv_lyrics.lyricsArray = scoreView.lyricsArray;
            ArrayList<KSALyric> lyricList = new ArrayList<>();

            List<Lyrics> list = new ArrayList<>();
            for (MidiEvent event : scoreView.lyricsTrack.getEvents()) {
                if (event instanceof Lyrics) {
                    list.add((Lyrics) event);
                }
            }

            //String lyricsLine = tv_lyrics.lyricsArray.get(lineIndex);
            StringBuilder line = new StringBuilder();
            int lineIndex = 0;
            int i;
            for (i = 0; i < list.size(); i++) {
                Lyrics event = list.get(i);
                if (event.getLyric().equals("\r")) {
                    continue;
                }
                if (event.getLyric().equals("\n")) {
                    continue;
                }
                if (event.getLyric().equals("")) {
                    continue;
                }

                String lyricLine = tv_lyrics.lyricsArray.get(lineIndex).replaceAll(" ", "");
                while (lyricLine.equals("@") || lyricLine.equals("#") || lyricLine.equals("")) {
                    lineIndex++;
                    lyricLine = tv_lyrics.lyricsArray.get(lineIndex).replaceAll(" ", "");
                }

                if (i < list.size() - 1) {
                    lyricList.add(new KSALyric(event.getLyric(), event.getTick(), list.get(i + 1).getTick()));
                    line.append(event.getLyric());
                }

                if (lyricLine.equals(line.toString())) {
                    tv_lyrics.KSALyricsArray.add(new KSALyrics(lyricList, tv_lyrics.lyricsArray.get(lineIndex)));
                    lineIndex++;
                    lyricList = new ArrayList<>();
                    line = new StringBuilder();
                }
            }
            tv_lyrics.KSALyricsArray.add(new KSALyrics(lyricList, tv_lyrics.lyricsArray.get(lineIndex)));

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //scoreView.callOnDraw();
    }

    /**
     * @param nowMeasureLyrics
     * @param tick             실제 mills가 건너온다.
     */
    @Override
    public void notifyMeasureChanged(ArrayList<String> nowMeasureLyrics, long tick) {
        if (nowMeasureLyrics.size() == 0) {
            return;
        }
    }

    int nowLyricsIndex = 0;
    @Override
    public void notifyCurrentTick(long tick, int term, int measureLength) {
        String lyric = "";

        String head = "";
        String tail = "";
        if(nowLyricsIndex == 0) {
            KSALyrics ksaLyrics = tv_lyrics.KSALyricsArray.get(0);
            if(tick > ksaLyrics.startTick - measureLength) {
                head = ksaLyrics.lyricLine;
                tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 1).lyricLine;
            }
            if(tick >= ksaLyrics.endTick + scoreView.resolution/2) {
                head = tv_lyrics.KSALyricsArray.get(nowLyricsIndex+2).lyricLine;
                tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex+1).lyricLine;
                nowLyricsIndex++;
                tv_lyrics.width = 0;
                tv_lyrics.callOnDraw();
            }
        } else {
            KSALyrics nowLyrics = tv_lyrics.KSALyricsArray.get(nowLyricsIndex);
            if(tick >= nowLyrics.endTick + scoreView.resolution/2) {
                if(nowLyricsIndex%2 == 0) {
                    head = tv_lyrics.KSALyricsArray.get(nowLyricsIndex+2).lyricLine;
                    tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex+1).lyricLine;
                } else {
                    head = tv_lyrics.KSALyricsArray.get(nowLyricsIndex+1).lyricLine;
                    tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex+2).lyricLine;
                }
                nowLyricsIndex++;
                tv_lyrics.width = 0;
                tv_lyrics.callOnDraw();
            }
        }

        final String text = head + "\n" + tail;
        if(!text.equals("\n")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_lyrics.setText(text);
                }
            });
        }
        tv_lyrics.setTick(tick, nowLyricsIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            camera = Camera.open(findBackFacingCamera());
            preview.refreshCamera(camera);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
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
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/vpang/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recorder = new MediaRecorder();
        camera.unlock();
        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));

        recorder.setVideoEncodingBitRate(9000000);
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + "/vpang/" + getNewFileName() + ".mp4");
        recorder.setMaxDuration(6000000 * 10); // Set max duration 60 sec.
        recorder.setMaxFileSize(300000000 * 20); // Set max file size 50M


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

