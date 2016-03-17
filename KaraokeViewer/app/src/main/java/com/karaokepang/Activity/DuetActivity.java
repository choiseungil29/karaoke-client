package com.karaokepang.Activity;

import android.net.Uri;

import com.karaokepang.R;
import com.karaokepang.View.ScoreView;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity(R.layout.activity_duet)
public class DuetActivity extends PlayActivity {

    private ActivityController controller = ActivityController.getInstance();

    @ViewById(R.id.sv_score) ScoreView sv_score;

    @Override
    public void afterViews() {
        super.afterViews();
    }

    @Override
    public void initMidiFile(Uri uri) {
        super.initMidiFile(uri);
    }
}
