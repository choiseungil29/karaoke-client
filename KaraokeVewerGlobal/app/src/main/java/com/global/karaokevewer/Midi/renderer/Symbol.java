package com.global.karaokevewer.Midi.renderer;

import android.graphics.Canvas;

/**
 * Created by clogic on 2015. 12. 10..
 */
public abstract class Symbol {

    private int leftMargin;
    protected int width;

    public abstract void draw(Canvas canvas);
    public void draw(Canvas canvas, int x) {
        canvas.translate(x, 0);
        draw(canvas);
        canvas.translate(-x, 0);
    };

    public void setLeftMargin(int leftMargin) { this.leftMargin = leftMargin; }
    public int getLeftMargin() { return leftMargin; }

    public void setWidth(int width) { this.width = width; }
    public int getWidth() { return width; }
}
