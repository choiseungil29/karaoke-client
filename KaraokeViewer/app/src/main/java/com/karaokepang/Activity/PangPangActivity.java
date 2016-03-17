package com.karaokepang.Activity;

import android.net.Uri;
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
    public void afterViews() {
        super.afterViews();
        activityController.setPangPangActivity(this);

        midiUri = getIntent().getData();
        initWithStartMidiFile(midiUri);
    }


    public void initWithStartMidiFile(Uri uri) {
        super.initMidiFile(uri);
        String songNumber = uri.getLastPathSegment().substring(0, uri.getLastPathSegment().length() - 4);
        play(songNumber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        activityController.setPangPangActivity(null);
    }
}
