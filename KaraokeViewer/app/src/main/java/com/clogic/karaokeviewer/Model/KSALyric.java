package com.clogic.karaokeviewer.Model;

/**
 * Created by clogic on 2016. 1. 15..
 */
public class KSALyric {

    public String lyric;
    public long startTick;
    public long endTick;

    public KSALyric(String lyric, long startTick, long endTick) {
        this.lyric = lyric;
        this.startTick = startTick;
        this.endTick = endTick;
    }

    public KSALyric(String lyric) {
    }
}
