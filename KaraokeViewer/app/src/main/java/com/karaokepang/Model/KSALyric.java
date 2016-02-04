package com.karaokepang.Model;

import com.karaokepang.View.ScoreView;

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
        /*if(endTick - startTick > ScoreView.resolution * 4) {
            this.endTick = this.startTick + ScoreView.resolution * 4;
        }*/
    }
}
