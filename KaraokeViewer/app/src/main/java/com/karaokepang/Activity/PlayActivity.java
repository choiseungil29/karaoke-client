package com.karaokepang.Activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.LinearLayout;

import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.MidiLyrics;
import com.karaokepang.Midi.event.meta.Tempo;
import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Midi.util.MidiUtil;
import com.karaokepang.Model.Lyric;
import com.karaokepang.Model.Lyrics;
import com.karaokepang.R;
import com.karaokepang.Util.Logger;
import com.karaokepang.View.CustomTextView;
import com.karaokepang.View.LyricsTextView;

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

    protected MediaPlayer player = new MediaPlayer();
    protected MidiFile midifile;
    protected MidiTrack lyricsTrack;
    protected MidiTrack renderTrack;
    protected List<MidiTrack> renderTracks = new ArrayList<>();
    protected List<Tempo> tempos;

    protected List<String> ksaLyricsArray = new ArrayList<>();
    protected Lyrics lyrics = new Lyrics();

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
    public void onDestroy() {
        super.onDestroy();
        Logger.i("destroy!");
        player.reset();
    }

    public void initMidiFile(Uri uri) {
        try {
            FileInputStream fis = new FileInputStream(uri.getPath());
            midifile = new MidiFile(fis);
            FileDescriptor fd = fis.getFD();
            player.reset();
            player.setDataSource(fd);
            player.prepare();

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    finish();
                }
            });
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MidiInfo.resolution = midifile.getResolution();
        loadKsaByMidi(uri);
        initTracks();
        initLyrics();

        File musicFile = new File(uri.getPath());
        tv_songName.setText(MidiUtil.getSongName(musicFile));
        tv_composerName.setText(MidiUtil.getComposer(musicFile));
        tv_singer.setText(MidiUtil.getSinger(musicFile));
    }

    public void play(String songNumber) {
        if (activityController.getPangPangSelectActivity() != null) {
            activityController.getPangPangSelectActivity().startRecord(songNumber);
        }

        if (activityController.getDuetSelectActivity() != null) {
            activityController.getDuetSelectActivity().startRecord(songNumber);
        }
        player.start();
        Logger.i("play!!");
        Logger.i("player play!");
        tickCounter();
        //draw();
        loop();
    }

    public void stop() {
        player.stop();
    }

    @Background
    protected void loop() {
        int term = 30;
        long beforePosition = player.getCurrentPosition();

        while (player.getCurrentPosition() < player.getDuration()) {
            if(player.getCurrentPosition() - beforePosition <= term) {
                continue;
            }
            beforePosition = player.getCurrentPosition();

            this.update(tick);
            this.draw(tick);

            ltv_lyrics.update(tick);
            ltv_lyrics.callOnDraw(tick);
        }
    }

    protected abstract void update(float tick);

    protected void draw(float tick) {
        ltv_lyrics.callOnDraw(tick);
    }

    @Background
    public void tickCounter() {
        long beforePosition = player.getCurrentPosition();
        int term = 32;

        while (player.getCurrentPosition() < player.getDuration()) {
            if(player.getCurrentPosition() - beforePosition <= term) {
                continue;
            }

            beforePosition = player.getCurrentPosition();
            if(tempos == null || tempos.size() == 0) {
                throw new RuntimeException("tempos size 0");
            }

            HashMap<Long, Float> tempoTickToMillis = new HashMap<>();
            for(Tempo t : tempos) {
                tempoTickToMillis.put(t.getTick(), t.getTick() / t.getBpm() * 1000);
            }

            float tick = 0;
            long currentPosition = player.getCurrentPosition();
            int i;
            for(i=0; i<tempos.size()-1; i++) {
                Tempo t = tempos.get(i);
                float limitMillis = tempoTickToMillis.get(tempos.get(i + 1).getTick());
                if(currentPosition > limitMillis - t.getTick()/t.getBpm() * 1000) {
                    tick += t.getBpm() / 60 * MidiInfo.resolution * limitMillis / 1000;
                    currentPosition -= limitMillis;
                }
            }
            Tempo lastTempo = tempos.get(i);
            tick += lastTempo.getBpm() / 60 * MidiInfo.resolution * ((float)currentPosition) / 1000;

            if(this.tick < tick) {
                this.tick = tick;
            }
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
        for(MidiTrack track : renderTracks) {
            for(MidiEvent event : track.getEvents()) {
                renderTrack.insertEvent(event);
            }
        }
        tempos = midifile.getTracks().get(0).getEvents(Tempo.class);
    }
    private void initLyrics() {
        Iterator<MidiEvent> lyricsIt = lyricsTrack.getEvents().iterator();
        int i = 0;
        int j = 0;
        String eng;

        List<MidiLyrics> midiLyrics = new ArrayList<>();
        try {
            while (lyricsIt.hasNext()) {
                MidiEvent event = lyricsIt.next();
                if (!(event instanceof MidiLyrics)) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().equals("\r")) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().equals("\n")) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().equals("")) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().endsWith(" ")) {
                    ((MidiLyrics) event).setLyric(
                            ((MidiLyrics) event).getLyric().substring(
                                    0, ((MidiLyrics) event).getLyric().length()-1));
                }

                char nowCharacter = ksaLyricsArray.get(i).charAt(j);
                if (nowCharacter == '@' ||
                        nowCharacter == '#') {
                    i++;
                    j = 0;
                }

                if (nowCharacter == ' ') {
                    j++;
                }

                nowCharacter = ksaLyricsArray.get(i).charAt(j);
                eng = "";
                char start = nowCharacter;
                if (start >= 'a' && start <= 'z' ||
                        start >= 'A' && start <= 'Z') {
                    while (nowCharacter != ' ' &&
                            nowCharacter != '\n' &&
                            nowCharacter != '\r' &&
                            nowCharacter != '^') {
                        if (eng.trim().length() < ((MidiLyrics) event).getLyric().trim().length()) {
                            eng += nowCharacter;
                            j++;
                            if (j >= ksaLyricsArray.get(i).length()) {
                                i++;
                                j = 0;
                            }
                            if(i >= ksaLyricsArray.size()) {
                                break;
                            }
                            nowCharacter = ksaLyricsArray.get(i).charAt(j);
                        } else {
                            break;
                        }
                    }
                }

                if (eng.length() > 0) {
                    ((MidiLyrics) event).setLyric(eng);
                } else {
                    ((MidiLyrics) event).setLyric(String.valueOf(nowCharacter));

                    j++;
                    if (j >= ksaLyricsArray.get(i).length()) {
                        i++;
                        j = 0;
                    }
                }

                midiLyrics.add((MidiLyrics) event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 가사들 줄별로 정리해주기.
        // MidiLyrics -> Lyrics로 대입
        List<Lyric> lyrics = new ArrayList<>();
        MidiLyrics beforeLyric = null;
        i=0;
        StringBuilder sb = new StringBuilder();
        for(MidiLyrics lyric : midiLyrics) {
            if(beforeLyric != null) {
                long duration = lyric.getTick() - beforeLyric.getTick();
                if(duration > MidiInfo.resolution * 2) {
                    duration = MidiInfo.resolution * 2;
                }
                lyrics.add(new Lyric(ksaLyricsArray.get(i),
                        beforeLyric.getLyric(),
                        beforeLyric.getTick(),
                        beforeLyric.getTick() + duration));
            }

            if(ksaLyricsArray.get(i).replaceAll(" ", "").equals(sb.toString())) {
                if(i < ksaLyricsArray.size() - 1) {
                    i++;
                    sb = new StringBuilder();
                }
            }
            sb.append(lyric.getLyric());
            beforeLyric = lyric;
        }
        lyrics.add(new Lyric(ksaLyricsArray.get(i),
                beforeLyric.getLyric(),
                beforeLyric.getTick(),
                beforeLyric.getTick() + MidiInfo.resolution * 2));

        ltv_lyrics.initLyrics(lyrics);
    }
    public void loadKsaByMidi(Uri uri) {
        File lyricsFile = new File(uri.getPath().toLowerCase().replace(".mid", ".ksa"));
        if (lyricsFile.exists()) {
            try {
                InputStream is = new FileInputStream(lyricsFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "euc-kr"));

                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    if (count < 4) {
                        count++;
                        continue;
                    }
                    if(line.equals("")) {
                        continue;
                    }
                    ksaLyricsArray.add(line);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
