package com.karaokepang.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.WindowFeature;

/**
 * Created by clogic on 16. 3. 17..
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity
public class DuetActivity extends PlayActivity {

    private ActivityController activityController = ActivityController.getInstance();
    private Uri midiUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityController.setDuetActivity(this);
    }

    @Override
    public void afterViews() {
        super.afterViews();

        midiUri = getIntent().getData();
        initWithStartMidiFile(midiUri);
    }
}
