package com.clogic.karaokeviewer.Model;

/**
 * Created by clogic on 2016. 1. 12..
 */
public class KSALyrics {
    public String lyricLine;
    public long startTick;
    public long endTick;

    public KSALyrics(String lyricLine, long startTick, long endTick) {
        this.lyricLine = lyricLine;
        this.startTick = startTick;
        this.endTick = endTick;
    }
}