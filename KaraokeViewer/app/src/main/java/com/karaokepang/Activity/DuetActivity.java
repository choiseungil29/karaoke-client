package com.karaokepang.Activity;

import android.net.Uri;

import com.karaokepang.R;
import com.karaokepang.View.ScoreView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import android.os.Bundle;
import android.view.Window;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.WindowFeature;

/**
 * Created by clogic on 16. 3. 17..
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity(R.layout.activity_duet)
public class DuetActivity extends PlayActivity {

    private ActivityController activityController = ActivityController.getInstance();
    private Uri midiUri;
    @ViewById(R.id.sv_score) ScoreView sv_score;

    @Override
    public void afterViews() {
        super.afterViews();

        midiUri = getIntent().getData();
        activityController.setDuetActivity(this);
        //initWithStartMidiFile(midiUri);
        initMidiFile(midiUri);
    }

    public void initWithStartMidiFile(Uri uri) {
        initMidiFile(uri);
        String songNumber = uri.getLastPathSegment().substring(0, uri.getLastPathSegment().length() - 4);
        play(songNumber);
    }

    @Override
    public void initMidiFile(Uri uri) {
        super.initMidiFile(uri);
    }
}
