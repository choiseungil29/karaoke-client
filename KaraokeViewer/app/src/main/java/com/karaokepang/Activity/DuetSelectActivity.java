package com.karaokepang.Activity;

import android.widget.ImageView;
import android.widget.TextView;

import com.karaokepang.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity(R.layout.activity_select_duet)
public class DuetSelectActivity extends SelectActivity {

    @ViewById(R.id.iv_background)
    ImageView imgBackground;

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setDuetSelectActivity(this);

        setImageView();
        setCameraPreView();
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