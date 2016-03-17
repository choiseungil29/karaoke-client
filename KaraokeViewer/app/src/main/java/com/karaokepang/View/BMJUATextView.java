package com.karaokepang.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.androidannotations.annotations.EView;

@EView
public class BMJUATextView extends TextView {

    public BMJUATextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyTypeface(context, attrs);
    }

    public BMJUATextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeface(context, attrs);
    }

    public BMJUATextView(Context context) {
        super(context);
    }

    private void applyTypeface(Context context, AttributeSet attrs) {
//        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.BMJUATextView);
        String typefaceName = "BMJUA_ttf.ttf";
        Typeface typeface = null;
        try {
            typeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
            setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(3);
        setTextColor(Color.WHITE);
        super.onDraw(canvas);

        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(Color.BLACK);

        super.onDraw(canvas);
    }
}