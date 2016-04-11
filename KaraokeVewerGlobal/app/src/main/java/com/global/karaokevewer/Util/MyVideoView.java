package com.global.karaokevewer.Util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.VideoView;

/**
 * Created by 1002230 on 16. 2. 15..
 */
public class MyVideoView extends VideoView {
    public MyVideoView(Context context) {
        super(context);
    }

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heigthMeasureSpec) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        if (displayMetrics != null) {
            setMeasuredDimension(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }

}
