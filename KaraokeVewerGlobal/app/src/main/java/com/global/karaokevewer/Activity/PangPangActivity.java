package com.global.karaokevewer.Activity;

import android.graphics.Color;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;

import com.global.karaokevewer.R;

import org.androidannotations.annotations.EActivity;
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
    public void afterViews() {
        super.afterViews();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        midiUri = getIntent().getData();
        initMidiFileWithStart(midiUri);
        ltv_lyrics.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void update(float tick) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        activityController.setPangPangActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityController.setPangPangActivity(null);
    }

}
