package com.duetwstar.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.duetwstar.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by clogic on 2015. 12. 12..
 */
public class Resources {

    public static final Map<Integer, Bitmap> timeSignatureTable = new HashMap<>();

    public static void initResources(Context context) {
        android.content.res.Resources res = context.getResources();
        timeSignatureTable.put(2, BitmapFactory.decodeResource(res, R.mipmap.two));
        timeSignatureTable.put(3, BitmapFactory.decodeResource(res, R.mipmap.three));
        timeSignatureTable.put(4, BitmapFactory.decodeResource(res, R.mipmap.four));
        timeSignatureTable.put(6, BitmapFactory.decodeResource(res, R.mipmap.six));
        timeSignatureTable.put(8, BitmapFactory.decodeResource(res, R.mipmap.eight));
        timeSignatureTable.put(9, BitmapFactory.decodeResource(res, R.mipmap.nine));
        timeSignatureTable.put(12, BitmapFactory.decodeResource(res, R.mipmap.twelve));
    }
}
