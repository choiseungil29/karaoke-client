package com.karaokepang.Activity;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.Lyrics;
import com.karaokepang.Model.KSALyric;
import com.karaokepang.Model.KSALyrics;
import com.karaokepang.R;
import com.karaokepang.Util.Logger;
import com.karaokepang.Util.Prefs;
import com.karaokepang.View.OutlineTextView;
import com.karaokepang.View.ScoreView;
import com.karaokepang.camera.CameraPreview;
import com.karaokepang.ftp.FtpServiceUp;
import com.midisheetmusic.ClefSymbol;
import com.midisheetmusic.MidiPlayer;
import com.midisheetmusic.TimeSignatureSymbol;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class TestActivity extends AppCompatActivity implements MusicListener {

    //녹화
    private Camera camera;
    private CameraPreview preview;
    private MediaRecorder recorder;
    public boolean is_recording;

    private RelativeLayout layoutCamera;

    private String fileName;
    ScoreView scoreView;
    OutlineTextView tv_lyrics;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private ScalableVideoView videoView;
//    private VideoView videoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_test_three);

        tv_lyrics = (OutlineTextView) findViewById(R.id.tv_lyric);
        scoreView = (ScoreView) findViewById(R.id.sv_score);

        initRecordView();

        ClefSymbol.LoadImages(this);
        TimeSignatureSymbol.LoadImages(this);
        MidiPlayer.LoadImages(this);

        Uri uri = this.getIntent().getData();
        Log.e("kkk", uri.getPath());
        Log.e("kkk", uri.getLastPathSegment());
        try {
            InputStream stream = new FileInputStream(uri.getPath());

            scoreView.setActivity(this);
            MidiFile midiFile = new MidiFile(stream);
            scoreView.setMidiFile(midiFile, getIntent().getStringExtra(Prefs.MIDI_FILE_NAME));
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

            for (KSALyrics lyrics : tv_lyrics.KSALyricsArray) {
                Logger.i("+++++++++++++++");
                Logger.i("lyrics start tick : " + lyrics.startTick);
                Logger.i("lyrics end tick : " + lyrics.endTick);
                Logger.i("lyrics line : " + lyrics.lyricLine);
                for (KSALyric lyric : lyrics.lyricList) {
                    Logger.i("----------------");
                    Logger.i("lyric : " + lyric.lyric);
                    Logger.i("lyric start tick : " + lyric.startTick);
                    Logger.i("lyric end tick : " + lyric.endTick);
                    Logger.i("lyric delta : " + (lyric.endTick - lyric.startTick));
                    Logger.i("----------------");
                }
                Logger.i("+++++++++++++++");
            }

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //scoreView.callOnDraw();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        videoView = (ScalableVideoView) findViewById(R.id.vv_background);
//        if (videoView == null) {
//            return;
//        }
        videoView.setClickable(false);
        videoView.setFocusable(false);
//        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.produce);
        try {
            videoView.setRawData(R.raw.produce);
            videoView.setVolume(0, 0);
            videoView.setLooping(true);
            videoView.prepare(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        tv_lyrics.bringToFront();
        tv_lyrics.invalidate();
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
        String head = "";
        String tail = "";
        if (nowLyricsIndex == 0) {
            KSALyrics ksaLyrics = tv_lyrics.KSALyricsArray.get(0);
            if (tick > ksaLyrics.startTick - measureLength) {
                head = ksaLyrics.lyricLine;
                tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 1).lyricLine;
            }
            if (tick >= ksaLyrics.endTick + scoreView.resolution / 2) {
                head = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 2).lyricLine;
                tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 1).lyricLine;
                nowLyricsIndex++;
                tv_lyrics.width = 0;
                tv_lyrics.callOnDraw();
            }
        } else {
            KSALyrics nowLyrics = tv_lyrics.KSALyricsArray.get(nowLyricsIndex);
            if (tick >= nowLyrics.endTick + scoreView.resolution / 2) {
                if (nowLyricsIndex % 2 == 0) {
                    head = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 2).lyricLine;
                    tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 1).lyricLine;
                } else {
                    head = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 1).lyricLine;
                    tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 2).lyricLine;
                }
                nowLyricsIndex++;
                tv_lyrics.width = 0;
                tv_lyrics.callOnDraw();
            }
        }

        final String text = head + "\n" + tail;
        if (!text.equals("\n")) {
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
            camera.stopPreview();
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
        new FtpServiceUp(this, fileName).execute();
        if (recorder != null) {
            recorder.stop();
            releaseMediaRecorder();
            Toast.makeText(getApplicationContext(), "녹화종료", Toast.LENGTH_LONG).show();
        }
   }

    public void startRecord() {
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
                    Toast.makeText(getApplicationContext(), "녹화시작", Toast.LENGTH_LONG).show();
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
        //recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));

        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getPath() + "/vpang/" + getNewFileName() + ".mp4");
        recorder.setMaxDuration(6000000 * 10);
        recorder.setMaxFileSize(300000000 * 20);

        fileName = getNewFileName();

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


    void initRecordView() {
        AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);

        layoutCamera = (RelativeLayout) findViewById(R.id.camera_layout);
        preview = new CameraPreview(this, getApplicationContext(), camera);
        preview.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        preview.setLayoutParams(new RelativeLayout.LayoutParams(1, 1));
        layoutCamera.addView(preview);
        is_recording = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopRecord();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Test Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.karaokepang.Activity/http/host/path")
        );
        //AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Test Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.karaokepang.Activity/http/host/path")
        );
        //AppIndex.AppIndexApi.end(client, viewAction);
        //client.disconnect();
    }
}
