package com.global.karaokevewer.Midi.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.global.karaokevewer.Midi.MidiTrack;
import com.global.karaokevewer.Midi.util.MidiInfo;

import java.util.List;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class StaffSymbol extends Symbol {

    private int height;
    private List<MeasureSymbol> measures;
    private ClefSymbol clef;

    public float nowTick;

    private long startTick;
    private long endTick;

    private float progress = 0.0f;

    public int leftPadding;
    public int topPadding;
    public int rightPadding;
    public int bottomPadding;

    // C5 (60)
    public StaffSymbol(Context context, int staffWidth, int staffHeight, MidiTrack track, List<MeasureSymbol> measures) {
        this.width = staffWidth;
        this.height = staffHeight;

        clef = new ClefSymbol(context, track);

        this.measures = measures;
        this.startTick = this.measures.get(0).startTicks;
        this.endTick = this.measures.get(measures.size()-1).endTicks;
    }

    public void drawHorizontalLines(Canvas canvas) {

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(MidiInfo.LINE_STROKE);

        int y = MidiInfo.FIRST_LINE_HEIGHT;
        for(int i=0; i<5; i++) {
            canvas.drawLine(0, y, width, y, paint);
            y += MidiInfo.LINE_SPACE_HEIGHT;
        }
    }

    public void draw(Canvas canvas) {
        clef.draw(canvas);
        int x = 0;

        canvas.translate(clef.getWidth() + clef.getLeftMargin(), 0);
        for(int i=0; i<measures.size(); i++) {
            measures.get(i).draw(canvas, x);
            x += measures.get(i).getWidth();
        }
        canvas.translate(-(clef.getWidth() + clef.getLeftMargin()), 0);
        drawHorizontalLines(canvas);

        Paint paint = new Paint();
        paint.setStrokeWidth(MidiInfo.LINE_STROKE * 3);
        paint.setColor(Color.BLUE);
        if(this.startTick <= this.nowTick &&
                this.endTick > this.nowTick) {
            float temp = (this.nowTick - this.startTick) / (this.endTick - this.startTick);
            if(progress < temp) {
                progress = temp;
            }

            canvas.drawLine(progress * (width - clef.getWidth()) + clef.getWidth(), MidiInfo.FIRST_LINE_HEIGHT - MidiInfo.LINE_SPACE_HEIGHT * 3,
                    progress * (width + 1 - clef.getWidth()) + clef.getWidth(), MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 7, paint);
        }
    }
}
