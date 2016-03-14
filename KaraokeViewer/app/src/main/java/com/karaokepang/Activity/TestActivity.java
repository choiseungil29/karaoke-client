package com.karaokepang.Activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.karaokepang.Dialog.ChooseSongDialog;
import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.Lyrics;
import com.karaokepang.Model.KSALyric;
import com.karaokepang.Model.KSALyrics;
import com.karaokepang.R;
import com.karaokepang.Util.FilePath;
import com.karaokepang.Util.Logger;
import com.karaokepang.Util.MyVideoView;
import com.karaokepang.Util.Util;
import com.karaokepang.View.BMJUATextView;
import com.karaokepang.View.CustomTextView;
import com.karaokepang.View.OutlineTextView;
import com.karaokepang.View.ScoreView;
import com.karaokepang.bluetooth.BluetoothActivity;
import com.karaokepang.camera.CameraPreview;
import com.karaokepang.ftp.FtpServiceUp;
import com.midisheetmusic.ClefSymbol;
import com.midisheetmusic.FileUri;
import com.midisheetmusic.MidiPlayer;
import com.midisheetmusic.TimeSignatureSymbol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class TestActivity extends Activity implements MusicListener {

    //녹화
    private Camera camera;
    private CameraPreview preview;
    private MediaRecorder recorder;
    public boolean is_recording;

    private RelativeLayout layoutCamera;
    public LinearLayout layoutLyric;

    private String fileName;
    private ScoreView scoreView;
    private OutlineTextView tv_lyrics;

    public VideoView videoViewBack;
    public MyVideoView videoView;

    private BMJUATextView textSelectSong;

    //Main
    public ChooseSongDialog dialog;

    private ArrayList<String> localFiles = new ArrayList<>();
    private ArrayList<FileUri> list;

    private String mode = "noData";

    private String songNumber = "";

    private ImageView iv_background;

    public RelativeLayout layoutScore;
    private LinearLayout layoutSongName;
    private CustomTextView textSong, textSinger, textComposer;
    private long firstTime = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        mode = getIntent().getStringExtra("mode");
        setContentView(R.layout.activity_test);

        preData();

        layoutScore = (RelativeLayout) findViewById(R.id.layout_score);
        layoutLyric = (LinearLayout) findViewById(R.id.layout_lyric);
        videoView = (MyVideoView) findViewById(R.id.vv_background);
        videoViewBack = (VideoView) findViewById(R.id.vv_background_back);
        iv_background = (ImageView) findViewById(R.id.iv_background);
        if (mode.equals("vpang")) {
            videoView.setVisibility(View.VISIBLE);
            videoViewBack.setVisibility(View.GONE);
            iv_background.setVisibility(View.GONE);
            layoutLyric.setBackgroundColor(Color.TRANSPARENT);
            setRandomVideoSource();
        } else if (mode.equals("duet")) {
            iv_background.setVisibility(View.VISIBLE);
            videoViewBack.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            iv_background.bringToFront();
            textSelectSong.bringToFront();

            videoViewBack.setLayoutParams(new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth() / 4, getWindowManager().getDefaultDisplay().getHeight() / 4));
            videoViewBack.setFocusable(false);
            videoViewBack.setClickable(false);
            videoViewBack.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mp.setVolume(0, 0);
                    videoViewBack.start();
                }
            });
            videoViewBack.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoViewBack.resume();
                }
            });
            videoViewBack.start();
