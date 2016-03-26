package com.karaokepang.Activity;

import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;

import com.karaokepang.R;

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
        activityController.setPangPangActivity(this);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setLayout(android.view.WindowManager.LayoutParams.MATCH_PARENT, android.view.WindowManager.LayoutParams.MATCH_PARENT);
        midiUri = getIntent().getData();
        initMidiFileWithStart(midiUri);
    }

    @Override
    protected void update(float tick) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityController.setPangPangActivity(this);
    }
}
