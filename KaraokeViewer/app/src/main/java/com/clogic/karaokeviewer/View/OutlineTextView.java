package com.clogic.karaokeviewer.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import com.clogic.karaokeviewer.Model.KSALyric;
import com.clogic.karaokeviewer.Model.KSALyrics;
import com.clogic.karaokeviewer.Util.Logger;

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

    public OutlineTextView(Context context) {
        this(context, null);
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        KSALyricsArray = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(3);
        getPaint().setColor(Color.BLACK);
        setTextColor(Color.BLACK);

        String lines = getText().toString();
        int i=0;
        for(String line : lines.split("\n")) {
            canvas.drawText(line, 0, line.length(), 0, getTextSize() * (i+1), getPaint());
            i++;
        }

        Rect textRect = new Rect();
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), textRect);

        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setColor(Color.BLUE);

        canvas.clipRect(0, 0, width, 1000);

        try {
            String line = lines.split("\n")[index];
            canvas.drawText(line, 0, line.length(), 0, getTextSize() * (index + 1), getPaint());
        } catch (Exception e) {
            e.printStackTrace();
            String line = lines;
            canvas.drawText(line, 0, line.length(), 0, getTextSize() * (index + 1), getPaint());
        }
    }

    public void setTick(long tick, int lyricsIndex) {
        KSALyrics lyrics = KSALyricsArray.get(lyricsIndex);

        index = lyricsIndex%2;
        KSALyric target = lyrics.lyricList.get(0);
        StringBuilder builder = new StringBuilder();
        int i=0;
        for(KSALyric lyric : lyrics.lyricList) {
            if(lyric.startTick <= tick) {
                target = lyric;
                builder.append(lyric.lyric);
                i++;
            }
        }
        Logger.i("string check : " + builder.toString());

        Rect completeRect = new Rect();
        Rect letterRect = new Rect();
        Rect spaceRect = new Rect();

        getPaint().getTextBounds(" ", 0, 1, spaceRect);

        int spaceCount = builder.toString().length() - builder.toString().replaceAll(" ", "").length();

        try {
            getPaint().getTextBounds(builder.toString(), 0, i, completeRect);
            getPaint().getTextBounds(target.lyric, 0, 1, letterRect);
            width = spaceRect.width() * spaceCount + completeRect.width() + letterRect.width() * ((float) tick - target.startTick) / ((float) target.endTick - target.startTick);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(lyrics.lyricLine.equals("Let's get out out")) {
            Logger.i("Let's get out out");
            Logger.i("start tick : " + lyrics.startTick + " end tick : " + lyrics.endTick);
            Logger.i("builder : " + builder.toString());
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
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
