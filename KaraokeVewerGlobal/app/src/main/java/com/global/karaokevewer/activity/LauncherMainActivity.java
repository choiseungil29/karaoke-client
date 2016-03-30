package com.global.karaokevewer.Activity;

import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.global.karaokevewer.Keys;
import com.global.karaokevewer.Model.FileUri;
import com.global.karaokevewer.R;
import com.global.karaokevewer.Util.FilePath;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

@EActivity(R.layout.activity_launcher_main)
public class LauncherMainActivity extends BluetoothActivity implements View.OnClickListener {

    @ViewById(R.id.videoView)
    VideoView videoView;
    private ImageButton btnOntv, btnVpang, btnDuet, btnMyAlbum, btnAudition, btnInternet;
    private ArrayList<FileUri> list;
    private ArrayList<String> localFiles = new ArrayList<>();

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        setContentView(R.layout.activity_launcher_main);
//
//        setLayoutId();
//        setVideoView();
//    }

    @Override
    public void afterViews() {
        super.afterViews();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setLayoutId();
        setVideoView();
    }

    private void setLayoutId() {

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
                try {
                    ComponentName compName = new ComponentName("com.karaokepang", "com.karaokepang.Activity.PangPangSelectActivity_");
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(compName);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.not_installed_application), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_duet:
                try {
                    ComponentName compName = new ComponentName("com.karaokepang", "com.karaokepang.Activity.DuetSelectActivity_");
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setComponent(compName);
                    startActivity(intent);
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

    public void loadSdcardMidiFiles() {
        File[] fileList = new File(FilePath.FILE_PATH_VPANGMID).listFiles();
        if (fileList == null)
            return;
        for (File file : fileList) {
            localFiles.add(file.getName());
            if (file.getName().endsWith(".mid")) {
                Uri uri = Uri.parse(file.getAbsolutePath());
                FileUri fileUri = new FileUri(uri, file.getName());
                list.add(fileUri);
            }
        }
    }

    private void deleteVideoData() {
        try {
            String[] fileList = getFileList(FilePath.FILE_PATH_VPANG);
            for (int i = 0; i < fileList.length; i++) {
                File file = new File(FilePath.FILE_PATH_VPANG + fileList[i]);
                boolean delete = file.delete();
                Log.d("kkk", file.getAbsoluteFile() + "삭제 완료 " + delete);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        deleteVideoData();
        bt.send(Keys.SendData.MODE_HOME, true);
    }

}
