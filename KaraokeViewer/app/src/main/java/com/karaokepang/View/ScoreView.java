package com.karaokepang.View;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.util.MidiInfo;
import com.karaokepang.Util.Resources;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EView;

/**
 * Created by clogic on 16. 3. 18..
 */
@EView
public class ScoreView extends SurfaceView implements SurfaceHolder.Callback {

    private MidiTrack renderTrack;

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

    public void initMidiFile(MidiFile midi) {

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
    public void onDraw(Canvas canvas) {

    }

    public void update(float tick) {

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
}
