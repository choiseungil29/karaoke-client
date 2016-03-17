package com.karaokepang.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import com.karaokepang.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.WindowFeature;

/**
 * Created by clogic on 16. 3. 17..
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity(R.layout.activity_pangpang)
public class PangPangActivity extends PlayActivity {

    private ActivityController activityController = ActivityController.getInstance();
    private Uri midiUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityController.setPangPangActivity(this);
    }

    @Override
    public void afterViews() {
        super.afterViews();

        midiUri = getIntent().getData();
        initWithStartMidiFile(midiUri);
    }

}
