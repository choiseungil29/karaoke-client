package com.karaokepang.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.karaokepang.Model.KSALyric;
import com.karaokepang.Model.KSALyrics;
import com.karaokepang.Util.Logger;

import java.util.ArrayList;

/**
 * Created by clogic on 2016. 1. 15..
 */
public class OutlineTextView extends TextView {

    public float width = 0;
    public int index = 0;
    public long tick = 0;

    public ArrayList<KSALyrics> KSALyricsArray;
    public ArrayList<String> lyricsArray;

    private Typeface font;

    public OutlineTextView(Context context) {
        this(context, null);
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        KSALyricsArray = new ArrayList<>();
        font = Typeface.createFromAsset(getContext().getAssets(), "BMJUA_ttf.ttf");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setStrokeWidth(10);

        String lines = getText().toString();
        int i = 0;
        getPaint().setTypeface(font);
        for (String line : lines.split("\n")) {
            getPaint().setStyle(Paint.Style.STROKE);
            getPaint().setColor(Color.BLACK);
            canvas.drawText(line, 0, line.length(), 0, getTextSize() * (i + 1), getPaint());
            getPaint().setStyle(Paint.Style.FILL);
            getPaint().setColor(Color.WHITE);
            canvas.drawText(line, 0, line.length(), 0, getTextSize() * (i + 1), getPaint());
            i++;
        }
        canvas.clipRect(0, 0, width, 1000);
        Log.e("kkk", "width = " + width);
        try {
            String line = lines.split("\n")[index];
            getPaint().setStyle(Paint.Style.FILL);
            getPaint().setColor(Color.parseColor("#ff7f50"));
            canvas.drawText(line, 0, line.length(), 0, getTextSize() * (index + 1), getPaint());
        } catch (Exception e) {
            e.printStackTrace();
            String line = lines;
            getPaint().setStyle(Paint.Style.FILL);
            getPaint().setColor(Color.parseColor("#ff7f50"));
            canvas.drawText(line, 0, line.length(), 0, getTextSize() * (index + 1), getPaint());
        }
    }

    public void setTick(float tick, int lyricsIndex) {
        KSALyrics lyrics = KSALyricsArray.get(lyricsIndex);

        index = lyricsIndex % 2;
        KSALyric target = lyrics.lyricList.get(0);
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (KSALyric lyric : lyrics.lyricList) {
            if (lyric.startTick <= tick) {
                target = lyric;
                builder.append(lyric.lyric);
                i++;
            }
        }

        Rect completeRect = new Rect();
        Rect letterRect = new Rect();
        Rect spaceRect = new Rect();

        getPaint().getTextBounds(" ", 0, 1, spaceRect);

        try {
            getPaint().getTextBounds(builder.toString(), 0, i, completeRect);
            getPaint().getTextBounds(target.lyric, 0, target.lyric.length(), letterRect);
            float temp = completeRect.width() + letterRect.width() * (tick - target.startTick) / ((float) target.endTick - target.startTick);
            if (width < temp) {
                width = temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        callOnDraw();
    }

    public void callOnDraw() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
    }
}
