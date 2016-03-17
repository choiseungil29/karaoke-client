package com.karaokepang.Midi.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.karaokepang.Midi.event.meta.TimeSignature;
import com.karaokepang.View.BeforeScoreView;

/**
 * Created by clogic on 2015. 12. 12..
 */
public class TimeSignatureSymbol extends Symbol {

    private TimeSignature time;
    private int height = BeforeScoreView.LINE_SPACE_HEIGHT * 2;

    public TimeSignatureSymbol(TimeSignature time) {
        this.time = time;
        this.width = BeforeScoreView.LINE_SPACE_HEIGHT * 2;
        this.height = BeforeScoreView.LINE_SPACE_HEIGHT * 2;
    }

    @Override
    public void draw(Canvas canvas) {

        Bitmap numerator = com.karaokepang.Util.Resources.timeSignatureTable.get(time.getNumerator());
        Bitmap denominator = com.karaokepang.Util.Resources.timeSignatureTable.get(time.getRealDenominator());

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect src = new Rect(0, 0, numerator.getWidth(), numerator.getHeight());
        Rect dst = new Rect(0, 0, width, height);
        canvas.translate(0, BeforeScoreView.FIRST_LINE_HEIGHT);
        canvas.drawBitmap(numerator, src, dst, paint);
        canvas.translate(0, -BeforeScoreView.FIRST_LINE_HEIGHT);

        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        canvas.drawLine(0, BeforeScoreView.FIRST_LINE_HEIGHT + BeforeScoreView.LINE_SPACE_HEIGHT * 2,
                        width, BeforeScoreView.FIRST_LINE_HEIGHT + BeforeScoreView.LINE_SPACE_HEIGHT * 2, paint);

        src = new Rect(0, 0, denominator.getWidth(), denominator.getHeight());
        dst = new Rect(0, 0, width, height);
        canvas.translate(0, BeforeScoreView.FIRST_LINE_HEIGHT + BeforeScoreView.LINE_SPACE_HEIGHT * 2);
        canvas.drawBitmap(denominator, src, dst, paint);
        canvas.translate(0, -(BeforeScoreView.FIRST_LINE_HEIGHT + BeforeScoreView.LINE_SPACE_HEIGHT * 2));
    }
}
