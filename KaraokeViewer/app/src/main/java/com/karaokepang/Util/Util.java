package com.karaokepang.Util;

import android.content.Context;
import android.util.TypedValue;

import com.karaokepang.Midi.event.meta.Lyrics;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class Util {

    public static float dpToPixel(Context context, int dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    /**
     *
     * @param event 가사 이벤트
     * @return 문제되는 텍스트가 있으면 false
     */
    public static boolean filterLyricText(Lyrics event) {
        if (event.getLyric().equals("\r")) {
            return false;
        }
        if (event.getLyric().equals("\n")) {
            return false;
        }
        if (event.getLyric().equals("")) {
            return false;
        }
        return true;
    }
}
