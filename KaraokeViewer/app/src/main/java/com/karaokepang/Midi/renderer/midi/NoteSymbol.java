package com.karaokepang.Midi.renderer.midi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Midi.util.MidiUtil;

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

    public float standardY = 0;
    private float append = 0;

    private float y = 0;

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
        paint.setStrokeWidth(MidiInfo.LINE_STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        int r = MidiInfo.resolution;

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
            float standardY = y;
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
                canvas.drawLine(MidiInfo.LINE_SPACE_HEIGHT / 2, y - MidiInfo.STEM_HEIGHT - append,
                        MidiInfo.LINE_SPACE_HEIGHT / 2 + segment * (getDuration() / (MidiInfo.resolution/4)) + 2, y - MidiInfo.STEM_HEIGHT - append, paint);
            } else {
                canvas.drawLine(-MidiInfo.LINE_SPACE_HEIGHT / 2, y + MidiInfo.STEM_HEIGHT + append,
                        -MidiInfo.LINE_SPACE_HEIGHT / 2 + segment * (getDuration() / (MidiInfo.resolution/4)) + 2, y + MidiInfo.STEM_HEIGHT + append, paint);
            }

            if(MidiUtil.Sixteenth(MidiInfo.resolution) == this.getDuration()) {
                if (isTailTop) {
                    canvas.drawLine(MidiInfo.LINE_SPACE_HEIGHT / 2,
                            y - MidiInfo.STEM_HEIGHT - append + paint.getStrokeWidth() * 2,
                            MidiInfo.LINE_SPACE_HEIGHT / 2 + segment * (getDuration() / (MidiInfo.resolution/4)) + 2,
                            y - MidiInfo.STEM_HEIGHT - append + paint.getStrokeWidth() * 2, paint);
                } else {
                    canvas.drawLine(-MidiInfo.LINE_SPACE_HEIGHT / 2,
                            y + MidiInfo.STEM_HEIGHT + append - paint.getStrokeWidth() * 2,
                            -MidiInfo.LINE_SPACE_HEIGHT / 2 + segment * (getDuration() / (MidiInfo.resolution/4)) + 2,
                            y + MidiInfo.STEM_HEIGHT + append - paint.getStrokeWidth() * 2, paint);
                }
            }
        }

        if (MidiUtil.needToPointLine(noteValue)) {
            drawPointLine(canvas, paint);
        }
    }

    private void drawPointLine(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(MidiInfo.LINE_STROKE);
        //canvas.drawLine(-ScoreView.LINE_SPACE_HEIGHT / 2 - 8, y, +ScoreView.LINE_SPACE_HEIGHT / 2 + 8, y, paint);
        if(noteValue%(MidiInfo.DEFAULT_C - MidiInfo.OCTAVE) <= MidiInfo.OCTAVE) {
            float y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 5;
            while (y <= this.y) {
                canvas.drawLine(-MidiInfo.LINE_SPACE_HEIGHT / 2 - 8, y, +MidiInfo.LINE_SPACE_HEIGHT / 2 + 8, y, paint);
                y += MidiInfo.LINE_SPACE_HEIGHT;
            }
        }
        if(noteValue%(MidiInfo.DEFAULT_C - MidiInfo.OCTAVE) >= MidiInfo.OCTAVE + 9) {
            float y = MidiInfo.FIRST_LINE_HEIGHT - (float) MidiInfo.LINE_SPACE_HEIGHT;
            while (y >= this.y) {
                canvas.drawLine(-MidiInfo.LINE_SPACE_HEIGHT / 2 - 8, y, +MidiInfo.LINE_SPACE_HEIGHT / 2 + 8, y, paint);
                y -= MidiInfo.LINE_SPACE_HEIGHT;
            }
        }
    }

    public void drawWhole(Canvas canvas, Paint paint, float y) {
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i < 4; i++) {
            canvas.rotate(-CIRCLE_ROUTE, 0, y);
            canvas.drawOval(new RectF(-MidiInfo.LINE_SPACE_HEIGHT / 2 - i, -MidiInfo.LINE_SPACE_HEIGHT / 2 + y,
                    MidiInfo.LINE_SPACE_HEIGHT / 2 + i, MidiInfo.LINE_SPACE_HEIGHT / 2 + y), paint);
            canvas.rotate(CIRCLE_ROUTE, 0, y);
        }
    }

    public void drawDotHalf(Canvas canvas, Paint paint, float y) {
        drawHalf(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT / 2, y, MidiInfo.LINE_SPACE_HEIGHT / 4, paint);
    }

    public void drawHalf(Canvas canvas, Paint paint, float y) {
        paint.setStyle(Paint.Style.STROKE);
        drawWhole(canvas, paint, y);
        drawStem(canvas, paint, y);
    }

    public void drawDotQuarter(Canvas canvas, Paint paint, float y) {
        drawQuarter(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT / 2, y, MidiInfo.LINE_SPACE_HEIGHT / 4, paint);
    }

    public void drawQuarter(Canvas canvas, Paint paint, float y) {
        paint.setStyle(Paint.Style.FILL);

        canvas.rotate(-CIRCLE_ROUTE, 0, y);
        canvas.drawOval(new RectF(-MidiInfo.LINE_SPACE_HEIGHT / 2 - 3, -MidiInfo.LINE_SPACE_HEIGHT / 2 + y,
                MidiInfo.LINE_SPACE_HEIGHT / 2 + 3, MidiInfo.LINE_SPACE_HEIGHT / 2 + y), paint);
        canvas.rotate(CIRCLE_ROUTE, 0, y);
        drawStem(canvas, paint, y);
    }

    public void drawDotEighth(Canvas canvas, Paint paint, float y) {
        drawEighth(canvas, paint, y);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT / 2, y, MidiInfo.LINE_SPACE_HEIGHT / 4, paint);
    }

    public void drawEighth(Canvas canvas, Paint paint, float y) {
        drawQuarter(canvas, paint, y);
        paint.setStrokeWidth(MidiInfo.LINE_STROKE * 2);
        paint.setStyle(Paint.Style.STROKE);
        if (isTailTop) {
            // top
            int xStart = MidiInfo.LINE_SPACE_HEIGHT / 2;
            float yStart = y - MidiInfo.STEM_HEIGHT;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart + (3 * MidiInfo.LINE_SPACE_HEIGHT) / 2,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT * 2, yStart + MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT / 2,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT / 2, yStart + MidiInfo.LINE_SPACE_HEIGHT * 2 + MidiInfo.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        } else {
            // down
            int xStart = -MidiInfo.LINE_SPACE_HEIGHT / 2;
            float yStart = y + MidiInfo.STEM_HEIGHT;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart - MidiInfo.LINE_SPACE_HEIGHT,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT * 2, yStart - MidiInfo.LINE_SPACE_HEIGHT - MidiInfo.LINE_SPACE_HEIGHT / 2,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT, yStart - MidiInfo.LINE_SPACE_HEIGHT * 2 - MidiInfo.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        }
    }

    public void drawSixteenth(Canvas canvas, Paint paint, float y) {
        drawEighth(canvas, paint, y);
        if (isTailTop) {
            int xStart = MidiInfo.LINE_SPACE_HEIGHT / 2 + 2;
            float yStart = y - MidiInfo.STEM_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT - MidiInfo.LINE_SPACE_HEIGHT / 4;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart + (3 * MidiInfo.LINE_SPACE_HEIGHT) / 2,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT * 2, yStart + MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT / 2,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT / 2, yStart + MidiInfo.LINE_SPACE_HEIGHT * 2 + MidiInfo.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        } else {
            // down
            int xStart = -MidiInfo.LINE_SPACE_HEIGHT / 2 - 2;
            float yStart = y + MidiInfo.STEM_HEIGHT - MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT / 4;
            Path bezierPath = new Path();
            bezierPath.moveTo(xStart, yStart);
            bezierPath.cubicTo(xStart, yStart - MidiInfo.LINE_SPACE_HEIGHT,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT * 2, yStart - MidiInfo.LINE_SPACE_HEIGHT - MidiInfo.LINE_SPACE_HEIGHT / 2,
                    xStart + MidiInfo.LINE_SPACE_HEIGHT, yStart - MidiInfo.LINE_SPACE_HEIGHT * 2 - MidiInfo.LINE_SPACE_HEIGHT / 2);
            canvas.drawPath(bezierPath, paint);
        }
    }

    public void drawStem(Canvas canvas, Paint paint, float y) {
        if (isTailTop) {
            canvas.drawLine(MidiInfo.LINE_SPACE_HEIGHT / 2 + 1, y,
                    MidiInfo.LINE_SPACE_HEIGHT / 2 + 1, y - MidiInfo.STEM_HEIGHT - append, paint);
        } else {
            canvas.drawLine(-MidiInfo.LINE_SPACE_HEIGHT / 2 - 1, y - 1,
                    -MidiInfo.LINE_SPACE_HEIGHT / 2 - 1, y + MidiInfo.STEM_HEIGHT + append, paint);
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
