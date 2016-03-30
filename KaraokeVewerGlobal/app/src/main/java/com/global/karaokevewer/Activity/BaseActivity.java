package com.global.karaokevewer.Activity;

import android.app.Activity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity
public class BaseActivity extends Activity {

    @AfterViews
    public void afterViews() {

    }
}
