package com.karaokepang.Activity;

import android.hardware.Camera;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.karaokepang.R;
import com.karaokepang.Util.FilePath;
import com.karaokepang.Util.MyVideoView;
import com.karaokepang.camera.CameraPreview;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.Random;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity(R.layout.activity_select_pangpang)
public class PangPangSelectActivity extends SelectActivity {

    private ActivityController activityController = ActivityController.getInstance();
    private Camera camera;
    private CameraPreview preview;

    @ViewById(R.id.vv_background)
    MyVideoView videoView;
    @ViewById(R.id.camera_layout)
    RelativeLayout layoutCamera;

    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setPangPangSelectActivity(this);
        setRandomVideoSource();

        setVideoView();
        setCameraPreView();
    }

    private void setCameraPreView() {
        preview = new CameraPreview(getApplicationContext(), camera);
        preview.setLayoutParams(new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth() / 4, getWindowManager().getDefaultDisplay().getHeight() / 4));
        layoutCamera.addView(preview);
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private void setVideoView() {
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

    private void setRandomVideoSource() {
        String[] fileList = getFileList(FilePath.FILE_PATH_VPANGBG);
        String randomVideoFileName = fileList[new Random().nextInt(fileList.length)];
        Log.d("kkk", "선택된파일 = " + randomVideoFileName);
        videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG + randomVideoFileName);
    }

    private String[] getFileList(String strPath) {
        File fileRoot = new File(strPath);
        if (!fileRoot.isDirectory())
            return null;
        return fileRoot.list();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            camera = Camera.open(findBackFacingCamera());
            preview.refreshCamera(camera);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityController.setPangPangSelectActivity(null);
    }
}
