package com.karaokepang.Activity;

import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity
public class BaseActivity extends AppCompatActivity {

    @AfterViews
    public void afterViews() {

    }
}
