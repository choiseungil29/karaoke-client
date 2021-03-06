package com.global.karaokevewer.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.global.karaokevewer.Activity.MusicPlayActivity;
import com.global.karaokevewer.Midi.MidiFile;
import com.global.karaokevewer.Midi.MidiTrack;
import com.global.karaokevewer.Midi.event.MidiEvent;
import com.global.karaokevewer.Midi.event.NoteOn;
import com.global.karaokevewer.Midi.event.meta.MidiLyrics;
import com.global.karaokevewer.Midi.event.meta.TimeSignature;
import com.global.karaokevewer.Midi.renderer.KeySignatureSymbol;
import com.global.karaokevewer.Midi.renderer.MeasureSymbol;
import com.global.karaokevewer.Midi.renderer.StaffSymbol;
import com.global.karaokevewer.Midi.renderer.Symbol;
import com.global.karaokevewer.Midi.renderer.TimeSignatureSymbol;
import com.global.karaokevewer.Midi.renderer.midi.MidiSymbol;
import com.global.karaokevewer.Midi.renderer.midi.NoteSymbol;
import com.global.karaokevewer.Util.FilePath;
import com.global.karaokevewer.Util.Logger;
import com.global.karaokevewer.Util.Resources;

import org.androidannotations.annotations.EView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by clogic on 2015. 12. 10..
 */
