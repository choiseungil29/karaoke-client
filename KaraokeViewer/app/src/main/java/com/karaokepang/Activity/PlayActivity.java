package com.karaokepang.Activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.Tempo;
import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Midi.util.MidiUtil;
import com.karaokepang.R;
import com.karaokepang.Util.FilePath;
import com.karaokepang.Util.Logger;
import com.karaokepang.View.CustomTextView;
import com.karaokepang.View.LyricsTextView;
import com.karaokepang.camera.CameraPreview;
import com.karaokepang.ftp.FtpServiceUp;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity
public abstract class PlayActivity extends BluetoothActivity {

    private ActivityController activityController = ActivityController.getInstance();

    public MediaPlayer player;
    private MidiFile midifile;
    private MidiTrack lyricsTrack;
    private MidiTrack renderTrack;
    private List<MidiTrack> renderTracks = new ArrayList<>();
    private List<Tempo> tempos;

    public boolean finishSign = false;

    private float tick = 0;
    @ViewById(R.id.layout_song_name)
    LinearLayout layoutSongName;
    @ViewById(R.id.tv_songName)
    CustomTextView tv_songName;
    @ViewById(R.id.tv_composer)
    CustomTextView tv_composerName;
    @ViewById(R.id.tv_singer)
    CustomTextView tv_singer;
    @ViewById(R.id.ltv_lyrics)
    LyricsTextView ltv_lyrics;

    public String songNumber;

    //=======================카메라==========================
    @ViewById(R.id.camera_layout)
    RelativeLayout layoutCamera;
    private Camera camera;
    public CameraPreview preview;
    private MediaRecorder recorder;
    public String fileName;


