package com.karaokepang.Midi.renderer;

import android.graphics.Canvas;

import com.karaokepang.Midi.event.meta.Lyrics;

/**
 * Created by clogic on 2015. 12. 31..
 */
public class LyricSymbol extends Symbol {

    Lyrics lyrics;

    public LyricSymbol(Lyrics lyrics) {
        this.lyrics = lyrics;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