@EView
public class BeforeScoreView extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = BeforeScoreView.class.getSimpleName();

    private MusicPlayActivity activity;

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
    public static int LOWER_NOTE_VALUE = 128;
    public static final int OCTAVE = 12;
    public static final int MEASURE_LIMIT = 4;

    public static int resolution = 0; // 한 박자의 단위길이

    private MusicListener listener;

    private ScoreThread thread = new ScoreThread();
    public MediaPlayer player = new MediaPlayer();

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

    private float nowTick;

    private Uri uri = null;
    private List<Float> alphaSeconds = new ArrayList<>();
    private Map<Float, Float> millisToBpm = new HashMap<>();

    private Handler musicStartHandler;
    private Runnable musicRunnable;

    public BeforeScoreView(Context context) {
        this(context, null);
    }

    public BeforeScoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeforeScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        musicStartHandler = new Handler();
        activity = (MusicPlayActivity) context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        Resources.initResources(context);
        renderTracks = new ArrayList<>();
        measures = new ArrayList<>();
        musicRunnable = new Runnable() {
            @Override
            public void run() {
                startMusicPlay();
                activity.showScoreView();
                activity.layoutLyric.setVisibility(VISIBLE);
            }
        };

        nowTick = 0;
        alphaSeconds = new ArrayList<>();
        millisToBpm = new HashMap<>();

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
                    lyricsTrack = midi.getTracks().get(i);
                }
            }
        }
        for (MidiEvent event : lyricsTrack.getEvents()) {
            //Logger.i("lyriccccccccccccccccccccccccc : " + event.toString());
        }

        try {
            File[] fileList = new File(FilePath.FILE_PATH_VPANGMID).listFiles();
            if (fileList == null)
                return;
            for (File file : fileList) {
                if ((file.getName().endsWith(".ksa") || file.getName().endsWith(".KSA")) && file.getName().startsWith(fileName)) {
                    InputStream is = new FileInputStream(file);
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
                if (event instanceof MidiLyrics) {

                    if (((MidiLyrics) event).getLyric().equals("\r")) {
                        continue;
                    }
                    if (((MidiLyrics) event).getLyric().equals("\n")) {
                        continue;
                    }
                    if (((MidiLyrics) event).getLyric().equals("")) {
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
                            if (english.trim().length() < ((MidiLyrics) event).getLyric().trim().length()) {
                                english += lyrics.charAt(lyricsIndex);
                                lyricsIndex++;
                            } else {
                                break;
                            }
                        }
                    }
                    if (english.length() != 0) {
                        ((MidiLyrics) event).setLyric(english);
                        continue;
                    }

                    ((MidiLyrics) event).setLyric(String.valueOf(lyrics.charAt(lyricsIndex)));


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

    public void release() {
        if (player != null) {
            player.release();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        canvas.drawRect(0, 0, width, height, paint);

        StaffSymbol staffSymbol = new StaffSymbol(getContext(), width, height / 2, renderTrack, nowMeasures[0]);
        staffSymbol.nowTick = nowTick;
        staffSymbol.draw(canvas);

        canvas.translate(0, height / 2);
        StaffSymbol staffSymbol1 = new StaffSymbol(getContext(), width, height / 2, renderTrack, nowMeasures[1]);
        staffSymbol1.nowTick = nowTick;
        staffSymbol1.draw(canvas);
        canvas.translate(0, -(height / 2));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        musicStartHandler.postDelayed(musicRunnable, 3000);
    }

    private void startMusicPlay() {
        LINE_SPACE_HEIGHT = getMeasuredHeight() / 50;
        LINE_STROKE = getMeasuredHeight() / 300;
        FIRST_LINE_HEIGHT = LINE_SPACE_HEIGHT * 9;
        STEM_HEIGHT = LINE_SPACE_HEIGHT * 3 + LINE_SPACE_HEIGHT / 2;

        for (MidiEvent e : renderTrack.getEvents()) {
            if (e instanceof NoteOn &&
                    ((NoteOn) e).getVelocity() > 0) {
                if (((NoteOn) e).getNoteValue() < BeforeScoreView.LOWER_NOTE_VALUE) {
                    BeforeScoreView.LOWER_NOTE_VALUE = ((NoteOn) e).getNoteValue();
                }
            }
        }

        BeforeScoreView.DEFAULT_C = (BeforeScoreView.LOWER_NOTE_VALUE + 12) / 12 * 12;
        Logger.i("DEFAULT C : " + BeforeScoreView.DEFAULT_C);

        createMeasures(renderTrack);
        settingMeasures();

        nowMeasure = measures.get(0);
        nowMeasures[0] = measures.subList(0, 4);
        nowMeasures[1] = measures.subList(4, 8);
        callOnDraw();
        thread.start();

        try {
            FileInputStream fis = new FileInputStream(uri.getPath());
            FileDescriptor fd = fis.getFD();
//            release();
            player.reset();
            player.setDataSource(fd);
            player.prepare();
            player.start();
            activity.startRecord();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        stopMusicHandler();
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
        TimeSignature signature = null;
        while (it.hasNext()) {
            MidiEvent e = it.next();

            while (measure.endTicks != 0 &&
                    measure.endTicks <= e.getTick()) {
                nowTicks = measure.endTicks;
                measure.created();
                measures.add(measure);
                measure = new MeasureSymbol();
                if (signature != null) {
                    measure.numerator = signature.getNumerator();
                    measure.denominator = signature.getRealDenominator();
                }
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
                signature = (TimeSignature) e;
                measure.numerator = signature.getNumerator();
                measure.denominator = signature.getRealDenominator();
                measureLength = (BeforeScoreView.resolution / (((TimeSignature) e).getRealDenominator() / 4)) * ((TimeSignature) e).getNumerator();
                measure.startTicks = nowTicks;
                measure.endTicks = nowTicks + measureLength;
                measure.addSymbol(e);
            }
        }
        // 여기서 마디 넘어가는애들 다시 정리해줘야됨
        //ScoreView.DEFAULT_C = (MeasureSymbol.totalNoteValue/MeasureSymbol.noteCount) / 12 * 12;

        for (int i = 0; i < measures.size(); i++) {
            MeasureSymbol item = measures.get(i);
            Logger.i(TAG, "" + i + "번째 마디 시작");
            Logger.i(TAG, "start tick : " + item.startTicks);

            Iterator<MidiSymbol> iter = item.getAllMidiSymbols().iterator();
            while (iter.hasNext()) {
                MidiSymbol symbol = iter.next();

                Logger.i("---------------------------------");
                if (symbol instanceof NoteSymbol) {
                    Logger.i(TAG, "note symbol start ");
                    Logger.i(TAG, "note value : " + ((NoteSymbol) symbol).getNoteValue());
                    Logger.i(TAG, "tie is : " + ((NoteSymbol) symbol).isTie());
                } else {
                    Logger.i(TAG, "rest symbol start ");
                }
                Logger.i(TAG, "symbol start tick : " + symbol.getStartTicks());
                Logger.i(TAG, "symbol end   tick : " + (symbol.getStartTicks() + symbol.getDuration()));
                Logger.i(TAG, "symbol duration   : " + symbol.getDuration());
                Logger.i(TAG, "---------------------------------");
            }

            Logger.i(TAG, "end tick : " + item.endTicks);
            Logger.i(TAG, "" + i + "번째 마디 끝");
            Logger.i(TAG, "==================================");
        }

        Logger.i(TAG, "created measure count : " + measures.size());
    }

    private void settingMeasures() {
        for (MeasureSymbol measure : measures) {
            measure.setWidth((getMeasuredWidth() - getPaddingRight() - getPaddingLeft()) / BeforeScoreView.MEASURE_LIMIT);
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
            long currentMillis2 = player.getCurrentPosition();
            float tick;

            boolean finishStaffUpdate = false;
            /*Tempo tempo = nowMeasure.tempoList.get(0);
            while (true) {
                float millis = 0;
                try {
                    for (Tempo t : nowMeasure.tempoList) {
                        if (tempo.getTick() >= t.getTick()) {
                            continue;
                        }

                        // 현재 시간을 틱으로 바꿔서 Tempo와 매칭시키는 부분
                        // tempo가 1번만 바뀌면 문제가 없지만, 2번이상 바뀔경우 문제가 발생한다. 템포가 중간에 안맞는 경우가 생기면 여기 코드에서 더 발전해나가야함
                        if (t.getTick() <
                                (tempo.getBpm() / 60 * resolution * ((float) player.getCurrentPosition() / 1000))) {
                            Logger.i("player current millis : " + player.getCurrentPosition());
                            Logger.i("result before tempo : " + tempo.getBpm());
                            Logger.i("result after tempo : " + t.getBpm());
                            Logger.i("result tick : " + (tempo.getBpm() / 60 * resolution * ((float) player.getCurrentPosition() / 1000)));
                            millis += (t.getTick() / (tempo.getBpm() / 60 * resolution));
                            alphaSeconds.add(millis);
                            millisToBpm.put(millis, tempo.getBpm());
                            tempo = t;
                        }
                    }

                    float stack = 0.0f;
                    tick = 0.0f;
                    for (float item : alphaSeconds) {
                        float section = item - stack;
                        float bpm = millisToBpm.get(item);

                        tick += bpm / 60 * resolution * section;
                        stack += section;
                    }
                    tick += tempo.getBpm() / 60 * resolution * (((float) player.getCurrentPosition() / 1000) - stack);
                    // 위 코드는 현재까지의 진행 시간을 tick으로 나타내주는 코드이다.

                    // 플레이어 진행된 시간 vs 현재 마디의 tick
                    //
                    if (!finishStaffUpdate) {
                        if (nowMeasure.startTicks <= tick &&
                                nowMeasure.endTicks > tick) {
                            // 윗줄인지 아랫줄인지
                            int measureIndex = (measureCount / MEASURE_LIMIT) % 2;
                            int nowMeasureIndex = measureCount / MEASURE_LIMIT * MEASURE_LIMIT;
                            try {
                                nowMeasures[measureIndex] = measures.subList(nowMeasureIndex, nowMeasureIndex + MEASURE_LIMIT);
                                nowMeasures[(measureIndex + 1) % 2] = measures.subList(nowMeasureIndex + 4, nowMeasureIndex + 4 + MEASURE_LIMIT);
                            } catch (IndexOutOfBoundsException e) {
                                e.printStackTrace();
                                nowMeasures[(measureIndex + 1) % 2] = measures.subList(nowMeasureIndex + 4, measures.size());
                                finishStaffUpdate = true;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (nowMeasure.endTicks <= tick) {
                            measureCount++;
                            if (measureCount >= measures.size()) {
                                return;
                            }
                            nowMeasure = measures.get(measureCount);
                        }
                    }

                    int term = 32;
                    try {
                        // 현재 tick계산해서 가사 던져준다
                        if (player.getCurrentPosition() - currentMillis2 > term) {
                            tick = 0;
                            stack = 0;
                            for (float item : alphaSeconds) {
                                float section = item - stack;
                                float bpm = millisToBpm.get(item);

                                tick += bpm / 60 * resolution * section;
                                stack += section;
                            }

                            tick += tempo.getBpm() / 60 * resolution * (((float) player.getCurrentPosition() / 1000) - stack);
                            nowTick = tick;
                            Logger.i("test tick : " + tick);
                            listener.notifyCurrentTick(tick, term, nowMeasure.endTicks - nowMeasure.startTicks);
                            currentMillis2 = player.getCurrentPosition();
                            callOnDraw();
                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        break;
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }*/
        }
    }

    public MusicPlayActivity getActivity() {
        return activity;
    }

    public void setActivity(MusicPlayActivity activity) {
        this.activity = activity;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public Handler getMusicStartHandler() {
        return musicStartHandler;
    }

    public void setMusicStartHandler(Handler musicStartHandler) {
        this.musicStartHandler = musicStartHandler;
    }

    public void stopMusicHandler() {
        player.stop();
        activity.stopRecord(true);
        musicStartHandler.removeCallbacks(musicRunnable);
    }

    public void reset() {
        nowTick = 0;
    }

    public interface MusicListener {
        public void notifyMeasureChanged(ArrayList<String> lyrics, long tick);
        public void notifyCurrentTick(float tick, int term, int measureLength);
    }
}
