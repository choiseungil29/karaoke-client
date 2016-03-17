package com.karaokepang.Activity;

import android.net.Uri;
import android.view.Window;

import com.karaokepang.R;

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
    public void afterViews() {
        super.afterViews();
        activityController.setDuetActivity(this);
        midiUri = getIntent().getData();
        initWithStartMidiFile(midiUri);
    }
}
