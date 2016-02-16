package com.karaokepang.Midi.renderer.midi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.karaokepang.Midi.renderer.MeasureSymbol;
import com.karaokepang.Midi.util.MidiUtil;
import com.karaokepang.View.ScoreView;

/**
 * Created by clogic on 2015. 12. 13..
 */
public class NoteSymbol extends MidiSymbol {

    private final int CIRCLE_ROUTE = 30;
    private int channel;
    private int noteValue;

    private boolean tie = false;

    public NoteSymbol prev = null;
    public NoteSymbol next = null;

    public int standardY = 0;
    private int append = 0;

    private int y = 0;

    private boolean isTailTop = false;

    public NoteSymbol(int startTicks, int noteValue, int channel) {
        this.startTicks = startTicks;
        this.noteValue = noteValue;
        this.channel = channel;
        y = MidiUtil.getHeightFromNoteValue(noteValue);
        isTailTop = MidiUtil.isTailTop(noteValue);
        standardY = y;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(ScoreView.LINE_STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        int r = ScoreView.resolution;

        if (prev == null && next == null) {
            if (duration == MidiUtil.Whole(r)) {
                drawWhole(canvas, paint, y);
            } else if (duration == MidiUtil.DotHalf(r)) {
                drawDotHalf(canvas, paint, y);
            } else if (duration == MidiUtil.Half(r)) {
                drawHalf(canvas, paint, y);
            } else if (duration == MidiUtil.DotQuarter(r)) {
                drawDotQuarter(canvas, paint, y);
            } else if (duration == MidiUtil.Quarter(r)) {
                drawQuarter(canvas, paint, y);
            } else if (duration == MidiUtil.Eighth(r)) {
                drawEighth(canvas, paint, y);
            } else if (duration == MidiUtil.Sixteenth(r)) {
                drawSixteenth(canvas, paint, y);
            }
        }

        if (prev == null && next != null) {
            int standardY = y;
            NoteSymbol symbol = this;
            while (symbol != null) {
                symbol.isTailTop = this.isTailTop;
                if (isTailTop) {
                    if (standardY > symbol.y) {
                        standardY = symbol.y;
                    }
                } else {
                    if (standardY < symbol.y) {
                        standardY = symbol.y;
                    }
                }
                symbol = symbol.next;
            }
            this.standardY = standardY;

            symbol = this;
            while (symbol != null) {
                symbol.standardY = this.standardY;
                if (this.isTailTop) {
                    symbol.append = symbol.y - symbol.standardY;
                } else {
                    symbol.append = symbol.standardY - symbol.y;
                }
                symbol = symbol.next;
            }
        }

        if (prev != null) {
            drawQuarter(canvas, paint, y);
        }
        if (next != null) {
            drawQuarter(canvas, paint, y);
            paint.setStrokeWidth(5);
            if (isTailTop) {
                canvas.drawLine(ScoreView.LINE_SPACE_HEIGHT / 2, y - ScoreView.STEM_HEIGHT - append,
                        ScoreView.LINE_SPACE_HEIGHT / 2 + MeasureSymbol.segment, y - ScoreView.STEM_HEIGHT - append, paint);
            } else {
                canvas.drawLine(-ScoreView.LINE_SPACE_HEIGHT / 2, y + ScoreView.STEM_HEIGHT + append,
                        -ScoreView.LINE_SPACE_HEIGHT / 2 + MeasureSymbol.segment, y + ScoreView.STEM_HEIGHT + append, paint);
            }
        }
    }

    public void drawWhole(Canvas canvas, Paint paint, int y) {
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i < 6; i++) {
            canvas.rotate(-CIRCLE_ROUTE, ScoreView.LINE_SPACE_HEIGHT / 2, ScoreView.LINE_SPACE_HEIGHT / 2 + y);
            canvas.drawOval(new RectF(-ScoreView.LINE_SPACE_HEIGHT / 2 - i, -ScoreView.LINE_SPACE_HEIGHT / 2 + y,
                    ScoreView.LINE_SPACE_HEIGHT / 2 + i, ScoreView.LINE_SPACE_HEIGHT / 2 + y), paint);
            canvas.rotate(CIRCLE_ROUTE, ScoreView.LINE_SPACE_HEIGHT / 2, ScoreView.LINE_SPACE_HEIGHT / 2 + y);
        }
    }

    public void drawDotHalf(Canvas canvas, Paint paint, int y) {
        drawHalf(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT / 2, y, ScoreView.LINE_SPACE_HEIGHT / 4, paint);
    }

