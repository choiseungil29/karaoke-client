package com.karaokepang.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.TextView;

import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.MidiLyrics;
import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Model.Lyric;
import com.karaokepang.Model.Lyrics;
import com.karaokepang.Util.Logger;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EView;
import org.androidannotations.annotations.UiThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by clogic on 16. 3. 20..
 */
@EView
public class LyricsTextView extends TextView {
    private Typeface font;

    Lyrics top;
    Lyrics bottom;

    private List<String> ksaLyricsArray = new ArrayList<>();

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

        Paint backgroundPaint = getPaint();
        backgroundPaint.setColor(((ColorDrawable)getBackground()).getColor());
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if(top.getIndex() >= top.getLyrics().size()) {
            canvas.drawText("", getWidth()/4, getTextSize(), paint);
        } else {
            canvas.save();
            drawLyrics(canvas, top, getWidth() / 4, getTextSize());
        }
        if(bottom.getIndex() >= bottom.getLyrics().size()) {
            canvas.drawText("", getWidth() / 2, getTextSize() * 2, paint);
        } else {
            canvas.restore();
            drawLyrics(canvas, bottom, getWidth() / 2, getTextSize() * 2);
            canvas.restore();
        }
    }

    public void initLyrics(List<Lyric> lyrics) {
        String parent = lyrics.get(0).getParent();
        int n = 0;
        List<Lyric> top = new ArrayList<>();
        List<Lyric> bot = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<lyrics.size(); i++) {
            Lyric lyric = lyrics.get(i);
            //if (!lyric.getParent().equals(parent)) {
            if(parent.replaceAll(" ", "").equals(sb.toString())) {
                if (n % 2 == 0) {
                    this.top.add(top);
                    top = new ArrayList<>();
                } else {
                    this.bottom.add(bot);
                    bot = new ArrayList<>();
                }
                sb = new StringBuilder();
                parent = lyric.getParent();
                n++;
            }
            if (n % 2 == 0) {
                top.add(lyric);
            } else {
                bot.add(lyric);
            }
            sb.append(lyric.getText());
        }
        if (n % 2 == 0) {
            this.top.add(top);
        } else {
            this.bottom.add(bot);
        }
        sb = new StringBuilder();
    }

    @Background
    public void update(float tick) {
        calculateLyricsIndex(tick, top);
        calculateLyricsIndex(tick, bottom);

        calculateLyricsWidth(tick, top);
        calculateLyricsWidth(tick, bottom);
    }

    // 여기를 수정
    private void calculateLyricsIndex(float tick, Lyrics lyrics) {
        for(int i=0; i<lyrics.getLyrics().size()-1; i++) {
            if(tick >= lyrics.getLyrics().get(i).get(
                    lyrics.getLyrics().get(i).size() - 1).getEndTick() &&
                    tick < lyrics.getLyrics().get(i+1).get(
                            lyrics.getLyrics().get(i+1).size() -1).getEndTick())  {
                if(lyrics.getIndex() == i) {
                    lyrics.setIndex(i + 1);
                    lyrics.setWidth(0);
                }
            }
        }
    }

    private void calculateLyricsWidth(float tick, Lyrics lyrics) {
        List<Lyric> oneLine = lyrics.getLyrics().get(lyrics.getIndex());

        if(lyrics.getIndex() >= lyrics.getLyrics().size()) {
            return;
        }

        int space = 0;
        for(int i=0; i<oneLine.size(); i++) {
            Lyric lyric = oneLine.get(i);
            try {
                if (lyric.getParent().charAt(i + space) == ' ') {
                    space++;
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                space--;
            }
            if(tick >= lyric.getStartTick() &&
                    tick <= lyric.getEndTick()) {
                StringBuilder textBuilder = new StringBuilder();
                for(int j=0; j<i; j++) {
                    textBuilder.append(oneLine.get(j).getText());
                }
                for(int j=0; j<space; j++) {
                    textBuilder.append(" ");
                }
                String lastIndex = null;
                try {
                    lastIndex = lyric.getParent().substring(i + space, i + lyric.getText().length() + space);
                } catch (IndexOutOfBoundsException e) {
                    lastIndex = lyric.getParent().substring(i + space, lyric.getParent().length());
                    e.printStackTrace();
                }
                float widthPercent = (tick - lyric.getStartTick()) /
                        (lyric.getEndTick() - lyric.getStartTick());
                float textWidth = getPaint().measureText(textBuilder.toString());
                float temp = textWidth + ((int)getPaint().measureText(lastIndex)) * widthPercent;
                if(temp > lyrics.getWidth()) {
                    lyrics.setWidth(temp);
                    if(oneLine.get(0).getParent().replaceAll(" ", "").equals("너를보낼때부터")) {
                        Logger.i("get width : " + lyrics.getWidth());
                    }
                }
            }
        }
    }

    private void drawLyrics(Canvas canvas, Lyrics lyrics, float x, float y) {
        Paint strokePaint = new Paint(getPaint());
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);

        canvas.drawText(lyrics.getLyrics().get(lyrics.getIndex()).get(0).getParent(), x, y, strokePaint);

        Paint paint = new Paint(getPaint());
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
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

    public void initLyrics(MidiTrack lyricsTrack) {
        Iterator<MidiEvent> lyricsIt = lyricsTrack.getEvents().iterator();
        int i = 0;
        int j = 0;
        String eng;

        List<MidiLyrics> midiLyrics = new ArrayList<>();
        int n=0;
        try {
            while (lyricsIt.hasNext()) {
                MidiEvent event = lyricsIt.next();

                //Logger.i("text : " + ((MidiLyrics)event).getLyric());

                if(n == 582) {
                    Logger.i("asdfagq3egSibbal");
                }
                if (!(event instanceof MidiLyrics)) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().equals("\r")) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().equals("\n")) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().equals("")) {
                    continue;
                }
                if (((MidiLyrics) event).getLyric().endsWith(" ")) {
                    ((MidiLyrics) event).setLyric(
                            ((MidiLyrics) event).getLyric().substring(
                                    0, ((MidiLyrics) event).getLyric().length()-1));
                }

                char nowCharacter = ksaLyricsArray.get(i).charAt(j);
                if (nowCharacter == '@' ||
                        nowCharacter == '#') {
                    i++;
                    j = 0;
                }

                if (nowCharacter == ' ') {
                    j++;
                }

                nowCharacter = ksaLyricsArray.get(i).charAt(j);
                eng = "";
                char start = nowCharacter;
                if (start >= 'a' && start <= 'z' ||
                        start >= 'A' && start <= 'Z') {
                    while (nowCharacter != ' ' &&
                            nowCharacter != '\n' &&
                            nowCharacter != '\r' &&
                            nowCharacter != '^' &&
                            nowCharacter != '#' &&
                            nowCharacter != '@') {
                        if (eng.trim().length() < ((MidiLyrics) event).getLyric().trim().length()) {
                            eng += nowCharacter;
                            j++;
                            if (j >= ksaLyricsArray.get(i).length()) {
                                i++;
                                j = 0;
                            }
                            if(i >= ksaLyricsArray.size()) {
                                break;
                            }
                            nowCharacter = ksaLyricsArray.get(i).charAt(j);
                        } else {
                            break;
                        }
                    }
                }

                if (eng.length() > 0) {
                    ((MidiLyrics) event).setLyric(eng);
                    //Logger.i("text : " + ((MidiLyrics) event).getLyric());
                } else {
                    ((MidiLyrics) event).setLyric(String.valueOf(nowCharacter));
                    //Logger.i("text : " + ((MidiLyrics) event).getLyric());

                    j++;
                    if (j >= ksaLyricsArray.get(i).length()) {
                        i++;
                        j = 0;
                    }
                }

                midiLyrics.add((MidiLyrics) event);
                n++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 가사들 줄별로 정리해주기.
        // MidiLyrics -> Lyrics로 대입
        List<Lyric> lyrics = new ArrayList<>();
        MidiLyrics beforeLyric = null;
        i=0;
        StringBuilder sb = new StringBuilder();
        for(MidiLyrics lyric : midiLyrics) {
            if(beforeLyric != null) {
                long duration = lyric.getTick() - beforeLyric.getTick();
                if(duration > MidiInfo.resolution * 2) {
                    duration = MidiInfo.resolution * 2;
                }
                lyrics.add(new Lyric(ksaLyricsArray.get(i),
                        beforeLyric.getLyric(),
                        beforeLyric.getTick(),
                        beforeLyric.getTick() + duration));
                Logger.i("text : " + (beforeLyric) + ", start tick : " + beforeLyric.getTick() + ", end tick : " + (beforeLyric.getTick() + duration));
            }

            if(ksaLyricsArray.get(i).replaceAll(" ", "").equals(sb.toString())) {
                if(i < ksaLyricsArray.size() - 1) {
                    i++;
                    sb = new StringBuilder();
                }
            }
            sb.append(lyric.getLyric());
            beforeLyric = lyric;
        }
        lyrics.add(new Lyric(ksaLyricsArray.get(i),
                beforeLyric.getLyric(),
                beforeLyric.getTick(),
                beforeLyric.getTick() + MidiInfo.resolution * 2));

        this.initLyrics(lyrics);
    }

    public void loadKsaByMidi(Uri uri) {
        File lyricsFile = new File(uri.getPath().toLowerCase().replace(".mid", ".ksa"));
        if (lyricsFile.exists()) {
            try {
                InputStream is = new FileInputStream(lyricsFile);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "euc-kr"));

                String line;
                int count = 0;
                while ((line = reader.readLine()) != null) {
                    if (count < 4) {
                        count++;
                        continue;
                    }
                    if(line.equals("")) {
                        continue;
                    }
                    if(line.equals("#") || line.equals("@")) {
                        continue;
                    }
                    ksaLyricsArray.add(line);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
