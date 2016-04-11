package com.global.karaokevewer.Model;

import java.util.ArrayList;

/**
 * Created by clogic on 2016. 1. 12..
 *
 */
public class KSALyrics {
    public String lyricLine;
    public long startTick;
    public long endTick;

    public ArrayList<KSALyric> lyricList;

    public KSALyrics(String lyricLine, long startTick, long endTick) {
        this.lyricLine = lyricLine;
        this.startTick = startTick;
        this.endTick = endTick;
    }

    public KSALyrics(ArrayList<KSALyric> lyricList, String lyricLine) {
        this.lyricList = lyricList;
        this.lyricLine = lyricLine;
        create();
    }

    public KSALyrics() {
        lyricList = new ArrayList<>();
        lyricLine = new String();
    }

    public void create() {
        long startTick = 1000000;
        long endTick = 0;
        for(KSALyric lyric : lyricList) {
            if(lyric.getStartTick() < startTick) {
                startTick = lyric.getStartTick();
                this.startTick = startTick;
            }
            if(lyric.getEndTick() > endTick) {
                endTick = lyric.getEndTick();
                this.endTick = endTick;
            }
        }
    }
}