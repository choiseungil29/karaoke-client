package com.karaokepang.Activity;

import android.media.MediaPlayer;
import android.widget.TextView;

import com.karaokepang.R;
import com.karaokepang.Util.FilePath;
import com.karaokepang.Util.MyVideoView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.Random;

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
        setCameraPreView();
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
