package com.clogic.karaokeviewer.Midi.renderer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.clogic.karaokeviewer.Midi.event.meta.TimeSignature;
import com.clogic.karaokeviewer.R;
import com.clogic.karaokeviewer.View.ScoreView;

import java.util.HashMap;

/**
 * Created by clogic on 2015. 12. 12..
 */
public class TimeSignatureSymbol extends Symbol {

    private TimeSignature time;
    private int height = ScoreView.LINE_SPACE_HEIGHT * 2;

    public TimeSignatureSymbol(TimeSignature time) {
        this.time = time;
        this.width = ScoreView.LINE_SPACE_HEIGHT * 2;
        this.height = ScoreView.LINE_SPACE_HEIGHT * 2;
    }

    @Override
    public void draw(Canvas canvas) {

        Bitmap numerator = com.clogic.karaokeviewer.Util.Resources.timeSignatureTable.get(time.getNumerator());
        Bitmap denominator = com.clogic.karaokeviewer.Util.Resources.timeSignatureTable.get(time.getRealDenominator());

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect src = new Rect(0, 0, numerator.getWidth(), numerator.getHeight());
        Rect dst = new Rect(0, 0, width, height);
        canvas.translate(0, ScoreView.FIRST_LINE_HEIGHT);
        canvas.drawBitmap(numerator, src, dst, paint);
        canvas.translate(0, -ScoreView.FIRST_LINE_HEIGHT);

        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);
        canvas.drawLine(0, ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 2,
                        width, ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 2, paint);

        src = new Rect(0, 0, denominator.getWidth(), denominator.getHeight());
        dst = new Rect(0, 0, width, height);
        canvas.translate(0, ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 2);
        canvas.drawBitmap(denominator, src, dst, paint);
        canvas.translate(0, -(ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 2));
    }
}
