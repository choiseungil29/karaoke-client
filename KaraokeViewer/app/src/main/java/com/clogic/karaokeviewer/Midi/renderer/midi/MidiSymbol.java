package com.clogic.karaokeviewer.Midi.renderer.midi;

import android.graphics.Canvas;

import com.clogic.karaokeviewer.Midi.renderer.Symbol;

/**
 * Created by clogic on 2015. 12. 13..
 */
public abstract class MidiSymbol extends Symbol {

    protected int startTicks;
    protected int endTicks;

    protected int duration = 0;


    @Override
    public void draw(Canvas canvas) {

    }

    public int getStartTicks() { return startTicks; }
    public int getDuration() { return duration; }

    public void setEndTicks(int ticks) {
        this.endTicks = ticks;
        this.duration = endTicks - startTicks;
    }
}
