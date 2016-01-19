package com.duetwstar.Midi.renderer;

import android.graphics.Canvas;

import com.duetwstar.Midi.event.meta.Lyrics;

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
