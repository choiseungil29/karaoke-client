package com.clogic.karaokeviewer.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clogic.karaokeviewer.Midi.MidiTrack;
import com.clogic.karaokeviewer.Midi.event.MidiEvent;
import com.clogic.karaokeviewer.Midi.event.meta.Lyrics;
import com.clogic.karaokeviewer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by clogic on 2016. 1. 10..
 */
public class LyricsTextView extends LinearLayout {

    @Bind(R.id.tv_lyrics) TextView tv_lyrics;
    @Bind(R.id.tv_count) TextView tv_count;

    public MidiTrack lyricsTrack;
    public ArrayList<KSALyric> KSALyricsArray;
    public ArrayList<String> lyricsArray;
    public HashMap<String, Long> lyricsTimeTable;

    public LyricsTextView(Context context) {
        this(context, null);
    }

    public LyricsTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_lyrics_text, this, true);

        ButterKnife.bind(this);

        KSALyricsArray = new ArrayList<>();
        lyricsTimeTable = new HashMap<>();
    }

    public void setText(String text) {
        tv_lyrics.setText(text);
    }

    public class KSALyric {
        public String lyricLine;
        public long startTick;
        public long endTick;

        public KSALyric(String lyricLine, long startTick, long endTick) {
            this.lyricLine = lyricLine;
            this.startTick = startTick;
            this.endTick = endTick;
        }
    }
}