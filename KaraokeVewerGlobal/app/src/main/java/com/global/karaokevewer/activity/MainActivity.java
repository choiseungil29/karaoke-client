package com.global.karaokevewer.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.global.karaokevewer.R;
import com.global.karaokevewer.util.FilePath;

import java.io.File;
import java.util.Random;

public class MainActivity extends Activity implements View.OnClickListener {

    private VideoView videoView;
    private ImageButton btnOntv, btnVpang, btnDuet, btnMyAlbum, btnAudition, btnInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        setLayoutId();
        setVideoView();
    }

    private void setLayoutId() {
        videoView = (VideoView) findViewById(R.id.videoView);

        btnOntv = (ImageButton) findViewById(R.id.btn_ontv);
        btnVpang = (ImageButton) findViewById(R.id.btn_karaoke);
        btnDuet = (ImageButton) findViewById(R.id.btn_duet);
        btnMyAlbum = (ImageButton) findViewById(R.id.btn_myalbum);
        btnAudition = (ImageButton) findViewById(R.id.btn_audition);
        btnInternet = (ImageButton) findViewById(R.id.btn_internet);

        btnOntv.setOnClickListener(this);
        btnVpang.setOnClickListener(this);
        btnDuet.setOnClickListener(this);
        btnMyAlbum.setOnClickListener(this);
        btnAudition.setOnClickListener(this);
        btnInternet.setOnClickListener(this);
    }

    private void setVideoView() {
        videoView.setClickable(false);
        videoView.setFocusable(false);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.setVolume(0, 0);
                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.resume();
            }
        });
        setRandomVideoSource();
        videoView.start();
    }

    private void setRandomVideoSource() {
        String[] fileList = getFileList(FilePath.FILE_PATH_VPANGBG);
        if (fileList != null) {
            videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG + fileList[new Random().nextInt(fileList.length)]);
        } else {
            String path = "android.resource://" + getPackageName() + "/" + R.raw.produce;
            videoView.setVideoURI(Uri.parse(path));
        }
    }

    private String[] getFileList(String strPath) {
        File fileRoot = new File(strPath);
        if (!fileRoot.isDirectory())
            return null;
        return fileRoot.list();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ontv:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.everyontv"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_installed_application), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_karaoke:
//                ComponentName compName = new ComponentName("com.karaokepang", "com.karaokepang");
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent.setComponent(compName);
//                startActivity(intent);
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.karaokepang"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_installed_application), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_duet:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.karaokepang"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_installed_application), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_myalbum:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("org.xbmc.kodi"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_installed_application), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_audition:
                try {
                    startActivity(getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_installed_application), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_internet:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")));
                break;
        }
    }
}
