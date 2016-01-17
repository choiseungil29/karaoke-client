package com.clogic.karaokeviewer.Midi.renderer.midi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.clogic.karaokeviewer.Midi.util.MidiUtil;
import com.clogic.karaokeviewer.View.ScoreView;

/**
 * Created by clogic on 2015. 12. 13..
 */
public class NoteSymbol extends MidiSymbol {

    private int channel;
    private int noteValue;

    private boolean tie = false;

    public NoteSymbol(int startTicks, int noteValue, int channel) {
        this.startTicks = startTicks;
        this.noteValue = noteValue;
        this.channel = channel;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(ScoreView.LINE_STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        int r = ScoreView.resolution;
        //int y = 높이 구해주는 함수 MidiUtil에 생성
        int y = MidiUtil.getHeightFromNoteValue(noteValue);

        /*if(y > ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 4 ||
                y < ScoreView.FIRST_LINE_HEIGHT) {

            canvas.drawLine(-ScoreView.STEM_HEIGHT / 3 + 2, y,
                    ScoreView.STEM_HEIGHT / 3 - 2, y, paint);
        }*/

        if(duration == MidiUtil.Whole(r)) {
            drawWhole(canvas, paint, y);
        } else if(duration == MidiUtil.DotHalf(r)) {
            drawDotHalf(canvas, paint, y);
        } else if(duration == MidiUtil.Half(r)) {
            drawHalf(canvas, paint, y);
        } else if(duration == MidiUtil.DotQuarter(r)) {
            drawDotQuarter(canvas, paint, y);
        } else if(duration == MidiUtil.Quarter(r)) {
            drawQuarter(canvas, paint, y);
        /*} else if(duration == MidiUtil.DotEighth(r)) {
            drawDotEighth(canvas, paint, y);
        */} else if(duration == MidiUtil.Eighth(r)) {
            drawEighth(canvas, paint, y);
        } else if(duration == MidiUtil.Sixteenth(r)) {
            drawSixteenth(canvas, paint, y);
        }
    }

    public void drawWhole(Canvas canvas, Paint paint, int y) {
        paint.setStyle(Paint.Style.STROKE);
        for(int i=1; i<6; i++) {
            canvas.drawOval(new RectF(-ScoreView.LINE_SPACE_HEIGHT/2-i, -ScoreView.LINE_SPACE_HEIGHT/2 + y,
                    ScoreView.LINE_SPACE_HEIGHT/2+i, ScoreView.LINE_SPACE_HEIGHT/2 + y), paint);
        }
    }

    public void drawDotHalf(Canvas canvas, Paint paint, int y) {
        drawHalf(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT/2, y, ScoreView.LINE_SPACE_HEIGHT/4, paint);
    }

    public void drawHalf(Canvas canvas, Paint paint, int y) {
        paint.setStyle(Paint.Style.STROKE);
        for(int i=1; i<6; i++) {
            canvas.drawOval(new RectF(-ScoreView.LINE_SPACE_HEIGHT/2-i, -ScoreView.LINE_SPACE_HEIGHT/2 + y,
                    ScoreView.LINE_SPACE_HEIGHT/2+i, ScoreView.LINE_SPACE_HEIGHT/2 + y), paint);
        }

        if(MidiUtil.isTailTop(noteValue)) {
            canvas.drawLine(ScoreView.LINE_SPACE_HEIGHT/2+5, y,
                    ScoreView.LINE_SPACE_HEIGHT/2+5, y-ScoreView.STEM_HEIGHT, paint);
        } else {
            canvas.drawLine(-ScoreView.LINE_SPACE_HEIGHT/2-5, y,
                    -ScoreView.LINE_SPACE_HEIGHT/2-5, y+ScoreView.STEM_HEIGHT, paint);
        }
    }

    public void drawDotQuarter(Canvas canvas, Paint paint, int y) {
        drawQuarter(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT/2, y, ScoreView.LINE_SPACE_HEIGHT/4, paint);
    }

    public void drawQuarter(Canvas canvas, Paint paint, int y) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(new RectF(-ScoreView.LINE_SPACE_HEIGHT/2-3, -ScoreView.LINE_SPACE_HEIGHT/2 + y,
                ScoreView.LINE_SPACE_HEIGHT/2+3, ScoreView.LINE_SPACE_HEIGHT/2 + y), paint);

        if(MidiUtil.isTailTop(noteValue)) {
            canvas.drawLine(ScoreView.LINE_SPACE_HEIGHT/2+2, y,
                    ScoreView.LINE_SPACE_HEIGHT/2+2, y-ScoreView.STEM_HEIGHT, paint);
        } else {
            canvas.drawLine(-ScoreView.LINE_SPACE_HEIGHT/2-2, y,
                    -ScoreView.LINE_SPACE_HEIGHT/2-2, y+ScoreView.STEM_HEIGHT, paint);
        }
    }

