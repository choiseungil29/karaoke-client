package com.clogic.karaokeviewer.View;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by clogic on 2015. 11. 28..
 */
public class SheetView extends SurfaceView implements SurfaceHolder.Callback {

    private Point screenSize;

    public SheetView(Context context) {
        super(context);

        Activity activity = (Activity) context;
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpec = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpec = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpec == 0 && heightSpec == 0) {
            setMeasuredDimension(screenSize.x, screenSize.y);
        }
        else if (widthSpec == 0) {
            setMeasuredDimension(screenSize.x, heightSpec);
        }
        else if (heightSpec == 0) {
            setMeasuredDimension(widthSpec, screenSize.y);
        }
        else {
            setMeasuredDimension(widthSpec, heightSpec);
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
}
