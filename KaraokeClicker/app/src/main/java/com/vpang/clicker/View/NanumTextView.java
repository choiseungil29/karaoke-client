package com.vpang.clicker.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class NanumTextView extends TextView {

    public NanumTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyTypeface(context, attrs);
    }

    public NanumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeface(context, attrs);
    }

    public NanumTextView(Context context) {
        super(context);
    }

    private void applyTypeface(Context context, AttributeSet attrs) {
        String typefaceName = "NanumGothic.ttf";
        Typeface typeface = null;
        try {
            typeface = Typeface.createFromAsset(context.getAssets(), typefaceName);
            setTypeface(typeface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}