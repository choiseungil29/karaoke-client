package com.global.karaokevewer.Activity;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.global.karaokevewer.R;
import com.global.karaokevewer.Util.FilePath;
import com.global.karaokevewer.camera.CameraPreview;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity
public class SelectActivity extends BluetoothActivity {

    public ActivityController activityController = ActivityController.getInstance();

    private Camera camera;
    public CameraPreview preview;
    private MediaRecorder recorder;
    public String fileName;

    @ViewById(R.id.vv_background)
    VideoView videoView;
    @ViewById(R.id.camera_layout)
    RelativeLayout layoutCamera;
    @ViewById(R.id.textView_song_selected)
    TextView textSongSelected;

    @Override
    public void afterViews() {

    }

    public void setCameraPreView() {
        preview = new CameraPreview(getApplicationContext(), camera);
        preview.setLayoutParams(new RelativeLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth() / 4, getWindowManager().getDefaultDisplay().getHeight() / 4));
        layoutCamera.addView(preview);
    }

    public void setDuetCameraPreView() {
        preview = new CameraPreview(getApplicationContext(), camera);
        int width = getWindowManager().getDefaultDisplay().getWidth() / 2;
        preview.setLayoutParams(new RelativeLayout.LayoutParams(width, (int) (width * 0.56)));
        layoutCamera.addView(preview);
    }

    public void setRandomVideoSource() {
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

    public int findBackFacingCamera() {
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


    public void stopRecord(boolean isRealFile) {
        if (recorder != null) {
            if (isRealFile) {
                recorder.stop();
                releaseMediaRecorder();
                Toast.makeText(getApplicationContext(), "녹화종료", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void startRecord(String songNumber) {
        if (!prepareMediaRecorder(songNumber)) {
            Toast.makeText(getApplicationContext(), "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
            finish();
        }
        // work on UiThread for better performance
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    recorder.start();
                    Toast.makeText(getApplicationContext(), "녹화시작", Toast.LENGTH_LONG).show();
                    activityController.getDuetSelectActivity().layoutPreiew.setVisibility(LinearLayout.VISIBLE);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean prepareMediaRecorder(String songNumber) {
        File dir = new File(FilePath.FILE_PATH_VPANG);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        recorder = new MediaRecorder();
        camera.unlock();
        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_1080P));
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));

        recorder.setVideoEncodingBitRate(1000000);
        //recorder.setVideoFrameRate(30);
        fileName = getNewFileName(songNumber);
        recorder.setOutputFile(FilePath.FILE_PATH_VPANG + fileName + ".mp4");
        recorder.setMaxDuration(6000000 * 10);
        recorder.setMaxFileSize(300000000 * 20);


        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    public String getNewFileName(String songNumber) {
        Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);
        int mm = c.get(Calendar.MONTH);
        int dd = c.get(Calendar.DAY_OF_MONTH);
        int hh = c.get(Calendar.HOUR_OF_DAY);
        int mi = c.get(Calendar.MINUTE);
        int ss = c.get(Calendar.SECOND);

        return songNumber + "-" + String.format(Locale.getDefault(), "%04d-%02d-%02d-%02d-%02d-%02d", yy, mm + 1, dd, hh, mi, ss);
    }


    private void releaseMediaRecorder() {
        if (recorder != null) {
            recorder.reset();
            recorder.release();
            recorder = null;
        }
    }


    public void cameraResume() {
        if (textSongSelected.getVisibility() == TextView.GONE) {
            stopRecord(true);
            textSongSelected.setVisibility(TextView.VISIBLE);
        }

        if (camera == null) {
            camera = Camera.open(findBackFacingCamera());
            preview.refreshCamera(camera);
        }
    }

}