    public void setCameraPreView() {
        preview = new CameraPreview(getApplicationContext(), this, camera);
        preview.setLayoutParams(new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth() / 4, getWindowManager().getDefaultDisplay().getHeight() / 4));
        layoutCamera.addView(preview);
    }

    public void setDuetCameraPreView() {
        preview = new CameraPreview(getApplicationContext(), this, camera);
        int width = getWindowManager().getDefaultDisplay().getWidth() / 2;
        preview.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (width * 0.56)));
        layoutCamera.addView(preview);
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void stopRecord(boolean isRealFile) {
        if (recorder != null) {
            if (isRealFile) {
                recorder.stop();
                releaseMediaRecorder();
                Toast.makeText(getApplicationContext(), "녹화종료", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void startRecord(String songNumber) {
        if (!prepareMediaRecorder(songNumber)) {
            Toast.makeText(getApplicationContext(), "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
            finish();
        }
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    Log.e("kkk", "====recoder===");
                    Toast.makeText(getApplicationContext(), "녹화시작", Toast.LENGTH_LONG).show();
                    player.prepareAsync();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean prepareMediaRecorder(String songNumber) {
        File dir = new File(FilePath.FILE_PATH_VPANG);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        releaseCamera();
        if (camera == null) {
            camera = Camera.open(findBackFacingCamera());
            preview.refreshCamera(camera);
            try {
                camera.unlock();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        if (recorder == null) {
            try {
                recorder = new MediaRecorder();
                recorder.setCamera(camera);
                recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
                recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
                recorder.setVideoEncodingBitRate(1000000);
                //recorder.setVideoFrameRate(30);
                recorder.setOutputFile(FilePath.FILE_PATH_VPANG + fileName + ".mp4");
                recorder.setMaxDuration(6000000 * 10);
                recorder.setMaxFileSize(300000000 * 20);
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
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        fileName = getNewFileName(songNumber);


        return true;
    }

    public String getNewFileName(String songNumber) {
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);
        int hh = c.get(Calendar.HOUR_OF_DAY);
        int mi = c.get(Calendar.MINUTE);
        int ss = c.get(Calendar.SECOND);

        return songNumber + "-" + String.format(Locale.getDefault(), "%04d-%02d-%02d-%02d-%02d-%02d", yy, mm + 1, dd, hh, mi, ss);
    }


    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }

    public void cameraResume() {
        stopRecord(true);
        if (camera == null) {
            camera = Camera.open(findBackFacingCamera());
            preview.refreshCamera(camera);
        }
    }

    public void cameraPause() {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public int findBackFacingCamera() {
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

    //=========================카메라=================
    @Override
    public void afterViews() {
        super.afterViews();
    }

    public void initMidiFileWithStart(Uri uri) {
        Log.e("kkk", "initMIdifile");
        String songNumber = uri.getLastPathSegment().substring(0, uri.getLastPathSegment().length() - 4);
        initMidiFile(uri, songNumber);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("kkk", "================onPause===========================");
        player.stop();
        player.reset();
        player.release();
    }

    public void initMidiFile(Uri uri, final String songNumber) {
        this.songNumber = songNumber;
        setDuetCameraPreView();
        try {
            FileInputStream fis = new FileInputStream(uri.getPath());
            midifile = new MidiFile(fis);
            player = new MediaPlayer();
            Log.w("kkk", "media player 초기화");
            player.reset();
            player.setDataSource(fis.getFD());
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    play(songNumber, mediaPlayer);
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.e("kkk", "player error" + what + "," + extra+"/");
                    if (what == 100) {
                        mp.stop();
                    } else if (what == 1) {
                        Log.i("My Error ", "handled here");
                        mp.stop();
                    } else if (what == 800) {
                        mp.stop();
                    } else if (what == 701) {
                        mp.stop();
                    } else if (what == 700) {
                        mp.stop();
                        Toast.makeText(getApplicationContext(), "Bad Media format ", Toast.LENGTH_SHORT).show();
                    } else if (what == -38) {
                        mp.stop();
                    }
                    return false;
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
                    ComponentName componentName = info.get(0).topActivity;
                    String activityName = componentName.getShortClassName().substring(1);
                    Log.e("kkk", "노래끝");
                    if (activityName.contains("PangPangActivity")) {
                        Log.e("kkk", "pangpang 모드 노래 끝");
                        if (null != activityController.getPangPangActivity()) {
                            activityController.getPangPangActivity().finishSign = true;
//                            activityController.getPangPangActivity().finish();
                            PangPangActivity.pangPangActivity.finish();
                        }
                        if (isReservation()) {
                            Log.i("kkk", getReservationNumber() + "/예약곡 있다. setResult/" + getLocalClassName());
                            startPangPlay(getReservationNumber());
                        }
                        new FtpServiceUp(fileName).execute();
                    } else if (activityName.contains("DuetActivity")) {
                        Log.e("kkk", "duet 모드 노래 끝");
                        if (null != activityController.getDuetActivity()) {
                            activityController.getDuetActivity().finishSign = true;
//                            activityController.getDuetActivity().finish();
                            DuetActivity.duetActivity.finish();
                        }
                        if (isReservation()) {
                            Log.i("kkk", getReservationNumber() + "/예약곡 있다. setResult/" + getLocalClassName());
                            startDuetPlay(getReservationNumber());
                        }
                        new FtpServiceUp(fileName).execute();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            MidiInfo.resolution = midifile.getResolution();
            initTracks();
            ltv_lyrics.loadKsaByMidi(uri);
            ltv_lyrics.initLyrics(lyricsTrack);
            layoutSongName.setVisibility(View.VISIBLE);
            File musicFile = new File(uri.getPath().replace(".mid", ".ksa"));
            tv_songName.setText(MidiUtil.getSongName(musicFile));
            tv_composerName.setText(MidiUtil.getComposer(musicFile));
            tv_singer.setText(MidiUtil.getSinger(musicFile));
            Log.e("kkk", "layoutsong =" + MidiUtil.getSongName(musicFile) + "," + MidiUtil.getComposer(musicFile) + "," + MidiUtil.getSinger(musicFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void play(String songNumber, final MediaPlayer mediaPlayer) {
        super.play(songNumber, mediaPlayer);
        Log.e("kkk", "====play play===");
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        layoutSongName.setVisibility(View.GONE);
                        recorder.start();
                        mediaPlayer.start();
                        finishSign = false;
                        tickCounter();
                        loop();
                    }
                });

            }
        };
        Handler mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 1500);


//        if (activityController.getPangPangSelectActivity() != null) {
//            activityController.getPangPangSelectActivity().startRecord(songNumber);
//        }
//
//        if (activityController.getDuetSelectActivity() != null) {
//            activityController.getDuetSelectActivity().startRecord(songNumber);
//        }
    }

    @Background
    public void loop() {
        try {
            int term = 30;
            long beforePosition = player.getCurrentPosition();
            while (!finishSign &&
                    player.getCurrentPosition() < player.getDuration()) {
                if (player.getCurrentPosition() - beforePosition <= term) {
                    continue;
                }
                beforePosition = player.getCurrentPosition();

                this.update(tick);
                this.draw(tick);

                ltv_lyrics.update(tick);
                ltv_lyrics.callOnDraw(tick);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected abstract void update(float tick);

    protected void draw(float tick) {
        ltv_lyrics.callOnDraw(tick);
    }

    @Background
    public void tickCounter() {
        try {
            long beforePosition = player.getCurrentPosition();
            int term = 32;

            while (!finishSign &&
                    player.getCurrentPosition() < player.getDuration()) {
                if (player.getCurrentPosition() - beforePosition <= term) {
                    continue;
                }
                if (tempos == null || tempos.size() == 0) {
                    throw new RuntimeException("tempos size 0");
                }

                long currentPosition = player.getCurrentPosition();
                if (tempos.size() == 1) {
                    float tick = 0;
                    Tempo lastTempo = tempos.get(0);
                    tick += lastTempo.getBpm() / 60 * MidiInfo.resolution * ((float) currentPosition) / 1000;

                    if (this.tick < tick) {
                        this.tick = tick;
                    }
                } else if (tempos.size() == 2) {
                    Tempo firstTempo = tempos.get(0);
                    Tempo secondTempo = tempos.get(1);

                    float totalTick = 0;
                    long firstTempoMillis = (long) (secondTempo.getTick() / (firstTempo.getBpm() / 60 * MidiInfo.resolution)) * 1000;
                    if (currentPosition < firstTempoMillis) {
                        totalTick += firstTempo.getBpm() / 60 * MidiInfo.resolution * ((float) currentPosition) / 1000;
                    } else {
                        currentPosition -= (secondTempo.getTick() / (firstTempo.getBpm() / 60 * MidiInfo.resolution)) * 1000;
                        totalTick += secondTempo.getTick();
                        totalTick += secondTempo.getBpm() / 60 * MidiInfo.resolution * ((float) currentPosition) / 1000;
                    }
                    if (this.tick < totalTick) {
                        this.tick = totalTick;
                    }
                } else if (tempos.size() == 3) {
                    Tempo firstTempo = tempos.get(0);
                    Tempo secondTempo = tempos.get(1);
                    Tempo thirdTempo = tempos.get(2);

                    float totalTick = 0;
                    long firstTempoMillis = (long) (secondTempo.getTick() / (firstTempo.getBpm() / 60 * MidiInfo.resolution)) * 1000;
                    long secondTempoMillis = (long) (firstTempoMillis + ((thirdTempo.getTick() - secondTempo.getTick()) / (secondTempo.getBpm() / 60 * MidiInfo.resolution)) * 1000);

                    if (currentPosition < firstTempoMillis) {
                        totalTick += firstTempo.getBpm() / 60 * MidiInfo.resolution * ((float) currentPosition) / 1000;
                    } else if (currentPosition < secondTempoMillis) {
                        currentPosition -= (secondTempo.getTick() / (firstTempo.getBpm() / 60 * MidiInfo.resolution)) * 1000;
                        totalTick += secondTempo.getTick();
                        totalTick += secondTempo.getBpm() / 60 * MidiInfo.resolution * ((float) currentPosition) / 1000;
                    } else {
                        Logger.i("tempo bpm : " + tempos.get(0).getBpm() + ", tempo tick : " + tempos.get(0).getTick());
                        Logger.i("tempo bpm : " + tempos.get(1).getBpm() + ", tempo tick : " + tempos.get(1).getTick());
                        Logger.i("tempo bpm : " + tempos.get(2).getBpm() + ", tempo tick : " + tempos.get(2).getTick());
                        Logger.i("seconds : " + secondTempoMillis);
                        currentPosition -= firstTempoMillis + ((thirdTempo.getTick() - secondTempo.getTick()) / (secondTempo.getBpm() / 60 * MidiInfo.resolution)) * 1000;
                        totalTick += thirdTempo.getTick();
                        totalTick += thirdTempo.getBpm() / 60 * MidiInfo.resolution * ((float) currentPosition) / 1000;
                    }
                    if (this.tick < totalTick) {
                        this.tick = totalTick;
                    }
                } else {

                }

                beforePosition = player.getCurrentPosition();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTracks() {
        for (int i = 0; i < midifile.getTracks().size(); i++) {
            Iterator<MidiEvent> it = midifile.getTracks().get(i).getEvents().iterator();
            while (it.hasNext()) {
                MidiEvent event = it.next();

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("kasa")) {
                    lyricsTrack = midifile.getTracks().get(i);
                }

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("melody")) {
                    renderTracks.add(midifile.getTracks().get(i));
                }
            }
        }

        renderTrack = new MidiTrack();
        for (MidiTrack track : renderTracks) {
            for (MidiEvent event : track.getEvents()) {
                renderTrack.insertEvent(event);
            }
        }
        tempos = midifile.getTracks().get(0).getEvents(Tempo.class);
    }
}
