package com.karaokepang.Activity;

import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    @ViewById(R.id.layout_preview)
    LinearLayout layoutPreiew;

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setDuetSelectActivity(this);

        setVideoView();
        setImageView();
//        setDuetCameraPreView();
    }

    @Override
    public void startSong(String songNumber) {
        super.startSong(songNumber);
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    layoutPreiew.setVisibility(LinearLayout.VISIBLE);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void setImageView() {
        imgBackground.setBackgroundResource(R.drawable.background_musicsheet);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("kkk", "===============onResume============" + getLocalClassName());
        cameraResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("kkk", "===============onPause============" + getLocalClassName());
        textSongSelected.setVisibility(TextView.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("kkk", "===============onDestroy============" + getLocalClassName());
        activityController.setDuetSelectActivity(null);
    }
}
