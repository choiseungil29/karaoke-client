package com.karaokepang.Midi.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.Tempo;
import com.karaokepang.View.ScoreView;

import java.util.List;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class StaffSymbol extends Symbol {

    private int height;

    private List<MeasureSymbol> measures;

    private ClefSymbol clef;

    // C5 (60)
    public StaffSymbol(Context context, int staffWidth, int staffHeight, MidiTrack track, List<MeasureSymbol> measures) {
        this.width = staffWidth;
        this.height = staffHeight;

        clef = new ClefSymbol(context, track);

        for (MidiEvent event : track.getEvents()) {
            for (MeasureSymbol measure : measures) {
                if(event instanceof Tempo &&
                        event.getTick() <= measure.startTicks) {
                    measure.BPM = ((Tempo) event).getBpm();
                }
            }
        }

        this.measures = measures;
    }

    public void drawHorizontalLines(Canvas canvas) {

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(ScoreView.LINE_STROKE);

        int y = ScoreView.FIRST_LINE_HEIGHT;
        for(int i=0; i<5; i++) {
            canvas.drawLine(0, y, width, y, paint);
            y += ScoreView.LINE_SPACE_HEIGHT;
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
    }
}
