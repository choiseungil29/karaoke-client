package com.karaokepang.View;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import org.androidannotations.annotations.EView;

/**
 * Created by clogic on 2016. 1. 15..
 */
@EView
public class OverlayTextView extends TextView {

    public OverlayTextView(Context context) {
        super(context, null);
    }

    public OverlayTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ColorStateList states = getTextColors();
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(5);
        setTextColor(Color.RED);
        //super.onDraw(canvas);

        Rect textRect = new Rect();
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), textRect);

        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(states);

        canvas.drawText(getText(), 0, getText().length(), 0, 0, getPaint());

        Canvas canvas2 = new Canvas();
        canvas2.clipRect(0, 0, textRect.width()/2, getPaint().getTextSize());
        canvas2.drawText(getText(), 0, getText().length(), 0, 0, getPaint());
    }
}
