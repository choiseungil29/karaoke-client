package com.karaokepang.Activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.common.base.Strings;
import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.MidiLyrics;
import com.karaokepang.Midi.event.meta.Tempo;
import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Midi.util.MidiUtil;
import com.karaokepang.Model.FileUri;
import com.karaokepang.Model.Lyric;
import com.karaokepang.R;
import com.karaokepang.Util.FilePath;
import com.karaokepang.Util.Logger;
import com.karaokepang.View.CustomTextView;
import com.karaokepang.View.LyricsTextView;
import com.karaokepang.ftp.FtpServiceUp;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity
public abstract class PlayActivity extends BluetoothActivity {

    private ActivityController activityController = ActivityController.getInstance();

    private MediaPlayer player = new MediaPlayer();
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

    @Override
    public void afterViews() {
        super.afterViews();
    }

    public void initMidiFileWithStart(Uri uri) {
        initMidiFile(uri);
        String songNumber = uri.getLastPathSegment().substring(0, uri.getLastPathSegment().length() - 4);
        play(songNumber);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.i("================onPause===========================");
        Logger.i("onPause!");

        player.stop();
        player.reset();
        player.release();
    }

    public void initMidiFile(Uri uri) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(uri.getPath());
            midifile = new MidiFile(fis);
            FileDescriptor fd = fis.getFD();
            player.reset();
            player.setDataSource(fd);
//            player.release();
            player.prepareAsync();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.e("kkk", "onPrepared@@@@@@@@@@@@@@@@@@@@@@");
                    mediaPlayer.start();
                    tickCounter();
                    loop();
                    Log.e("kkk", "onPrepared######################");
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
                    ComponentName componentName = info.get(0).topActivity;
                    String activityName = componentName.getShortClassName().substring(1);
                    if (activityName.contains("PangPangActivity")) {
                        activityController.getPangPangActivity().finishSign = true;
                        activityController.getPangPangActivity().finish();
                        new FtpServiceUp(activityController.getPangPangSelectActivity().fileName).execute();
                    } else if (activityName.contains("DuetActivity")) {
                        activityController.getDuetActivity().finishSign = true;
                        activityController.getDuetActivity().finish();
                        new FtpServiceUp(activityController.getDuetSelectActivity().fileName).execute();
                    }

                    //예약곡 실행
                    if (isReservation()) {
                        File file = new File(FilePath.FILE_PATH_VPANGMID + getReservationNumber() + ".mid");
                        Uri uri = Uri.parse(file.getAbsolutePath());
                        FileUri fileUri = new FileUri(uri, file.getName());
                        if (activityController.isDuetSelectMode()) {
                            Intent intent = new Intent(activityController.getDuetSelectActivity(), DuetActivity_.class);
                            intent.setData(fileUri.getUri());
                            startActivity(intent);
                        } else if (activityController.isPangSelectMode()) {
                            Intent intent = new Intent(activityController.getPangPangSelectActivity(), PangPangActivity_.class);
                            intent.setData(fileUri.getUri());
                            startActivity(intent);
                        }

                        sbReservation = new StringBuffer();
                        for (int i = 1; i < reservation.length; i++) {
                            sbReservation.append("," + reservation[i] + "-" + reservationName[i]);
                        }
//                        reservation = sbReservation.toString().split(",");

                        String[] split = sbReservation.toString().split(",");
                        reservation = new String[split.length - 1];
                        reservationName = new String[split.length - 1];
                        int reservationCount = 0;
                        Log.e("kkk", "splite = " + arrayJoin("#", split));
                        Log.e("kkk", "splite = " + split.length);
                        for (int i = 1; i < split.length; i++) {
                            reservation[reservationCount] = split[i].split("-")[0];
                            reservationName[reservationCount] = split[i].split("-")[1];
                            Log.e("kkk", "예약곡 " + (reservationCount) + ":" + reservation[reservationCount] + "," + reservationName[reservationCount]);
                            reservationCount++;
                        }
                        if (activityController.isDuetSelectMode()) {
                            Log.e("kkk", "!@# = " + arrayJoin(", ", reservationName));
                            activityController.getDuetSelectActivity().textReservation.setText(arrayJoin(", ", reservationName));
                        } else if (activityController.isPangSelectMode()) {
                            Log.e("kkk", "!@#### = " + arrayJoin(", ", reservationName));
                            activityController.getPangPangSelectActivity().textReservation.setText(arrayJoin(", ", reservationName));
                        }
                    }
                    mp.release();
                }
            });

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            MidiInfo.resolution = midifile.getResolution();
            initTracks();
            ltv_lyrics.loadKsaByMidi(uri);
            ltv_lyrics.initLyrics(lyricsTrack);

            Log.e("kkk", "layoutSongName = visible");
            Log.e("kkk", uri.getPath());
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

    private void play(String songNumber) {
        Log.e("kkk", "====play===");
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                layoutSongName.setVisibility(View.GONE);
            }
        };
        Handler mHandler = new Handler();
        mHandler.postDelayed(mRunnable, 3000);

        if (activityController.getPangPangSelectActivity() != null) {
            activityController.getPangPangSelectActivity().startRecord(songNumber);
        }

        if (activityController.getDuetSelectActivity() != null) {
            activityController.getDuetSelectActivity().startRecord(songNumber);
        }
//        player.start();
    }

    public void stop() {
        player.stop();
    }

    @Background
    protected void loop() {
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
