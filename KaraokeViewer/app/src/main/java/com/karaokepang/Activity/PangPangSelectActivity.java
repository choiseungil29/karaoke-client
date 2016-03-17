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

    @ViewById(R.id.vv_background)
    MyVideoView videoView;

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setPangPangSelectActivity(this);

        setRandomVideoSource();
        setVideoView();
        setCameraPreView();
    }


    private void setRandomVideoSource() {
        String[] fileList = getFileList(FilePath.FILE_PATH_VPANGBG);
        String randomVideoFileName = fileList[new Random().nextInt(fileList.length)];
        videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG + randomVideoFileName);
    }

    private String[] getFileList(String strPath) {
        File fileRoot = new File(strPath);
        if (!fileRoot.isDirectory())
            return null;
        return fileRoot.list();
    }

    public void setVideoView() {
        videoView.setClickable(false);
        videoView.setFocusable(false);
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
