package com.duetwstar.Midi.renderer.accidental;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.duetwstar.Midi.renderer.Symbol;
import com.duetwstar.View.ScoreView;

/**
 * Created by clogic on 2015. 12. 11..
 */
public class SharpSymbol extends Symbol {

    private int lineWidth;
    private int lineSpace;

    private int x;
    private int y;

    public SharpSymbol() {
        lineWidth = ScoreView.LINE_STROKE;
        lineSpace = ScoreView.LINE_SPACE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT/4;

        width = lineSpace + lineSpace/3;
    }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(Color.BLACK);

        int middleX = x + lineSpace/4;

        canvas.drawLine(middleX - lineSpace/4, y - lineSpace,
                        middleX - lineSpace/4, y + lineSpace, paint);
        canvas.drawLine(middleX + lineSpace/4, y - lineSpace,
                        middleX + lineSpace/4, y + lineSpace, paint);

        paint.setStrokeWidth(lineSpace/4);
        canvas.drawLine(middleX - lineSpace + lineSpace/3, y - lineSpace/4,
                        middleX + lineSpace - lineSpace/3, y - lineSpace/2, paint);
        canvas.drawLine(middleX - lineSpace + lineSpace/3, y + lineSpace/2,
                        middleX + lineSpace - lineSpace/3, y + lineSpace/4, paint);
    }
}
