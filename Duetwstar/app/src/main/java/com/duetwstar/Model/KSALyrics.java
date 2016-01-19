package com.duetwstar.Model;

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

    public KSALyrics(ArrayList<KSALyric> lyricList) {
        this.lyricList = lyricList;
    }
}