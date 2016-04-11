package com.global.karaokevewer;

import android.app.Application;

import org.androidannotations.annotations.EApplication;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by clogic on 16. 3. 17..
 */
@EApplication
public class App extends Application {
    private static App mApplication;

    // Object들을 저장해두는 Map
    private HashMap<String, Object> mAppStorage = new HashMap<>();

    // List<Object>들을 저장해두는 Map
    private HashMap<String, List<? extends Object>> mAppListStorage = new LinkedHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }

    public static App getInstance() {
        return mApplication;
    }

    public Object getValue(String key) {
        return mAppStorage.get(key);
    }

    public Object getValue(String key, boolean deleteFlag) {
        Object value = mAppStorage.get(key);
        if (deleteFlag) {
            if (value != null)
                mAppStorage.remove(key);
        }
        return value;
    }

    public List<? extends Object> getObjectList(String key, boolean delFlag) {
        List<? extends Object> value = mAppListStorage.get(key);
        if (delFlag) {
            if (value != null)
                mAppListStorage.remove(key);
        }
        return value;
    }

    public List<? extends Object> getObjectList(String key) {
        return mAppListStorage.get(key);
    }

    public void setValue(String key, List<? extends Object> value) {
        mAppListStorage.put(key, value);
    }

    public void setValue(String key, Object value) {
        mAppStorage.put(key, value);
    }
}
