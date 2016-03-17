package com.karaokepang.Activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.LinearLayout;

import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.util.MidiUtil;
import com.karaokepang.Model.KSALyrics;
import com.karaokepang.Model.Lyrics;
import com.karaokepang.R;
import com.karaokepang.View.CustomTextView;

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
import java.util.Map;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity
public class PlayActivity extends BluetoothActivity {

    private ActivityController activityController = ActivityController.getInstance();

    protected MediaPlayer player = new MediaPlayer();
    protected MidiFile midifile;
    protected MidiTrack lyricsTrack;

    protected Lyrics lyrics;
    protected List<String> ksaLyricsArray = new ArrayList<>();
    protected StringBuilder ksaLyrics;

    private Map<Float, Float> millisToBpm = new HashMap<>();

    @ViewById(R.id.layout_song_name) LinearLayout layoutSongName;
    @ViewById(R.id.tv_songName) CustomTextView tv_songName;
    @ViewById(R.id.tv_composer) CustomTextView tv_composerName;
    @ViewById(R.id.tv_singer) CustomTextView tv_singer;

    @Override
    public void afterViews() {
        super.afterViews();
    }

    public void initWithStartMidiFile(Uri uri) {
        initMidiFile(uri);
        String songNumber = uri.getLastPathSegment().substring(0, uri.getLastPathSegment().length() - 4);
        play(songNumber);
    }

    public void initMidiFile(Uri uri) {
        try {
            FileInputStream fis = new FileInputStream(uri.getPath());
            midifile = new MidiFile(fis);
            FileDescriptor fd = fis.getFD();
            player.reset();
            player.setDataSource(fd);
            player.prepare();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0; i<midifile.getTracks().size(); i++) {
            Iterator<MidiEvent> it = midifile.getTracks().get(i).getEvents().iterator();
            while (it.hasNext()) {
                MidiEvent event = it.next();

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("kasa")) {
                    lyricsTrack = midifile.getTracks().get(i);
                }
            }
        }

        File lyricsFile = new File(uri.getPath().toLowerCase().replace(".mid", ".ksa"));
        if(lyricsFile.exists()) {
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

        Iterator<MidiEvent> lyricsIt = lyricsTrack.getEvents().iterator();
        int i=0;
        int j=0;
        try {
            while (lyricsIt.hasNext()) {
                MidiEvent event = lyricsIt.next();
                if (!(event instanceof com.karaokepang.Midi.event.meta.Lyrics)) {
                    continue;
                }

                if (((com.karaokepang.Midi.event.meta.Lyrics) event).getLyric().equals("\r")) {
                    continue;
                }
                if (((com.karaokepang.Midi.event.meta.Lyrics) event).getLyric().equals("\n")) {
                    continue;
                }
                if (((com.karaokepang.Midi.event.meta.Lyrics) event).getLyric().equals("")) {
                    continue;
                }

                char nowCharacter = ksaLyricsArray.get(i).charAt(j);
                if(nowCharacter == '@' ||
                        nowCharacter == '#' ||
                        nowCharacter == ' ') {
                    j++;
                }

                if(j >= ksaLyricsArray.get(i).length()) {
                    i++;
                    j=0;
                }

                nowCharacter = ksaLyricsArray.get(i).charAt(j);
                String eng = "";
                char start = nowCharacter;
                if(start >= 'a' && start <= 'z' ||
                        start >= 'A' && start <= 'Z') {
                    while (nowCharacter != ' ' &&
                            nowCharacter != '\n' &&
                            nowCharacter != '\r' &&
                            nowCharacter != '^') {
                        if(eng.trim().length() < ((com.karaokepang.Midi.event.meta.Lyrics) event).getLyric().trim().length()) {
                            eng += nowCharacter;
                            j++;
                            if(j >= ksaLyricsArray.get(i).length()) {
                                i++;
                                j=0;
                            }
                            nowCharacter = ksaLyricsArray.get(i).charAt(j);
                        } else {
                            break;
                        }
                    }
                }

                if(eng.length() > 0) {
                    ((com.karaokepang.Midi.event.meta.Lyrics) event).setLyric(eng);
                    continue;
                }

                ((com.karaokepang.Midi.event.meta.Lyrics) event).setLyric(String.valueOf(nowCharacter));

                j++;
                if(j >= ksaLyricsArray.get(i).length()) {
                    i++;
                    j=0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        File musicFile = new File(uri.getPath());
        tv_songName.setText(MidiUtil.getSongName(musicFile));
        tv_composerName.setText(MidiUtil.getComposer(musicFile));
        tv_singer.setText(MidiUtil.getSinger(musicFile));
    }

    public void play(String songNumber) {
        if (activityController.getPangPangSelectActivity() != null) {
            activityController.getPangPangSelectActivity().startRecord(songNumber);
        }
        player.start();
        tickCounter();
    }

    public void stop() {
        player.stop();
    }

    @Background
    public void tickCounter() {
        long beforePosition = player.getCurrentPosition();
        int term = 32;

        while (true) {
            if(player.getCurrentPosition() - beforePosition > term) {

            }
        }
    }
}
