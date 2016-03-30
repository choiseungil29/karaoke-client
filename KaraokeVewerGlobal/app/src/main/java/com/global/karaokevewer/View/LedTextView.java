package com.global.karaokevewer.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.global.karaokevewer.R;

import org.androidannotations.annotations.EView;

@EView
public class LedTextView extends TextView {

    public LedTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyTypeface(context, attrs);
    }

    public LedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyTypeface(context, attrs);

    }

    public LedTextView(Context context) {
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
        }

        Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.translate_alpha);
        this.setAnimation(anim);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(20);
        setTextColor(Color.YELLOW);
        super.onDraw(canvas);

        getPaint().setStyle(Paint.Style.FILL);
        setTextColor(Color.CYAN);

        super.onDraw(canvas);
    }
}