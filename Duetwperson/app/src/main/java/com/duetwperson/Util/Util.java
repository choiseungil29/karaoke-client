package com.duetwperson.Util;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class Util {

    public static float dpToPixel(Context context, int dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
