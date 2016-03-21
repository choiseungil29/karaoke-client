package com.karaokepang.Midi.renderer;

import android.graphics.Canvas;

import com.karaokepang.Midi.event.meta.MidiLyrics;

/**
 * Created by clogic on 2015. 12. 31..
 */
public class LyricSymbol extends Symbol {

    MidiLyrics midiLyrics;

    public LyricSymbol(MidiLyrics midiLyrics) {
        this.midiLyrics = midiLyrics;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
