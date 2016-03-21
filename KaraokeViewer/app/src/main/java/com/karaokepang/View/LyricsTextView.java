package com.karaokepang.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.karaokepang.Model.Lyric;
import com.karaokepang.Util.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by clogic on 16. 3. 20..
 */
@EView
public class LyricsTextView extends TextView {
    private Typeface font;

    private float topLyricsWidth = 0.0f;
    private float bottomLyricsWidth = 0.0f;

    private List<List<Lyric>> topLyrics = new ArrayList<>();
    private List<List<Lyric>> bottomLyrics = new ArrayList<>();

    private int topIdx = 0;
    private int bottomIdx = 0;

    public LyricsTextView(Context context) {
        this(context, null);
    }

    public LyricsTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @AfterViews
    public void afterViews() {
        font = Typeface.createFromAsset(getContext().getAssets(), "BMJUA_ttf.ttf");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Logger.i("call on draw : " + bottomLyricsWidth);
        Paint paint = getPaint();
        paint.setTextSize(getTextSize());
        paint.setStrokeWidth(10);
        paint.setTypeface(font);
        paint.setColor(Color.BLACK);

        canvas.drawText(topLyrics.get(topIdx).get(0).getParent(), getWidth() / 4, getTextSize(), paint);
        canvas.drawText(bottomLyrics.get(bottomIdx).get(0).getParent(), getWidth() / 2 + getWidth() / 8, getTextSize() * 2, paint);

        getPaint().setStyle(Paint.Style.FILL);
        getPaint().setColor(Color.parseColor("#ff7f50"));

        {
            canvas.save();
            canvas.clipRect(0, 0, getWidth() / 4 + topLyricsWidth, 10000);
            canvas.drawText(topLyrics.get(topIdx).get(0).getParent(), getWidth() / 4, getTextSize(), paint);
        }

        canvas.restore();

        {
            canvas.clipRect(0, 0, getWidth() / 2 + getWidth() / 8 + bottomLyricsWidth, 10000);
            canvas.drawText(bottomLyrics.get(bottomIdx).get(0).getParent(), getWidth() / 2 + getWidth() / 8, getTextSize() * 2, paint);
        }
    }

    public void initLyrics(List<Lyric> lyrics) {
        String parent = lyrics.get(0).getParent();
        int i = 0;
        List<Lyric> top = new ArrayList<>();
        List<Lyric> bot = new ArrayList<>();
        for (Lyric lyric : lyrics) {
            if (!lyric.getParent().equals(parent)) {
                if (i % 2 == 0) {
                    topLyrics.add(top);
                    top = new ArrayList<>();
                } else {
                    bottomLyrics.add(bot);
                    bot = new ArrayList<>();
                }
                parent = lyric.getParent();
                i++;
            }
            if (i % 2 == 0) {
                top.add(lyric);
            } else {
                bot.add(lyric);
            }
        }
    }

    @Background
    public void update(float tick) {
        for (int i = 0; i < topLyrics.size() - 1; i++) {
            if (tick > bottomLyrics.get(i).get(0).getEndTick()) {
                topIdx = i + 1;
            }
        }

        for (int i = 0; i < bottomLyrics.size() - 1; i++) {
            if (tick > topLyrics.get(i + 1).get(0).getEndTick()) {
                bottomIdx = i + 1;
            }
        }

        {
            StringBuilder sb = new StringBuilder();
            Lyric lastLyric = null;
            for (int i = 0; i < topLyrics.get(topIdx).size(); i++) {
                Lyric lyric = topLyrics.get(topIdx).get(i);

                if (tick > lyric.getEndTick()) {
                    sb.append(lyric.getText());
                }

                if (tick >= lyric.getStartTick() && tick < lyric.getEndTick()) {
                    lastLyric = lyric;
                }
            }

            if (lastLyric == null) {
                topLyricsWidth = 0;
            } else {

                Rect fullRect = new Rect();
                Rect currentLetterRect = new Rect();
                Rect spaceRect = new Rect();

                getPaint().getTextBounds(" ", 0, 1, spaceRect);
                getPaint().getTextBounds(sb.toString(), 0, sb.length(), fullRect);
                getPaint().getTextBounds(lastLyric.getText(), 0, lastLyric.getText().length(), currentLetterRect);

                int spaceCount = 0;
                for (int i = 0; i < lastLyric.getParent().length(); i++) {
                    if (sb.toString().length() >= i) {
                        break;
                    }

                    if (lastLyric.getParent().charAt(i - spaceCount) != sb.toString().charAt(i)) {
                        spaceCount++;
                    }
                }

                topLyricsWidth = fullRect.width() + currentLetterRect.width() *
                        (tick - lastLyric.getStartTick()) / (lastLyric.getEndTick() - lastLyric.getStartTick()) +
                        spaceRect.width() * spaceCount;
            }

        }

        {
            StringBuilder sb = new StringBuilder();
            Lyric lastLyric = null;
            for (int i = 0; i < bottomLyrics.get(bottomIdx).size(); i++) {
                Lyric lyric = bottomLyrics.get(bottomIdx).get(i);

                if (tick >= lyric.getEndTick()) {
                    sb.append(lyric.getText());
                }

                if (tick >= lyric.getStartTick() && tick < lyric.getEndTick()) {
                    lastLyric = lyric;
                }
            }

            if (lastLyric == null) {
                bottomLyricsWidth = 0;
            } else {
                Rect fullRect = new Rect();
                Rect currentLetterRect = new Rect();

                getPaint().getTextBounds(sb.toString(), 0, sb.length(), fullRect);
                getPaint().getTextBounds(lastLyric.getText(), 0, lastLyric.getText().length(), currentLetterRect);

                bottomLyricsWidth = fullRect.width() + currentLetterRect.width() *
                        (tick - lastLyric.getStartTick()) / (lastLyric.getEndTick() - lastLyric.getStartTick());
            }
        }
    }

    @UiThread
    public void callOnDraw(float tick) {
        invalidate();
    }
}
