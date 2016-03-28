package com.karaokepang.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.NoteOn;
import com.karaokepang.Midi.event.meta.TimeSignature;
import com.karaokepang.Midi.renderer.KeySignatureSymbol;
import com.karaokepang.Midi.renderer.MeasureSymbol;
import com.karaokepang.Midi.renderer.StaffSymbol;
import com.karaokepang.Midi.renderer.Symbol;
import com.karaokepang.Midi.renderer.TimeSignatureSymbol;
import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Midi.util.MidiUtil;
import com.karaokepang.Util.Resources;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EView;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by clogic on 16. 3. 18..
 */
@EView
public class ScoreView extends SurfaceView implements SurfaceHolder.Callback {

    private List<MidiTrack> renderTracks = new ArrayList<>();
    private MidiTrack renderTrack;
    private MidiTrack signTrack;

    private float tick = 0.0f;

    private List<MeasureSymbol> allMeasures = new ArrayList<>();
    private List<MeasureSymbol> renderMeasures[] = new List[2];
    private MeasureSymbol nowMeasure = null;

    private StaffSymbol[] staffs = new StaffSymbol[2];

    private int measureLength;
    private int measureCount = 0;

    public ScoreView(Context context) {
        this(context, null);
    }

    public ScoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @AfterViews
    public void afterViews() {
        Resources.initResources(getContext());
        MidiInfo.LINE_SPACE_HEIGHT = getMeasuredHeight() / 50;
        MidiInfo.LINE_STROKE = getMeasuredHeight() / 300;
        MidiInfo.FIRST_LINE_HEIGHT = MidiInfo.LINE_SPACE_HEIGHT * 9;
        MidiInfo.STEM_HEIGHT = MidiInfo.LINE_SPACE_HEIGHT * 3 + MidiInfo.LINE_SPACE_HEIGHT / 2;
    }

    public void initMidiFile(Uri midiUri) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(midiUri.getPath());
            MidiFile midi = new MidiFile(fis);
            initRenderTracks(midi);
            initRenderTrack();
            createAllMeasures();
            settingMeasures();

            nowMeasure = allMeasures.get(0);
            renderMeasures[0] = allMeasures.subList(0, 4);
            renderMeasures[1] = allMeasures.subList(4, 8);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void settingMeasures() {
        for (MeasureSymbol measure : allMeasures) {
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        canvas.drawRect(0, 0, width, height, paint);

        staffs[0] = new StaffSymbol(getContext(), width, height/2, renderTrack, renderMeasures[0]);
        staffs[0].nowTick = tick;
        staffs[0].draw(canvas);

        canvas.translate(0, height / 2);

        staffs[1] = new StaffSymbol(getContext(), width, height/2, renderTrack, renderMeasures[1]);
        staffs[1].nowTick = tick;
        staffs[1].draw(canvas);

        canvas.translate(0, -(height / 2));
    }

    public void update(float tick) {
        this.tick = tick;

        if (nowMeasure.startTicks <= tick &&
                nowMeasure.endTicks > tick) {
            // 윗줄인지 아랫줄인지
            int measureIndex = (measureCount / MidiInfo.MEASURE_LIMIT) % 2;
            int nowMeasureIndex = measureCount / MidiInfo.MEASURE_LIMIT * MidiInfo.MEASURE_LIMIT;
            try {
                renderMeasures[measureIndex] = allMeasures.subList(nowMeasureIndex, nowMeasureIndex + MidiInfo.MEASURE_LIMIT);
                renderMeasures[(measureIndex + 1) % 2] = allMeasures.subList(nowMeasureIndex + 4, nowMeasureIndex + 4 + MidiInfo.MEASURE_LIMIT);
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                renderMeasures[(measureIndex + 1) % 2] = allMeasures.subList(nowMeasureIndex + 4, allMeasures.size());
                //finishStaffUpdate = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (nowMeasure.endTicks <= tick) {
            measureCount++;
            if (measureCount >= allMeasures.size()) {
                return;
            }
            nowMeasure = allMeasures.get(measureCount);
        }
    }

    public void draw() {
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        onDraw(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    private void initRenderTracks(MidiFile midi) {
        signTrack = midi.getTracks().get(0);
        for (int i = 0; i < midi.getTracks().size(); i++) {
            Iterator<MidiEvent> it = midi.getTracks().get(i).getEvents().iterator();
            while (it.hasNext()) {
                MidiEvent event = it.next();

                if (event.toString().contains("TrackName") &&
                        event.toString().toLowerCase().contains("melody")) {
                    renderTracks.add(midi.getTracks().get(i));
                }
            }
        }
    }

    private void initRenderTrack() {
        // 이벤트 다 렌더 트랙으로 옮긴다
        renderTrack = new MidiTrack();
        for (MidiTrack track : renderTracks) {
            Iterator<MidiEvent> it = track.getEvents().iterator();
            while (it.hasNext()) {
                MidiEvent event = it.next();
                renderTrack.insertEvent(event);
            }
        }

        // signtrack은 signtrack으로써 존재.
        Iterator<MidiEvent> it = signTrack.getEvents().iterator();
        while (it.hasNext()) {
            MidiEvent event = it.next();
            renderTrack.insertEvent(event);
        }

        for (MidiEvent e : renderTrack.getEvents()) {
            if (e instanceof NoteOn &&
                    ((NoteOn) e).getVelocity() > 0) {
                if (((NoteOn) e).getNoteValue() < BeforeScoreView.LOWER_NOTE_VALUE) {
                    BeforeScoreView.LOWER_NOTE_VALUE = ((NoteOn) e).getNoteValue();
                }
            }
        }
    }

    private void createAllMeasures() {
        allMeasures = new ArrayList<>();

        /*long tickCount = 0;
        List<TimeSignature> tsList = signTrack.getEvents(TimeSignature.class);
        Iterator<MidiEvent> renderIter = renderTrack.getEvents().iterator();
        int tsIndex = 0;
        TimeSignature timeSignature = tsList.get(tsIndex);

        MeasureSymbol measure = new MeasureSymbol();
        int lastTick = 0;
        while (renderIter.hasNext()) {
            MidiEvent e = renderIter.next();
            if(e.getTick() < timeSignature.getTick()) {
                tsIndex++;
                timeSignature = tsList.get(tsIndex);
            }

            measure.startTicks = lastTick;
            measure.endTicks = measure.startTicks + (BeforeScoreView.resolution / timeSignature.getRealDenominator() / 4) * timeSignature.getNumerator();
            measure.addSymbol(e);
        }*/

        allMeasures = new ArrayList<>();

        Iterator<MidiEvent> it = renderTrack.getEvents().iterator();
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
                allMeasures.add(measure);
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
    }
}
