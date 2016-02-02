package com.karaokepang.Midi.renderer.accidental;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.karaokepang.Midi.renderer.Symbol;
import com.karaokepang.View.ScoreView;

/**
 * Created by clogic on 2015. 12. 11..
 */
public class FlatSymbol extends Symbol {

    // 라인 너비
    private int lineWidth;
    private int lineSpace;

    private int x;
    private int y;

    public FlatSymbol() {
        lineWidth = ScoreView.LINE_STROKE;
        lineSpace = ScoreView.LINE_SPACE_HEIGHT;

        width = lineSpace + lineSpace/4;
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

        // 수직선 하나를 그린다
        canvas.drawLine(x, y - lineSpace - lineSpace/2 - lineSpace/2,
                        x, y + lineSpace - lineSpace/2, paint);

        // 베지어 곡선을 이용해서 그림
        // 세개를 그려서 플랫의 아랫쪽 살을 예쁘게 채움
        Path bezierPath = new Path();
        bezierPath.moveTo(x, y - lineSpace/2);
        bezierPath.cubicTo(
                x + lineSpace/2, -lineSpace/2 + y - lineSpace/2,
                x + lineSpace,    lineSpace/3 + y - lineSpace/2,
                x,                y + lineSpace - lineSpace/2);
        canvas.drawPath(bezierPath, paint);

        bezierPath = new Path();
        bezierPath.moveTo(x, y - lineSpace/2);
        bezierPath.cubicTo(
                x + lineSpace/2, -lineSpace/2 + y - lineSpace/2,
                x + lineSpace + lineSpace/4, y + lineSpace/3 + lineSpace/4 - lineSpace/2,
                x,                y + lineSpace - lineSpace/2);
        canvas.drawPath(bezierPath, paint);

        // 가장 너비가 넓게 그림
        bezierPath = new Path();
        bezierPath.moveTo(x, y - lineSpace/2);
        bezierPath.cubicTo(
                x + lineSpace/2, -lineSpace/2 + y - lineSpace/2,
                x + lineSpace + lineSpace/2, y + lineSpace/3 + lineSpace/2 - lineSpace/2,
                x,                y + lineSpace - lineSpace/2);
        canvas.drawPath(bezierPath, paint);
    }

}







