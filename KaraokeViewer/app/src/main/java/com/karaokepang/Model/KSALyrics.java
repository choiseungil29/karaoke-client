package com.karaokepang.Model;

import com.karaokepang.Util.Logger;

import java.util.ArrayList;

/**
 * Created by clogic on 2016. 1. 12..
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
        long startTick = 1000000;
        long endTick = 0;
        for(KSALyric lyric : lyricList) {
            if(lyric.startTick < startTick) {
                startTick = lyric.startTick;
                this.startTick = startTick;
            }
            if(lyric.endTick > endTick) {
                endTick = lyric.endTick;
                this.endTick = endTick;
            }
        }
        this.lyricLine = lyricLine;
    }
}