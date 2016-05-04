package com.karaokepang.Activity;

import android.widget.TextView;

import com.karaokepang.R;

import org.androidannotations.annotations.EActivity;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity(R.layout.activity_select_pangpang)
public class PangPangSelectActivity extends SelectActivity {

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setPangPangSelectActivity(this);

        setRandomVideoSource();
        setVideoView();
//        setCameraPreView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        textSongSelected.setVisibility(TextView.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityController.setPangPangSelectActivity(null);
    }
}
