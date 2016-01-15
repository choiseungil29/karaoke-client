package com.clogic.karaokeviewer.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clogic.karaokeviewer.Midi.MidiTrack;
import com.clogic.karaokeviewer.Model.KSALyric;
import com.clogic.karaokeviewer.Model.KSALyrics;
import com.clogic.karaokeviewer.R;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by clogic on 2016. 1. 10..
 */
public class LyricsTextView extends LinearLayout {

    @Bind(R.id.tv_lyrics) OutlineTextView tv_lyrics;
    @Bind(R.id.tv_count) TextView tv_count;

    public ArrayList<KSALyrics> KSALyricsArray;
    public ArrayList<String> lyricsArray;

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
    }

    public void setText(String text) {
        tv_lyrics.setText(text);
    }

    public void setTick(long tick) {
        for(int i=0; i<KSALyricsArray.size(); i++) {
            KSALyrics ksaLyrics = KSALyricsArray.get(i);

            for(int j=0; j<ksaLyrics.lyricList.size(); j++) {
                KSALyric ksaLyric = ksaLyrics.lyricList.get(j);

                if(ksaLyric.startTick <= tick && ksaLyric.endTick >= tick) {
                    try {
                        tv_lyrics.setTick(i % 2, tick, ksaLyrics);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }
}