    public void drawDotEighth(Canvas canvas, Paint paint, int y) {
        drawEighth(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT/2, y, ScoreView.LINE_SPACE_HEIGHT/4, paint);
    }

    public void drawEighth(Canvas canvas, Paint paint, int y) {
        drawQuarter(canvas, paint, y);
        paint.setStrokeWidth(ScoreView.LINE_STROKE*2);
        paint.setStyle(Paint.Style.STROKE);
        if(MidiUtil.isTailTop(noteValue)) {
            // top
            int xStart = ScoreView.LINE_SPACE_HEIGHT/2+2;
            int yStart = y-ScoreView.STEM_HEIGHT;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart + (3 * ScoreView.LINE_SPACE_HEIGHT) /2,
                            xStart + ScoreView.LINE_SPACE_HEIGHT*2, yStart + ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT/2,
                            xStart + ScoreView.LINE_SPACE_HEIGHT/2, yStart + ScoreView.LINE_SPACE_HEIGHT * 2 + ScoreView.LINE_SPACE_HEIGHT/2);
            canvas.drawPath(bezierPath, paint);
        } else {
            // down
            int xStart = -ScoreView.LINE_SPACE_HEIGHT/2-2;
            int yStart = y+ScoreView.STEM_HEIGHT;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart -ScoreView.LINE_SPACE_HEIGHT,
                    xStart + ScoreView.LINE_SPACE_HEIGHT*2, yStart - ScoreView.LINE_SPACE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT/2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT, yStart - ScoreView.LINE_SPACE_HEIGHT * 2 - ScoreView.LINE_SPACE_HEIGHT/2);
            canvas.drawPath(bezierPath, paint);
        }
    }

    public void drawSixteenth(Canvas canvas, Paint paint, int y) {
        drawEighth(canvas, paint, y);
        if(MidiUtil.isTailTop(noteValue)) {
            int xStart = ScoreView.LINE_SPACE_HEIGHT/2+2;
            int yStart = y-ScoreView.STEM_HEIGHT + ScoreView.LINE_SPACE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT/4;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart + (3 * ScoreView.LINE_SPACE_HEIGHT) /2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT*2, yStart + ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT/2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT/2, yStart + ScoreView.LINE_SPACE_HEIGHT * 2 + ScoreView.LINE_SPACE_HEIGHT/2);
            canvas.drawPath(bezierPath, paint);
        } else {
            // down
            int xStart = -ScoreView.LINE_SPACE_HEIGHT/2-2;
            int yStart = y+ScoreView.STEM_HEIGHT - ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT/4;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart -ScoreView.LINE_SPACE_HEIGHT,
                    xStart + ScoreView.LINE_SPACE_HEIGHT*2, yStart - ScoreView.LINE_SPACE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT/2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT, yStart - ScoreView.LINE_SPACE_HEIGHT * 2 - ScoreView.LINE_SPACE_HEIGHT/2);
            canvas.drawPath(bezierPath, paint);
        }
    }

    public int getChannel() { return channel; }
    public int getNoteValue() { return noteValue; }

    /**
     * 다음 마디의 음표와의 이음줄이 필요하면 해당 함수 호출
     */
    public void needToTie() {
        tie = true;
    }

    public boolean isTie() { return tie; }
}
