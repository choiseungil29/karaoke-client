package com.karaokepang.launcher;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.karaokepang.Activity.BluetoothActivity;
import com.karaokepang.Activity.TestActivity;
import com.karaokepang.Midi.event.meta.Text;
import com.karaokepang.R;
import com.karaokepang.Util.soundfont.MidiTest;
import com.karaokepang.ftp.FtpServiceDown;
import com.midisheetmusic.FileUri;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class LauncherMainActivity extends BluetoothActivity implements View.OnClickListener {

    private Button btnVpang;
    private Button btnFriend;
    private Button btnStart;

    private VideoView videoView;

    private TextView textLed;


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

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(getApplicationContext(), "[" + message + "]", Toast.LENGTH_SHORT).show();
                switch (message) {
                    case "mode_vpang":
                        Intent intent = new Intent(LauncherMainActivity.this, TestActivity.class);
                        intent.putExtra("mode", "vpang");
                        startActivity(intent);
                        break;
                    case "mode_friend":
                        Intent intent2 = new Intent(LauncherMainActivity.this, TestActivity.class);
                        intent2.putExtra("mode", "friend");
                        startActivity(intent2);
                        break;
                    case "mode_star":
//                        Toast.makeText(getApplicationContext(), "연결전...(수정중)", Toast.LENGTH_SHORT).show();
                        Intent intent3 = getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen");
                        startActivity(intent3);
                        break;
                }
//                chooseSong(message);
            }
        });
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
        File[] fileList = new File("/mnt/sdcard/vpang_mid").listFiles();
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
        textLed = (TextView) findViewById(R.id.text_led);
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
                intent2.putExtra("mode", "friend");
                startActivity(intent2);
                break;
            case R.id.btn_launcher_star:
//                Intent intent3 = getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen");
//                startActivity(intent3);
                startActivity(new Intent(LauncherMainActivity.this, MidiTest.class));
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (bt != null) {
            Log.e("kkk", "blue!!");
//            bt.autoConnect("vpang");
            bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
                public void onNewConnection(String name, String address) {
                    Log.e("kkk", "자동연결 성공");
                    // Do something when earching for new connection device
                }

                public void onAutoConnectionStarted() {
                    Log.e("kkk", "자동연결 성공2");
                    // Do something when auto connection has started
                }
            });
            if (!bt.isBluetoothEnabled()) {
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
            } else {
                if (!bt.isServiceAvailable()) {
                    bt.setupService();
                    bt.startService(BluetoothState.DEVICE_ANDROID);
                    bluetoothSetUp();
                }
            }
        }
    }
}
