package com.clogic.karaokeviewer.View;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.clogic.karaokeviewer.Activity.MusicListener;
import com.clogic.karaokeviewer.Activity.TestActivity;
import com.clogic.karaokeviewer.Midi.MidiFile;
import com.clogic.karaokeviewer.Midi.MidiTrack;
import com.clogic.karaokeviewer.Midi.event.MidiEvent;
import com.clogic.karaokeviewer.Midi.event.PitchBend;
import com.clogic.karaokeviewer.Midi.event.meta.Lyrics;
import com.clogic.karaokeviewer.Midi.event.meta.TimeSignature;
import com.clogic.karaokeviewer.Midi.renderer.KeySignatureSymbol;
import com.clogic.karaokeviewer.Midi.renderer.MeasureSymbol;
import com.clogic.karaokeviewer.Midi.renderer.StaffSymbol;
import com.clogic.karaokeviewer.Midi.renderer.Symbol;
import com.clogic.karaokeviewer.Midi.renderer.TimeSignatureSymbol;
import com.clogic.karaokeviewer.Util.Logger;
import com.clogic.karaokeviewer.Util.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class ScoreView extends SurfaceView implements SurfaceHolder.Callback {

    private MidiFile midi = null;

    private ArrayList<MidiTrack> renderTracks;
    private MidiTrack signTrack;
    private MidiTrack renderTrack;
    private MidiTrack lyricsTrack;

    public static int LINE_SPACE_HEIGHT; // 오선 사이의 공간
    public static int LINE_STROKE;
    public static int STEM_HEIGHT;
    public static int FIRST_LINE_HEIGHT; // 오선 맨 윗줄 높이
    public static int DEFAULT_C = 128; // 가장 기본이 되는 도의 위치. 중간에 계산되어지고 변경된다.
    public static final int MEASURE_LIMIT = 2;

    public static int resolution = 0;

    private MusicListener listener;

    private ScoreThread thread = new ScoreThread();

    private MediaPlayer player = new MediaPlayer();

    private List<MeasureSymbol> measures;
    private List<MeasureSymbol> nowMeasures;
    private MeasureSymbol nowMeasure;

    private String fileName;
    private StringBuilder lyrics;
    private String soneName;
    private String composer;

    private Uri uri = null;

    private TestActivity activity;

    public ScoreView(Context context) {
        this(context, null);
    }

    public ScoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        activity = (TestActivity) context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        Resources.initResources(context);
        renderTracks = new ArrayList<>();
        measures = new ArrayList<>();
        //player.set
    }

    public void setFileUri(Uri uri) {
        this.uri = uri;
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void setMidiFile(MidiFile midi, String fileName) {
        this.midi = midi;
        this.fileName = fileName;

        Logger.i("filename : " + this.fileName);

        signTrack = this.midi.getTracks().get(0);

        for (int i = 0; i < midi.getTracks().size(); i++) {
            Iterator<MidiEvent> it = midi.getTracks().get(i).getEvents().iterator();
            int save = -1;
            while (it.hasNext()) {
                MidiEvent event = it.next();

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("melody")) {
                    renderTracks.add(midi.getTracks().get(i));
                    save = i;
                }

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("kasa")) {
                    renderTracks.add(midi.getTracks().get(i));
                    lyricsTrack = midi.getTracks().get(i);
                }

                if (save != -1 &&
                        !(event instanceof PitchBend)) {
                    Logger.i("TEST", event.toString());
                }
            }
        }

        for (MidiEvent event : renderTracks.get(0).getEvents()) {
            Logger.i("KASA Event : " + event.toString());
        }

        try {
            AssetManager assets = this.getResources().getAssets();
            for (String path : assets.list("")) {
                if (path.endsWith(".KSA") && path.startsWith(fileName)) {
                    Uri uri = Uri.parse("file:///android_asset/" + path);
                    Logger.i("file uri : " + uri.getLastPathSegment());
                    Logger.i("file uri : " + uri.toString());
                    InputStream is = getResources().getAssets().open(path);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "euc-kr"));
                    String line;
                    lyrics = new StringBuilder();
                    int count = 0;
                    while ((line = reader.readLine()) != null) {
                        if (count < 4) {
                            count++;
                            continue;
                        }
                        lyrics.append(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<MidiEvent> lyricsIt = lyricsTrack.getEvents().iterator();
        int lyricsIndex = 0;
        int eventLyricsSize = 0;
        while (lyricsIt.hasNext()) {
            MidiEvent event = lyricsIt.next();
            if (event instanceof Lyrics) {
                //Logger.i("lyricsList : " + ((Lyrics) event).getLyric());
                //Logger.i("lyricsList : " + ((Lyrics) event).getLyric().length());
                //((Lyrics) event).setLyric(String.valueOf(lyrics.charAt(lyricsIndex)));
                try {
                    //((Lyrics) event).setLyric(String.valueOf(lyricsList.substring(lyricsIndex + ((Lyrics) event).getLyric().length())));
                } catch (Exception e) {
                    //((Lyrics) event).setLyric(String.valueOf(lyricsList.substring(lyricsIndex + ((Lyrics) event).getLyric().length() - 1)));
                }
                //lyricsIndex += ((Lyrics) event).getLyric().length();
                if (((Lyrics) event).getLyric().length() == 0) {
                    ((Lyrics) event).setLyric("\n");
                    continue;
                }
                if (((Lyrics) event).getLyric().length() == 1) {
                    ((Lyrics) event).setLyric("\r");
                    continue;
                }
                if (((Lyrics) event).getLyric().length() == 2) {
                    try {
                        ((Lyrics) event).setLyric(new String(((Lyrics) event).getLyric().getBytes(), "euc-kr"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                if (((Lyrics) event).getLyric().length() == 4) {
                    ((Lyrics) event).setLyric(String.valueOf(lyrics.charAt(lyricsIndex)) + lyrics.charAt(lyricsIndex + 1));
                    lyricsIndex++;
                }
                if (((Lyrics) event).getLyric().length() == 3) {
                    ((Lyrics) event).setLyric(String.valueOf(lyrics.charAt(lyricsIndex)));
                }
                /*try {
                    ((Lyrics) event).setLyric(new String(((Lyrics) event).getLyric().getBytes(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/

                lyricsIndex++;
                eventLyricsSize += ((Lyrics) event).getLyric().length();
                Logger.i("" + ((Lyrics) event).getLyric().length());
            }
        }

        renderTrack = new MidiTrack();
        for (MidiTrack track : renderTracks) {
            Iterator<MidiEvent> it = track.getEvents().iterator();
            while (it.hasNext()) {
                MidiEvent event = it.next();
                renderTrack.insertEvent(event);
            }
        }

        // 이벤트 다 렌더 트랙으로 옮긴다
        Iterator<MidiEvent> it = signTrack.getEvents().iterator();
        while (it.hasNext()) {
            MidiEvent event = it.next();
            renderTrack.insertEvent(event);
        }
    }

    public void callOnDraw() {
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        onDraw(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        canvas.drawRect(0, 0, width, height, paint);

        StaffSymbol staffSymbol = new StaffSymbol(getContext(), width, height / 2, renderTrack, nowMeasures);
        staffSymbol.draw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LINE_SPACE_HEIGHT = getMeasuredHeight() / 40;
        LINE_STROKE = getMeasuredHeight() / 300;
        FIRST_LINE_HEIGHT = LINE_SPACE_HEIGHT * 3;
        STEM_HEIGHT = LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT / 2;
        this.resolution = midi.getResolution();

        createMeasures(renderTrack);
        settingMeasures();

        try {
            AssetFileDescriptor afd = getContext().getResources().getAssets().openFd(uri.getLastPathSegment());
            player.reset();
            player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            player.prepare();
            player.start();
            activity.startRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nowMeasure = measures.get(0);
        nowMeasures = measures.subList(0, 2);
        callOnDraw();
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        player.stop();
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }

    private void createMeasures(MidiTrack track) {
        measures = new ArrayList<>();

        Iterator<MidiEvent> it = track.getEvents().iterator();
        int nowTicks = 0;
        int measureLength = 0;
        MeasureSymbol measure = new MeasureSymbol();
        while (it.hasNext()) {
            MidiEvent e = it.next();

            while (measure.endTicks != 0 &&
                    measure.endTicks <= e.getTick()) {
                nowTicks = measure.endTicks;
                measure.created();
                measures.add(measure);
                measure = new MeasureSymbol();
                measure.startTicks = nowTicks;
                measure.endTicks = nowTicks + measureLength;
            }

            if (measure.endTicks > e.getTick()) {
                if (!(e instanceof TimeSignature)) {
                    measure.addSymbol(e);
                    continue;
                }
            }
            if (e instanceof TimeSignature) {
                measureLength = ScoreView.resolution * 4 * ((TimeSignature) e).getNumerator() / ((TimeSignature) e).getRealDenominator();
                measure.startTicks = nowTicks;
                measure.endTicks = nowTicks + measureLength;
                measure.addSymbol(e);
            }
        }

        Logger.i("created measure count : " + measures.size());
    }

    private void settingMeasures() {
        for (MeasureSymbol measure : measures) {
            measure.setWidth(getMeasuredWidth() / ScoreView.MEASURE_LIMIT);
            for (Symbol symbol : measure.getAllSymbols()) {
                if (symbol instanceof KeySignatureSymbol) {
                    measure.paddingLeft += symbol.getWidth();
                }
                if (symbol instanceof TimeSignatureSymbol) {
                    measure.paddingLeft += symbol.getWidth();
                }
            }
        }
    }

    public void setListener(MusicListener listener) {
        this.listener = listener;
    }

    public class ScoreThread extends Thread {

        private int measureCount = 0;

        @Override
        public void run() {
            long currentMillis = System.currentTimeMillis();
            long startMills = currentMillis;
            long endMills;
            while (true) {
                if (System.currentTimeMillis() - currentMillis >
                        ((60 / nowMeasure.BPM) * ((nowMeasure.endTicks - nowMeasure.startTicks) / resolution)) * 1000) {
                    Logger.i("CHECK TIME", "times : " + (System.currentTimeMillis() - currentMillis));

                    currentMillis = System.currentTimeMillis();
                    if (measureCount >= measures.size()) {
                        endMills = System.currentTimeMillis();

                        Logger.i("duration : " + (endMills - startMills));
                        return;
                    }
                    nowMeasure = measures.get(measureCount);
                    if (measureCount % 2 == 0) {
                        nowMeasures = measures.subList(measureCount, measureCount + 2);
                        ArrayList<String> list = new ArrayList<>();
                        try {
                            list.add(nowMeasures.get(0).lyrics);
                            list.add(nowMeasures.get(1).lyrics);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        listener.notifyMeasureChanged(list);
                    }

                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);

                    callOnDraw();

                    measureCount++;
                }
            }
        }
    }
}