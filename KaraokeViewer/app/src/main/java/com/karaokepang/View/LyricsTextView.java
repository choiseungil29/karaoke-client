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
import com.karaokepang.Model.Lyrics;
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

    Lyrics top;
    Lyrics bottom;

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
        top = new Lyrics();
        bottom = new Lyrics();

        this.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();
        paint.setTextSize(getTextSize());
        paint.setStrokeWidth(10);
        paint.setTypeface(font);
        paint.setColor(Color.BLACK);

        canvas.save();
        //if(top.getIndex() < top.getLyrics().size()) {
            drawLyrics(canvas, top, getWidth() / 4, getTextSize());
        //}
        canvas.restore();
        //if(bottom.getIndex() < bottom.getLyrics().size()) {
            drawLyrics(canvas, bottom, getWidth() / 2, getTextSize() * 2);
        //}
        canvas.restore();
    }

    public void initLyrics(List<Lyric> lyrics) {
        String parent = lyrics.get(0).getParent();
        int i = 0;
        List<Lyric> top = new ArrayList<>();
        List<Lyric> bot = new ArrayList<>();
        for (Lyric lyric : lyrics) {
            if (!lyric.getParent().equals(parent)) {
                if (i % 2 == 0) {
                    this.top.add(top);
                    top = new ArrayList<>();
                } else {
                    this.bottom.add(bot);
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
        calculateLyricsIndex(tick, top);
        calculateLyricsIndex(tick, bottom);

        calculateLyricsWidth(tick, top);
        calculateLyricsWidth(tick, bottom);
    }

    private void calculateLyricsIndex(float tick, Lyrics lyrics) {
        for(int i=0; i<lyrics.getLyrics().size(); i++) {
            if(tick >= lyrics.getLyrics().get(i).get(
                    lyrics.getLyrics().get(i).size()-1).getEndTick()) {
                lyrics.setIndex(i+1);
                lyrics.setWidth(0);
            }
        }
    }

    private void calculateLyricsWidth(float tick, Lyrics lyrics) {
        List<Lyric> oneLine = lyrics.getLyrics().get(lyrics.getIndex());

        int space = 0;
        for(int i=0; i<oneLine.size(); i++) {
            Lyric lyric = oneLine.get(i);
            if(lyric.getParent().charAt(i + space) == ' ') {
                space++;
            }
            if(tick >= lyric.getStartTick() &&
                    tick <= lyric.getEndTick()) {
                String text = lyric.getParent().substring(0, i + space);
                StringBuilder textBuilder = new StringBuilder();
                for(int j=0; j<i; j++) {
                    textBuilder.append(oneLine.get(j).getText());
                }
                for(int j=0; j<space; j++) {
                    textBuilder.append(" ");
                }
                String lastIndex = lyric.getParent().substring(i + space, i + lyric.getText().length() + space);
                float widthPercent = (tick - lyric.getStartTick()) /
                        (lyric.getEndTick() - lyric.getStartTick());
                float textWidth = getPaint().measureText(text);
                textWidth = getPaint().measureText(textBuilder.toString());
                lyrics.setWidth(textWidth + ((int)getPaint().measureText(lastIndex)) * widthPercent);
            }
        }
    }

    private void drawLyrics(Canvas canvas, Lyrics lyrics, float x, float y) {
        Paint paint = new Paint(getPaint());
        canvas.drawText(lyrics.getLyrics().get(lyrics.getIndex()).get(0).getParent(), x, y, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#ff7f50"));

        canvas.clipRect(0, 0, x + lyrics.getWidth(), 10000);
        canvas.drawText(lyrics.getLyrics().get(lyrics.getIndex()).get(0).getParent(), x, y, paint);
    }

    @UiThread
    public void callOnDraw(float tick) {
        invalidate();
    }
}
