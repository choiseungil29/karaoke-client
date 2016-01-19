package com.duetwstar.Util;

import android.util.Log;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class Logger {

    public static final String TAG = Logger.class.getSimpleName();

    public static void i(String message) {
        Logger.i(TAG, message);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }
}
