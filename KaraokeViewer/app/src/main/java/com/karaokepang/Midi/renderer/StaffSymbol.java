package com.karaokepang.Midi.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.Tempo;
import com.karaokepang.Util.Logger;
import com.karaokepang.View.ScoreView;

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
                if(event instanceof Tempo &&
                        event.getTick() >= measure.startTicks &&
                        event.getTick() < measure.endTicks) {
                    measure.tempoList.add((Tempo) event);
                }
                /*if(event instanceof Tempo) {
                    Logger.i("tempo log : " + event.toString());
                }*/
            }
        }

        this.measures = measures;

        this.startTick = this.measures.get(0).startTicks;
        this.endTick = this.measures.get(measures.size()-1).endTicks;
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

        Paint paint = new Paint();
        paint.setStrokeWidth(ScoreView.LINE_STROKE * 2);
        float progress = 0;
        if(this.startTick >= this.nowTick &&
                this.endTick < this.nowTick) {
            progress = (this.nowTick - this.startTick) / (this.endTick - this.startTick);

            canvas.drawLine(progress * width, ScoreView.FIRST_LINE_HEIGHT,
                    progress * width + 1, ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 4, paint);

            Logger.i("progress : " + progress);
        }
    }
}
