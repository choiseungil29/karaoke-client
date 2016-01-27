package com.karaokepang.View;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by clogic on 2016. 1. 20..
 */
public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        this(context, null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    public void init(Context context) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(3);
        setTextColor(Color.WHITE);
        super.onDraw(canvas);

        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(Color.BLACK);

        super.onDraw(canvas);
    }
}
