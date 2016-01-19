package com.duetwperson.Midi.renderer.midi;

import android.graphics.Canvas;

import com.duetwperson.Midi.renderer.Symbol;

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

    public void setStartTicks(int ticks) { this.startTicks = ticks; }
    public int getStartTicks() { return startTicks; }
    public int getDuration() { return duration; }

    public void roundStartTicks() {
        double newStartTicks = this.startTicks;
        newStartTicks = newStartTicks/10;
        newStartTicks = Math.floor(newStartTicks + 0.5d) * 10;
        this.startTicks = (int)newStartTicks;

        setEndTicks(this.startTicks + this.duration);
    }

    public void setEndTicks(int ticks) {
        this.endTicks = ticks;
        this.duration = endTicks - startTicks;
    }
}
