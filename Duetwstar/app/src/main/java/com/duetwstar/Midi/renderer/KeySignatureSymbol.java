package com.duetwstar.Midi.renderer;

import android.graphics.Canvas;

import com.duetwstar.Midi.event.meta.KeySignature;
import com.duetwstar.Midi.renderer.accidental.FlatSymbol;
import com.duetwstar.Midi.renderer.accidental.SharpSymbol;
import com.duetwstar.View.ScoreView;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class KeySignatureSymbol extends Symbol {

    private KeySignature key;

    public KeySignatureSymbol(KeySignature key) {
        this.key = key;
    }

    public KeySignatureSymbol(KeySignature key, int x) {
        this.key = key;

        this.width = 0;
        if(key.getKey() > 0) {
            this.width = new SharpSymbol().width * key.getKey();
        }
        if(key.getKey() < 0) {
            this.width = new FlatSymbol().width * -key.getKey();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if(key.getKey() == 0) {
            return;
        }

        if(key.getKey() > 0) {
            drawSharp(canvas);
        } else {
            drawFlat(canvas);
        }
    }

    private void drawFlat(Canvas canvas) {
        int width = 0;
        int x = 0;
        for(int i=0; i<-key.getKey(); i++) {
            FlatSymbol flat = new FlatSymbol();
            flat.setX(x);

            int quotient = (i+1)/2;
            int remainder = (i+1)%2;
            int height = ScoreView.LINE_SPACE_HEIGHT;
            int middleHeight = height/2;
            flat.setY(ScoreView.FIRST_LINE_HEIGHT + middleHeight * quotient + (remainder * height * 2));
            flat.draw(canvas);

            x += flat.getWidth();
            width += flat.getWidth();
        }
        this.width = width;
    }

    private void drawSharp(Canvas canvas) {
        int y = ScoreView.FIRST_LINE_HEIGHT;
        int width = 0;
        int x = 0;
        for(int i=0; i<7; i++) {
            SharpSymbol sharp = new SharpSymbol();
            sharp.setX(x);

            if(i%2 == 0) {
                int idx = (i+1)/2;
                int height = ScoreView.LINE_SPACE_HEIGHT;
                int middleHeight = height/2;
                if(i > 3) {
                    sharp.setY(y + ScoreView.LINE_SPACE_HEIGHT * 3 + ScoreView.LINE_SPACE_HEIGHT/2 - idx * middleHeight);
                } else {
                    sharp.setY(y - idx * middleHeight);
                }
            } else {
                int idx = (i+1)/2;
                int height = ScoreView.LINE_SPACE_HEIGHT;
                int middleHeight = height/2;
                sharp.setY(y - idx * middleHeight + height * 2);
            }
            sharp.draw(canvas);
            width += sharp.getWidth();
        }
        this.width = width;
    }
}
