package com.karaokepang.Activity;

import android.net.Uri;
import android.view.Window;

import com.karaokepang.R;
import com.karaokepang.View.ScoreView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.WindowFeature;

/**
 * Created by clogic on 16. 3. 17..
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity(R.layout.activity_duet)
public class DuetActivity extends PlayActivity {

    private ActivityController activityController = ActivityController.getInstance();
    private Uri midiUri;

    @ViewById(R.id.sv_score)
    ScoreView sv_score;

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setDuetActivity(this);
        midiUri = getIntent().getData();
        initMidiFileWithStart(midiUri);
    }

    @Override
    protected void update(float tick) {
        super.update(tick);
        sv_score.update(tick);
    }

    @Override
    protected void draw(float tick) {
        super.draw(tick);
        sv_score.draw();
    }
}
