package com.karaokepang.Midi.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.karaokepang.Midi.MidiTrack;
import com.karaokepang.Midi.event.MidiEvent;
import com.karaokepang.Midi.event.NoteOn;
import com.karaokepang.R;
import com.karaokepang.View.ScoreView;

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

        if(totalPitch / totalCount > 48) {
            clef = trebleRes;
        } else {
            clef = bassRes;
        }
        // C4는 48.
        // 높은음자리표는. G4 52를 찾기위해 존재
        clef = trebleRes;

        this.setLeftMargin(10);
        width = clef.getWidth();
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();

        Rect src = new Rect(0, 0, getWidth(), clef.getHeight());
        Rect dst = new Rect(0, 0, getWidth(), ScoreView.LINE_SPACE_HEIGHT * 6);
        canvas.translate(0, ScoreView.FIRST_LINE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT);
        canvas.drawBitmap(clef, src, dst, paint);
        canvas.translate(0, -(ScoreView.FIRST_LINE_HEIGHT - ScoreView.LINE_SPACE_HEIGHT));
    }
}
