package com.karaokepang.Activity;

import android.media.MediaPlayer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.karaokepang.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity(R.layout.activity_select_duet)
public class DuetSelectActivity extends SelectActivity {

    @ViewById(R.id.vv_background_back)
    VideoView videoView;
    @ViewById(R.id.iv_background)
    ImageView imgBackground;

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setDuetSelectActivity(this);

        setVideoView();
        setImageView();
        setCameraPreView();
    }

    public void setVideoView() {
        videoView.setClickable(false);
        videoView.setFocusable(false);
        videoView.setLayoutParams(new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth() / 4, getWindowManager().getDefaultDisplay().getHeight() / 4));
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.resume();
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.setVolume(0, 0);
                videoView.start();
            }
        });
        videoView.start();
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
