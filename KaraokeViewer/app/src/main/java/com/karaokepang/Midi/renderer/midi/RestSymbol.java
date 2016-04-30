package com.karaokepang.Midi.renderer.midi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Midi.util.MidiUtil;

/**
 * Created by clogic on 2015. 12. 13..
 */
public class RestSymbol extends MidiSymbol {

    public RestSymbol(int startTicks, int duration) {
        this.startTicks = startTicks;
        this.endTicks = startTicks + duration;
        this.duration = duration;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(MidiInfo.LINE_STROKE);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);

        int r = MidiInfo.resolution;
        if(duration == MidiUtil.Whole(r)) {
            drawWhole(canvas, paint);
        } else if(duration == MidiUtil.DotHalf(r)) {
            drawDotHalf(canvas, paint);
        } else if(duration == MidiUtil.Half(r)) {
            drawHalf(canvas, paint);
        /*} else if(duration == MidiUtil.DotQuarter(r)) {
            drawDotQuarter(canvas, paint);
        */} else if(duration == MidiUtil.Quarter(r)) {
            drawQuarter(canvas, paint);
        /*} else if(duration == MidiUtil.DotEighth(r)) {
            drawDotEighth(canvas, paint);
        */} else if(duration == MidiUtil.Eighth(r)) {
            drawEighth(canvas, paint);
        } else if(duration == MidiUtil.Sixteenth(r)) {
            drawSixteenth(canvas, paint);
        }
    }

    public void drawWhole(Canvas canvas, Paint paint) {
        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT;
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(-MidiInfo.LINE_SPACE_HEIGHT, y, MidiInfo.LINE_SPACE_HEIGHT, y + MidiInfo.LINE_SPACE_HEIGHT/2, paint);
    }

    public void drawDotHalf(Canvas canvas, Paint paint) {
        drawHalf(canvas, paint);

        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2;
        canvas.drawCircle(MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2, y, MidiInfo.LINE_SPACE_HEIGHT/5, paint);
    }

    public void drawHalf(Canvas canvas, Paint paint) {
        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 2;
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(-MidiInfo.LINE_SPACE_HEIGHT, y, MidiInfo.LINE_SPACE_HEIGHT, y - MidiInfo.LINE_SPACE_HEIGHT/2, paint);
    }

    public void drawDotQuarter(Canvas canvas, Paint paint) {
        drawQuarter(canvas, paint);

        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 2 + MidiInfo.LINE_SPACE_HEIGHT/2;
        canvas.drawCircle(MidiInfo.LINE_SPACE_HEIGHT, y, MidiInfo.LINE_SPACE_HEIGHT/5, paint);
    }

    public void drawQuarter(Canvas canvas, Paint paint) {
        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2;

        int tailHeight = MidiInfo.LINE_SPACE_HEIGHT/4 + MidiInfo.LINE_SPACE_HEIGHT/2;

        canvas.drawLine(0, y, MidiInfo.LINE_SPACE_HEIGHT/2, y + tailHeight, paint);
        /*canvas.drawLine(0, y, StaffSymbol.LINE_SPACE_HEIGHT/2-1, y + StaffSymbol.LINE_SPACE_HEIGHT, paint);
        canvas.drawLine(0, y, StaffSymbol.LINE_SPACE_HEIGHT/2-2, y + StaffSymbol.LINE_SPACE_HEIGHT, paint);*/

        paint.setStrokeWidth(MidiInfo.LINE_STROKE * 3);
        canvas.drawLine(MidiInfo.LINE_SPACE_HEIGHT/2, y + tailHeight, 0, y + tailHeight*2, paint);

        paint.setStrokeWidth(MidiInfo.LINE_STROKE * 1);
        canvas.drawLine(0, y + tailHeight*2, MidiInfo.LINE_SPACE_HEIGHT/2, y + tailHeight*3, paint);

        // 두번째선이랑 세번째 선 사이의 좌측으로 drawLine
        paint.setStrokeWidth(MidiInfo.LINE_STROKE * 3);
        canvas.drawLine(MidiInfo.LINE_SPACE_HEIGHT/2, y + tailHeight*3, - tailHeight/2, y + tailHeight*3 - tailHeight/4, paint);

        paint.setStrokeWidth(MidiInfo.LINE_STROKE * 1);
        canvas.drawLine(-tailHeight/2, y + tailHeight*3 - tailHeight/4,
                        tailHeight/4, y + tailHeight*4, paint);
    }

    private void drawDotEighth(Canvas canvas, Paint paint) {
        drawEighth(canvas, paint);
        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 2 + MidiInfo.LINE_SPACE_HEIGHT/2;
        canvas.drawCircle(MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2, y, MidiInfo.LINE_SPACE_HEIGHT/5, paint);
    }

    private void drawEighth(Canvas canvas, Paint paint) {
        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2;
        float radius = (MidiInfo.LINE_SPACE_HEIGHT-3)/3;

        canvas.drawCircle(0, y, radius, paint);
        canvas.drawLine(0, y + radius, MidiInfo.LINE_SPACE_HEIGHT/2 + MidiInfo.LINE_SPACE_HEIGHT/2, y - radius, paint);
        canvas.drawLine(MidiInfo.LINE_SPACE_HEIGHT/2 + MidiInfo.LINE_SPACE_HEIGHT/2,
                y - radius,
                (MidiInfo.LINE_SPACE_HEIGHT/2 + MidiInfo.LINE_SPACE_HEIGHT)/4,
                y + MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2, paint);
    }

    private void drawSixteenth(Canvas canvas, Paint paint) {
        int y = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2;
        float radius = (MidiInfo.LINE_SPACE_HEIGHT-3)/3;

        canvas.drawCircle(0, y, radius, paint);
        canvas.drawLine(0, y + radius, MidiInfo.LINE_SPACE_HEIGHT/2 + MidiInfo.LINE_SPACE_HEIGHT/2, y - radius, paint);

        canvas.drawCircle(-radius, y + MidiInfo.LINE_SPACE_HEIGHT, radius, paint);
        canvas.drawLine(-radius, y + MidiInfo.LINE_SPACE_HEIGHT + radius, MidiInfo.LINE_SPACE_HEIGHT/2 + MidiInfo.LINE_SPACE_HEIGHT/2 - radius, y + radius, paint);

        canvas.drawLine(MidiInfo.LINE_SPACE_HEIGHT/2 + MidiInfo.LINE_SPACE_HEIGHT/2,
                y - radius,
                (MidiInfo.LINE_SPACE_HEIGHT/2 + MidiInfo.LINE_SPACE_HEIGHT)/4,
                y + MidiInfo.LINE_SPACE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT/2, paint);
    }
}