//            setRandomVideoSource();
            // 여기에 배경 이미지
        }
        videoView.setClickable(false);
        videoView.setFocusable(false);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.resume();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.setVolume(0, 0);
                videoView.start();
            }
        });
        videoView.start();

        initRecordView();

        BluetoothActivity.testActivity = this;
    }

    private void setRandomVideoSource() {
        String[] fileList = getFileList(FilePath.FILE_PATH_VPANGBG);
        String randomVideoFileName = fileList[new Random().nextInt(fileList.length)];
        Log.d("kkk", "선택된파일 = " + randomVideoFileName);
        videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG + randomVideoFileName);
    }

    private String[] getFileList(String strPath) {
        File fileRoot = new File(strPath);
        if (!fileRoot.isDirectory())
            return null;
        return fileRoot.list();
    }

    public void reset() {
        layoutLyric.setVisibility(View.INVISIBLE);
        layoutScore.setVisibility(View.INVISIBLE);
        textSelectSong.setVisibility(View.VISIBLE);
        iv_background.setVisibility(View.VISIBLE);
        stopRecord(false);
        scoreView.player.stop();
        nowLyricsIndex = 0;
        tv_lyrics.reset();
        scoreView.reset();
    }

    private void initSongName(ScoreView scoreView) {
        layoutSongName = (LinearLayout) findViewById(R.id.layout_song_name);
        layoutSongName.setVisibility(LinearLayout.VISIBLE);

        textSong = (CustomTextView) findViewById(R.id.text_songName);
        textComposer = (CustomTextView) findViewById(R.id.text_composer);
        textSinger = (CustomTextView) findViewById(R.id.text_singer);

        textSong.setText(scoreView.getSongName());
        textComposer.setText(scoreView.getComposer());
        textSinger.setText(scoreView.getSinger());
    }

    public void initVpang(Uri uri, String midiFileName) {
        textSelectSong.setVisibility(View.GONE);
        tv_lyrics = (OutlineTextView) findViewById(R.id.tv_lyric);
        scoreView = new ScoreView(this);
        scoreView.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        relativeLayout.setBackgroundColor(Color.WHITE);
        relativeLayout.addView(scoreView);
        layoutScore.addView(relativeLayout);
        scoreView.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // 태경아 여기서 끝나
//                Toast.makeText(getApplicationContext(), "끝", Toast.LENGTH_SHORT).show();
//                Log.e("kkk", "===================END===============");
//                reset();
                if (!mp.isPlaying()) {
                    reset();
                    new FtpServiceUp(BluetoothActivity.testActivity, fileName).execute();
                }
            }
        });


        ClefSymbol.LoadImages(this);
        TimeSignatureSymbol.LoadImages(this);
        MidiPlayer.LoadImages(this);

        try {
            InputStream stream = new FileInputStream(uri.getPath());

            scoreView.setActivity(this);
            MidiFile midiFile = new MidiFile(stream);
            scoreView.setMidiFile(midiFile, midiFileName);
            scoreView.setFileUri(uri);
            scoreView.setListener(this);

            songNumber = uri.getLastPathSegment().substring(0, uri.getLastPathSegment().length() - 4);

            createLyrics();
            initSongName(scoreView);

            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        iv_background.setVisibility(View.GONE);

        tv_lyrics.bringToFront();
        tv_lyrics.invalidate();
    }

    private void createLyrics() {
        tv_lyrics.lyricsArray = scoreView.lyricsArray;

        List<Lyrics> list = new ArrayList<>();
        for (MidiEvent event : scoreView.lyricsTrack.getEvents()) {
            if (event instanceof Lyrics) {
                list.add((Lyrics) event);
            }
        }

        StringBuilder line = new StringBuilder();
        KSALyrics lyrics = new KSALyrics();
        int lineIndex = 0;
        int i;

        for (i = 0; i < list.size(); i++) {
            Lyrics event = list.get(i);
            if (!Util.filterLyricText(event)) {
                continue;
            }

            // 라인이 ""이거나 @, #이면 넘어감
            String lyricLine = tv_lyrics.lyricsArray.get(lineIndex);
            // lyricLine : " "를 없앤 KSA파일 기준 한 줄
            String removeSpace = lyricLine.replaceAll(" ", "");
            while (removeSpace.equals("@") || removeSpace.equals("#") || removeSpace.equals("")) {
                lineIndex++;
                //lyricLine = tv_lyrics.lyricsArray.get(lineIndex).replaceAll(" ", "");
                lyricLine = tv_lyrics.lyricsArray.get(lineIndex);
                removeSpace = lyricLine.replaceAll(" ", "");
            }

            long startTick = event.getTick();
            long endTick;
            try {
                while (!Util.filterLyricText(list.get(i + 1))) {
                    i++;
                }
                endTick = list.get(i + 1).getTick();

            } catch (IndexOutOfBoundsException e) {
                endTick = list.get(i).getTick();
            }
            if ((endTick - startTick) > (ScoreView.resolution * 4)) {
                endTick = startTick + ScoreView.resolution * 4;
            }
            lyrics.lyricList.add(new KSALyric(event.getLyric(), startTick, endTick));
            line.append(event.getLyric());

            // line : string builder. 문자열 한 줄을 조립한다
            if (lyricLine.replaceAll(" ", "").equals(line.toString())) {
                lyrics.lyricLine = lyricLine;
                lyrics.create();
                tv_lyrics.KSALyricsArray.add(lyrics);
                lineIndex++;
                lyrics = new KSALyrics();
                line = new StringBuilder();
            }
        }
        lyrics.create();
        tv_lyrics.KSALyricsArray.add(lyrics);
        boolean isFirst = true;
        for (KSALyrics log : tv_lyrics.KSALyricsArray) {
            if (isFirst) {
                firstTime = log.startTick;
                isFirst = false;
            }
            Logger.i("Lyric", "+++++++++++++++");
            Logger.i("Lyric", "lyrics start tick : " + log.startTick);
            Logger.i("Lyric", "lyrics end tick : " + log.endTick);
            Logger.i("Lyric", "lyrics line : " + log.lyricLine);
            for (KSALyric lyric : log.lyricList) {
                Logger.i("Lyric", "----------------");
                Logger.i("Lyric", "lyric : " + lyric.lyric);
                Logger.i("Lyric", "lyric start tick : " + lyric.startTick);
                Logger.i("Lyric", "lyric end tick : " + lyric.endTick);
                Logger.i("Lyric", "lyric delta : " + (lyric.endTick - lyric.startTick));
                Logger.i("Lyric", "----------------");
            }
            Logger.i("Lyric", "+++++++++++++++");
        }
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
    public void notifyCurrentTick(final float tick, int term, int measureLength) {
        String head = "";
        String tail = "";
        try {
            if (nowLyricsIndex == 0) {
                KSALyrics ksaLyrics = tv_lyrics.KSALyricsArray.get(0);
                if (tick > ksaLyrics.startTick - (ScoreView.resolution * 4)) {
                    head = ksaLyrics.lyricLine;
                    tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 1).lyricLine;
                }
                if (tick >= ksaLyrics.endTick) {
                    head = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 2).lyricLine;
                    tail = tv_lyrics.KSALyricsArray.get(nowLyricsIndex + 1).lyricLine;
                    nowLyricsIndex++;
                    tv_lyrics.width = 0;
                    tv_lyrics.callOnDraw();
                }
            } else {
                KSALyrics nowLyrics = tv_lyrics.KSALyricsArray.get(nowLyricsIndex);
                if (tick >= nowLyrics.endTick) {
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
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
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

    public void showScoreView() {
        layoutSongName.setVisibility(LinearLayout.GONE);
        layoutScore.setVisibility(RelativeLayout.VISIBLE);
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

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
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

        return songNumber + "-" + String.format(Locale.getDefault(), "%04d-%02d-%02d-%02d-%02d-%02d", yy, mm + 1, dd, hh, mi, ss);
    }

    public void stopRecord(boolean isRealFile) {
        if (recorder != null) {
            if (isRealFile) {
                Log.e("kkk", "레코딩 진입!");
                recorder.stop();
                releaseMediaRecorder();
                Toast.makeText(getApplicationContext(), "녹화종료", Toast.LENGTH_LONG).show();
            }
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
        File dir = new File(FilePath.FILE_PATH_VPANG);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recorder = new MediaRecorder();
        camera.unlock();
        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));

        recorder.setVideoEncodingBitRate(1000000);
        //recorder.setVideoFrameRate(30);
        recorder.setOutputFile(FilePath.FILE_PATH_VPANG + getNewFileName() + ".mp4");
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
        preview.setLayoutParams(new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth() / 4, getWindowManager().getDefaultDisplay().getHeight() / 4));
        layoutCamera.addView(preview);
//        layoutCamera.bringToFront();
        is_recording = false;
    }

    void preData() {
        textSelectSong = (BMJUATextView) findViewById(R.id.textView_song_selected);
        textSelectSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseSong("");
            }
        });

        list = new ArrayList<>();
        loadSdcardMidiFiles();

        if (list.size() > 0) {
            Collections.sort(list, list.get(0));
        }

        ArrayList<FileUri> origlist = list;
        list = new ArrayList<>();
        String prevname = "";
        for (FileUri file : origlist) {
            if (!file.toString().equals(prevname)) {
                list.add(file);
            }
            prevname = file.toString();
        }

        dialog = new ChooseSongDialog(this, list);
    }

    public void loadSdcardMidiFiles() {
        File[] fileList = new File(FilePath.FILE_PATH_VPANGMID).listFiles();
        if (fileList == null)
            return;
        for (File file : fileList) {
            localFiles.add(file.getName());
            if (file.getName().endsWith(".mid")) {
                Uri uri = Uri.parse(file.getAbsolutePath());
                FileUri fileUri = new FileUri(uri, file.getName());
                list.add(fileUri);
            }
        }
    }

    private void chooseSong(String message) {
        if (!dialog.isShowing()) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = MATCH_PARENT;
            params.height = MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
            dialog.show();
        }
        dialog.setData(message);
    }

    public void deleteRecodingFile() {
        deleteFile(FilePath.FILE_PATH_VPANG + fileName + ".mp4");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopRecord(false);
//        deleteRecodingFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scoreView != null) {
            scoreView.release();
        }
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
        if (scoreView != null) {
            scoreView.stopMusicHandler();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
