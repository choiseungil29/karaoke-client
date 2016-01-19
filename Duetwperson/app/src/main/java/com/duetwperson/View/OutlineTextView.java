package com.duetwperson.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import com.duetwperson.Model.KSALyric;
import com.duetwperson.Model.KSALyrics;

/**
 * Created by clogic on 2016. 1. 15..
 */
public class OutlineTextView extends TextView {

    public float width = 0;
    public int index = 0;
    public long tick = 0;

    public OutlineTextView(Context context) {
        this(context, null);
    }

    public OutlineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OutlineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(3);
        getPaint().setColor(Color.GREEN);
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
        getPaint().setColor(Color.BLACK);

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

    public void setTick(int index, long tick, KSALyrics ksaLyrics) {
        this.index = index;
        this.tick = tick;

        width = 0;

        KSALyric nowLyric = null;
        int i;
        for(i=0; i<ksaLyrics.lyricList.size(); i++) {
            KSALyric lyric = ksaLyrics.lyricList.get(i);
            if(lyric.startTick <= tick && lyric.endTick >= tick) {
                nowLyric = lyric;
                break;
            }
        }

        String line;
        try {
            line = getText().toString().split("\n")[index];
        } catch (Exception e) {
            e.printStackTrace();
            line = getText().toString();
        }

        float term = tick - nowLyric.startTick; // term이 점점 커진다.
        float delta = nowLyric.endTick - nowLyric.startTick;

        Rect lineBounds = new Rect();
        Rect fullBounds = new Rect();
        getPaint().getTextBounds(line, 0, line.length(), fullBounds);
        getPaint().getTextBounds(line.substring(0, i), 0, i, lineBounds);
        width += lineBounds.width();

        float percent = term/delta; // 현재 단어의 진행도(percent)를 구함.
        Rect nowLetterBounds = new Rect();
        getPaint().getTextBounds(nowLyric.lyric, 0, 1, nowLetterBounds);

        width += percent * nowLetterBounds.width();
    }

    public void callOnDraw() {
        this.invalidate();
    }
}
