package com.karaokepang.launcher;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.karaokepang.Util.FilePath;
import com.karaokepang.bluetooth.BluetoothActivity;
import com.karaokepang.Activity.TestActivity;
import com.karaokepang.R;
import com.karaokepang.View.VerticalMarqueeTextView;
import com.karaokepang.ftp.FtpServiceDown;
import com.midisheetmusic.FileUri;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class LauncherMainActivity extends BluetoothActivity implements View.OnClickListener {

    private Button btnVpang;
    private Button btnFriend;
    private Button btnStart;

    private VideoView videoView;

    private VerticalMarqueeTextView textLed;


    private ArrayList<FileUri> list;
    private ArrayList<String> localFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_main);

        initDefaultData();
        initVideoView();
        initTextVIew();

        btnVpang = (Button) findViewById(R.id.btn_launcher_vpang);
        btnFriend = (Button) findViewById(R.id.btn_launcher_friend);
        btnStart = (Button) findViewById(R.id.btn_launcher_star);

        btnVpang.setOnClickListener(this);
        btnFriend.setOnClickListener(this);
        btnStart.setOnClickListener(this);
    }

    void initDefaultData() {
        new FtpServiceDown(LauncherMainActivity.this, localFiles).execute();

        list = new ArrayList<>();
        loadSdcardMidiFiles();

        if (list.size() > 0) {
            Collections.sort(list, list.get(0));
        }

        ArrayList<FileUri> origlist = list;
        list = new ArrayList<>();
        String prevname = "";
        for (FileUri file : origlist) {
            if (!file.toString().equals(prevname)) {
                list.add(file);
            }
            prevname = file.toString();
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

    private void initVideoView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setClickable(false);
        videoView.setFocusable(false);
//        videoView.setVideoPath("/mnt/sdcard/vpang_bg/test1est1.TS");
        String path = "android.resource://" + getPackageName() + "/" + R.raw.produce;
        videoView.setVideoURI(Uri.parse(path));

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

    private void initTextVIew() {
        textLed = (VerticalMarqueeTextView) findViewById(R.id.text_led);
        String text = "<html>" +
                "<br /><br />" +
                "<font color='#ff3333'>Welcome to PANG PANG!</font><br /><br />" +
                "<font color='#ff0000'> UCC (User Created Contents)</font> 반주기는 3D 배경 영상으로 뮤직" +
                "비디오를 만들 수 있으며 다른 룸을 초청하거나 또는 외부에 있는 가족의 스마트 폰과 듀엣으로 " +
                "노래를 할 수 있는 새로운 반주기 입니다. <br /><br />" +
                "새로운 노래를 배우거나 악기 연주에 사용할 수 있는 악보음악을 제공하고 있습니다.<br /><br /><br />" +
                "</html>";
        textLed.setText(Html.fromHtml(text));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_launcher_vpang:
                Intent intent = new Intent(LauncherMainActivity.this, TestActivity.class);
                intent.putExtra("mode", "vpang");
                startActivity(intent);
                break;
            case R.id.btn_launcher_friend:
                Intent intent2 = new Intent(LauncherMainActivity.this, TestActivity.class);
                intent2.putExtra("mode", "duet");
                startActivity(intent2);
                break;
            case R.id.btn_launcher_star:
                Intent intent3 = getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen");
                startActivity(intent3);
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
