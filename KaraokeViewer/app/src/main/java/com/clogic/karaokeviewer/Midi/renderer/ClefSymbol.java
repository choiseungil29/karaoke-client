package com.clogic.karaokeviewer.Midi.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.clogic.karaokeviewer.Midi.MidiTrack;
import com.clogic.karaokeviewer.Midi.event.MidiEvent;
import com.clogic.karaokeviewer.Midi.event.NoteOn;
import com.clogic.karaokeviewer.R;
import com.clogic.karaokeviewer.View.ScoreView;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class ClefSymbol extends Symbol {

    private Bitmap clef;

    private Bitmap trebleRes;
    private Bitmap bassRes;

    public ClefSymbol(Context context, MidiTrack track) {

        int totalCount = 0;
        int totalPitch = 0;
        for(MidiEvent event : track.getEvents()) {
            if(event instanceof NoteOn) {
                NoteOn on = (NoteOn) event;
                totalCount++;
                totalPitch += on.getNoteValue();
            }
        }

        trebleRes = BitmapFactory.decodeResource(context.getResources(), R.mipmap.treble);
        bassRes = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bass);

        if(totalPitch / totalCount > 60) {
            clef = trebleRes;
        } else {
            clef = bassRes;
        }

        this.setLeftMargin(10);
        width = clef.getWidth();
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();

        Rect src = new Rect(0, 0, getWidth(), clef.getHeight());
        Rect dst = new Rect(0, 0, getWidth(), ScoreView.LINE_SPACE_HEIGHT * 6);
        //canvas.translate(getLeftMargin(), ScoreView.FIRST_LINE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT);
        canvas.translate(0, ScoreView.FIRST_LINE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT);
        canvas.drawBitmap(clef, src, dst, paint);
        //canvas.translate(-getLeftMargin(), -(ScoreView.FIRST_LINE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT));
        canvas.translate(0, -(ScoreView.FIRST_LINE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT));
    }
}