    public void drawHalf(Canvas canvas, Paint paint, int y) {
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i < 6; i++) {
            canvas.rotate(-CIRCLE_ROUTE, 0, y);
            canvas.drawOval(new RectF(-ScoreView.LINE_SPACE_HEIGHT / 2 - i, -ScoreView.LINE_SPACE_HEIGHT / 2 + y,
                    ScoreView.LINE_SPACE_HEIGHT / 2 + i, ScoreView.LINE_SPACE_HEIGHT / 2 + y), paint);
            canvas.rotate(CIRCLE_ROUTE, 0, y);
        }
        drawStem(canvas, paint, y);
    }

    public void drawDotQuarter(Canvas canvas, Paint paint, int y) {
        drawQuarter(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT / 2, y, ScoreView.LINE_SPACE_HEIGHT / 4, paint);
    }

    public void drawQuarter(Canvas canvas, Paint paint, int y) {
        paint.setStyle(Paint.Style.FILL);

        canvas.rotate(-CIRCLE_ROUTE, 0, y);
        canvas.drawOval(new RectF(-ScoreView.LINE_SPACE_HEIGHT / 2 - 3, -ScoreView.LINE_SPACE_HEIGHT / 2 + y,
                ScoreView.LINE_SPACE_HEIGHT / 2 + 3, ScoreView.LINE_SPACE_HEIGHT / 2 + y), paint);
        canvas.rotate(CIRCLE_ROUTE, 0, y);
        drawStem(canvas, paint, y);
    }

    public void drawDotEighth(Canvas canvas, Paint paint, int y) {
        drawEighth(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT / 2, y, ScoreView.LINE_SPACE_HEIGHT / 4, paint);
    }

    public void drawEighth(Canvas canvas, Paint paint, int y) {
        drawQuarter(canvas, paint, y);
        paint.setStrokeWidth(ScoreView.LINE_STROKE * 2);
        paint.setStyle(Paint.Style.STROKE);
        if (isTailTop) {
            // top
            int xStart = ScoreView.LINE_SPACE_HEIGHT / 2;
            int yStart = y - ScoreView.STEM_HEIGHT;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart + (3 * ScoreView.LINE_SPACE_HEIGHT) / 2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT * 2, yStart + ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT / 2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT / 2, yStart + ScoreView.LINE_SPACE_HEIGHT * 2 + ScoreView.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        } else {
            // down
            int xStart = -ScoreView.LINE_SPACE_HEIGHT / 2;
            int yStart = y + ScoreView.STEM_HEIGHT;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart - ScoreView.LINE_SPACE_HEIGHT,
                    xStart + ScoreView.LINE_SPACE_HEIGHT * 2, yStart - ScoreView.LINE_SPACE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT / 2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT, yStart - ScoreView.LINE_SPACE_HEIGHT * 2 - ScoreView.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        }
    }

    public void drawSixteenth(Canvas canvas, Paint paint, int y) {
        drawEighth(canvas, paint, y);
        if (isTailTop) {
            int xStart = ScoreView.LINE_SPACE_HEIGHT / 2 + 2;
            int yStart = y - ScoreView.STEM_HEIGHT + ScoreView.LINE_SPACE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT / 4;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart + (3 * ScoreView.LINE_SPACE_HEIGHT) / 2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT * 2, yStart + ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT / 2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT / 2, yStart + ScoreView.LINE_SPACE_HEIGHT * 2 + ScoreView.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        } else {
            // down
            int xStart = -ScoreView.LINE_SPACE_HEIGHT / 2 - 2;
            int yStart = y + ScoreView.STEM_HEIGHT - ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT / 4;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart - ScoreView.LINE_SPACE_HEIGHT,
                    xStart + ScoreView.LINE_SPACE_HEIGHT * 2, yStart - ScoreView.LINE_SPACE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT / 2,
                    xStart + ScoreView.LINE_SPACE_HEIGHT, yStart - ScoreView.LINE_SPACE_HEIGHT * 2 - ScoreView.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        }
    }

    public void drawStem(Canvas canvas, Paint paint, int y) {
        if (isTailTop) {
            canvas.drawLine(ScoreView.LINE_SPACE_HEIGHT / 2, y,
                    ScoreView.LINE_SPACE_HEIGHT / 2, y - ScoreView.STEM_HEIGHT - append, paint);
        } else {
            canvas.drawLine(-ScoreView.LINE_SPACE_HEIGHT / 2, y - 1,
                    -ScoreView.LINE_SPACE_HEIGHT / 2, y + ScoreView.STEM_HEIGHT + append, paint);
        }
    }

    public int getChannel() {
        return channel;
    }

    public int getNoteValue() {
        return noteValue;
    }

    /**
     * 다음 마디의 음표와의 이음줄이 필요하면 해당 함수 호출
     */
    public void needToTie() {
        tie = true;
    }

    public boolean isTie() {
        return tie;
    }
}
