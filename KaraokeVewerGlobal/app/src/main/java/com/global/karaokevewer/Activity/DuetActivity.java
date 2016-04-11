package com.global.karaokevewer.Activity;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.global.karaokevewer.R;
import com.global.karaokevewer.View.ScoreView;

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
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        midiUri = getIntent().getData();
        sv_score.initMidiFile(midiUri);
        initMidiFileWithStart(midiUri);
        ltv_lyrics.setBackgroundColor(Color.parseColor("#FF444444"));
    }

    @Override
    protected void update(float tick) {
        sv_score.update(tick);
    }

    @Override
    protected void draw(float tick) {
        super.draw(tick);
        sv_score.draw();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("kkk", "----------------------onResume------------------");
        activityController.setDuetActivity(this);
    }

        @Override
    protected void onPause() {
        super.onPause();
        Log.d("kkk", "----------------------onPause------------------");
        activityController.setDuetActivity(null);
    }
}
