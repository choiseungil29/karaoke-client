package com.karaokepang.View;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.karaokepang.Activity.MusicListener;
import com.karaokepang.Activity.TestActivity;
import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.meta.Lyrics;
import com.karaokepang.Midi.event.meta.TimeSignature;
import com.karaokepang.Midi.renderer.KeySignatureSymbol;
import com.karaokepang.Midi.renderer.MeasureSymbol;
import com.karaokepang.Midi.renderer.StaffSymbol;
import com.karaokepang.Midi.renderer.Symbol;
import com.karaokepang.Midi.renderer.TimeSignatureSymbol;
import com.karaokepang.Midi.renderer.midi.MidiSymbol;
import com.karaokepang.Midi.renderer.midi.NoteSymbol;
import com.karaokepang.Util.Logger;
import com.karaokepang.Util.Resources;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class ScoreView extends SurfaceView implements SurfaceHolder.Callback {

    private TestActivity activity;

    private MidiFile midi = null;

    private ArrayList<MidiTrack> renderTracks;
    private MidiTrack signTrack;
    private MidiTrack renderTrack;
    public MidiTrack lyricsTrack;

    public static int LINE_SPACE_HEIGHT; // 오선 사이의 공간
    public static int LINE_STROKE;
    public static int STEM_HEIGHT;
    public static int FIRST_LINE_HEIGHT; // 오선 맨 윗줄 높이
    public static int DEFAULT_C = 128; // 가장 기본이 되는 도의 위치. 중간에 계산되어지고 변경된다.
    public static final int MEASURE_LIMIT = 4;

    public static int resolution = 0; // 한 박자의 단위길이

    private MusicListener listener;

    private ScoreThread thread = new ScoreThread();
    private MediaPlayer player = new MediaPlayer();

    private List<MeasureSymbol> measures;
    private List<MeasureSymbol> nowMeasures[] = new List[2];
    public MeasureSymbol nowMeasure;

    private String fileName;
    private StringBuilder lyrics;
    public ArrayList<String> lyricsArray;
    public String songName;
    public String composer;
    public String singer;

    public long startTick;
    public static int measureLength;

    private Uri uri = null;

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
        TestActivity activity = (TestActivity) context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        Resources.initResources(context);
        renderTracks = new ArrayList<>();
        measures = new ArrayList<>();
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
        resolution = midi.getResolution();

        signTrack = this.midi.getTracks().get(0);

        for (int i = 0; i < midi.getTracks().size(); i++) {
            Iterator<MidiEvent> it = midi.getTracks().get(i).getEvents().iterator();
            while (it.hasNext()) {
                MidiEvent event = it.next();

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("melody")) {
                    renderTracks.add(midi.getTracks().get(i));
                }

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("kasa")) {
                    //renderTracks.add(midi.getTracks().get(i));
                    lyricsTrack = midi.getTracks().get(i);
                }
            }
        }
        for (MidiEvent event : lyricsTrack.getEvents()) {
            //Logger.i("lyriccccccccccccccccccccccccc : " + event.toString());
        }

        try {
            AssetManager assets = this.getResources().getAssets();
            for (String path : assets.list("")) {
                if (path.endsWith(".KSA") && path.startsWith(fileName)) {
                    InputStream is = getResources().getAssets().open(path);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "euc-kr"));
                    String line;
                    lyrics = new StringBuilder();
                    lyricsArray = new ArrayList<>();
                    int count = 0;
                    while ((line = reader.readLine()) != null) {
                        switch (count) {
                            case 0:
                                songName = line;
                                break;
                            case 2:
                                composer = line;
                                break;
                            case 3:
                                singer = line;
                                break;
                            default:
                                break;
                        }
                        if (count == 0) {
                            songName = line;
                        }
                        if (count == 1) {

                        }
                        if (count < 4) {
                            count++;
                            continue;
                        }
                        lyricsArray.add(line);
                        lyrics.append(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<MidiEvent> lyricsIt = lyricsTrack.getEvents().iterator();
        int lyricsIndex = 0;
        try {
            while (lyricsIt.hasNext()) {
                MidiEvent event = lyricsIt.next();
                if (event instanceof Lyrics) {

                    if (((Lyrics) event).getLyric().equals("\r")) {
                        continue;
                    }
                    if (((Lyrics) event).getLyric().equals("\n")) {
                        continue;
                    }
                    if (((Lyrics) event).getLyric().equals("")) {
                        continue;
                    }

                    if (lyrics.charAt(lyricsIndex) == '@' ||
                            lyrics.charAt(lyricsIndex) == '#') {
                        lyricsIndex++;
                    }

                    if (lyrics.charAt(lyricsIndex) == ' ') {
                        lyricsIndex++;
                    }

                    String english = "";
                    char start = lyrics.charAt(lyricsIndex);
                    if (start >= 'a' && start <= 'z' ||
                            start >= 'A' && start <= 'Z') {
                        while (lyrics.charAt(lyricsIndex) != ' ' &&
                                lyrics.charAt(lyricsIndex) != '\n' &&
                                lyrics.charAt(lyricsIndex) != '\r' &&
                                lyrics.charAt(lyricsIndex) != '^') {
                            if (english.trim().length() < ((Lyrics) event).getLyric().trim().length()) {
                                english += lyrics.charAt(lyricsIndex);
                                lyricsIndex++;
                            } else {
                                break;
                            }
                        }
                    }
                    if (english.length() != 0) {
                        ((Lyrics) event).setLyric(english);
                        continue;
                    }

                    ((Lyrics) event).setLyric(String.valueOf(lyrics.charAt(lyricsIndex)));


                    lyricsIndex++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        StaffSymbol staffSymbol = new StaffSymbol(getContext(), width, height / 2, renderTrack, nowMeasures[0]);
        staffSymbol.draw(canvas);

        canvas.translate(0, height / 2);
        StaffSymbol staffSymbol1 = new StaffSymbol(getContext(), width, height / 2, renderTrack, nowMeasures[1]);
        staffSymbol1.draw(canvas);
        canvas.translate(0, -(height / 2));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LINE_SPACE_HEIGHT = getMeasuredHeight() / 40;
        LINE_STROKE = getMeasuredHeight() / 300;
        FIRST_LINE_HEIGHT = LINE_SPACE_HEIGHT * 3;
        STEM_HEIGHT = LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT / 2;

        createMeasures(renderTrack);
        settingMeasures();

        try {
            FileInputStream fis = new FileInputStream(uri.getPath());
            FileDescriptor fd = fis.getFD();
            player.reset();
            player.setDataSource(fd);
            player.prepare();
            player.start();
            activity.startRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }

        nowMeasure = measures.get(0);
        nowMeasures[0] = measures.subList(0, 4);
        nowMeasures[1] = measures.subList(4, 8);
        callOnDraw();
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("kkk", "stop!!!!!!!");
        player.stop();
        activity.stopRecord();
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }

    private void createMeasures(MidiTrack track) {
        measures = new ArrayList<>();

        Iterator<MidiEvent> it = track.getEvents().iterator();
        int nowTicks = 0;
        measureLength = 0;
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
                Logger.i("measure length : " + measureLength);
                measure.startTicks = nowTicks;
                measure.endTicks = nowTicks + measureLength;
                measure.addSymbol(e);
            }
        }
        // 여기서 마디 넘어가는애들 다시 정리해줘야됨

        for (int i = 0; i < measures.size(); i++) {
            MeasureSymbol item = measures.get(i);
            Logger.i("" + i + "번째 마디 시작");
            Logger.i("start tick : " + item.startTicks);

            Iterator<MidiSymbol> iter = item.getAllMidiSymbols().iterator();
            while (iter.hasNext()) {
                MidiSymbol symbol = iter.next();

                Logger.i("---------------------------------");
                if (symbol instanceof NoteSymbol) {
                    Logger.i("note symbol start ");
                    Logger.i("prev is : " + ((NoteSymbol) symbol).prev);
                    Logger.i("next is : " + ((NoteSymbol) symbol).next);
                    Logger.i("tie is : " + ((NoteSymbol) symbol).isTie());
                } else {
                    Logger.i("rest symbol start ");
                }
                Logger.i("symbol start tick : " + symbol.getStartTicks());
                Logger.i("symbol end   tick : " + (symbol.getStartTicks() + symbol.getDuration()));
                Logger.i("symbol duration   : " + symbol.getDuration());
                Logger.i("---------------------------------");
            }

            Logger.i("end tick : " + item.endTicks);
            Logger.i("" + i + "번째 마디 끝");
            Logger.i("==================================");
        }

        Logger.i("created measure count : " + measures.size());
    }

    private void settingMeasures() {
        for (MeasureSymbol measure : measures) {
            measure.setWidth((getMeasuredWidth() - getPaddingRight() - getPaddingLeft()) / ScoreView.MEASURE_LIMIT);
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
            long currentMillis = player.getCurrentPosition();
            long currentMillis2 = player.getCurrentPosition();
            long tick = 0;
            long startTime = System.currentTimeMillis();
            while (true) {

                if (player.getCurrentPosition() - currentMillis >
                        ((60 / nowMeasure.BPM) * ((nowMeasure.endTicks - nowMeasure.startTicks) / resolution)) * 1000) {
                    currentMillis = player.getCurrentPosition();

                    if (measureCount >= measures.size()) {
                        return;
                    }
                    nowMeasure = measures.get(measureCount);

                    int measureIndex = (measureCount / MEASURE_LIMIT) % 2;
                    int nowMeasureIndex = measureCount / MEASURE_LIMIT * MEASURE_LIMIT;
                    try {
                        nowMeasures[measureIndex] = measures.subList(nowMeasureIndex, nowMeasureIndex + MEASURE_LIMIT);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        nowMeasures[measureIndex] = measures.subList(nowMeasureIndex, nowMeasures.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ArrayList<String> list = new ArrayList<>();
                    for (int i = 0; i < MEASURE_LIMIT; i++) {
                        list.add(nowMeasures[measureIndex].get(i).lyrics);
                    }

                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);

                    callOnDraw();

                    measureCount++;
                    tick = nowMeasure.endTicks;
                }

                int term = 5;
                try {
                    if (player.getCurrentPosition() - currentMillis2 > term) { // 0.005초마다 들어온당

                        float plusTick = ((nowMeasure.BPM / 60 * resolution) / 1000) * (player.getCurrentPosition() - currentMillis2);
                        /*if (plusTick > 12) {
                            plusTick /= 2;
                        }*/
                        tick += plusTick;
                        Log.e("fucking", player.getCurrentPosition() + " | " + currentMillis2 + " = " + (player.getCurrentPosition() - currentMillis2));
                        Logger.i("fucking", "plus tick : " + plusTick);
                        Logger.i("fucking", "tick : " + tick);

                        listener.notifyCurrentTick(tick, term, nowMeasure.endTicks - nowMeasure.startTicks);
                        currentMillis2 = player.getCurrentPosition();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public TestActivity getActivity() {
        return activity;
    }

    public void setActivity(TestActivity activity) {
        this.activity = activity;
    }
}
