package com.global.karaokevewer.Activity;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.global.karaokevewer.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity(R.layout.activity_select_duet)
public class DuetSelectActivity extends SelectActivity {

    @ViewById(R.id.iv_background)
    ImageView imgBackground;
    @ViewById(R.id.layout_preview)
    LinearLayout layoutPreiew;

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setDuetSelectActivity(this);

        setVideoView();
        setImageView();
        setDuetCameraPreView();
    }

    private void setImageView() {
        imgBackground.setBackgroundResource(R.drawable.background_musicsheet);
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
        activityController.setDuetSelectActivity(null);
    }
}
