package com.karaokepang.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.Html;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;

import com.karaokepang.Keys;
import com.karaokepang.Model.FileUri;
import com.karaokepang.Util.FilePath;
import com.karaokepang.R;
import com.karaokepang.View.VerticalMarqueeTextView;
import com.karaokepang.bluetooth.SendData;
import com.karaokepang.ftp.FtpServiceDown;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

@EActivity(R.layout.activity_launcher_main)
public class LauncherMainActivity extends BluetoothActivity {


    @ViewById(R.id.btn_launcher_vpang) Button btnVpang;
    @ViewById(R.id.btn_launcher_friend) Button btnFriend;
    @ViewById(R.id.btn_launcher_star) Button btnStart;

    @ViewById(R.id.text_led) VerticalMarqueeTextView textLed;
    @ViewById(R.id.videoView) VideoView videoView;


    private ArrayList<FileUri> list;
    private ArrayList<String> localFiles = new ArrayList<>();

    @Override
    public void afterViews() {
        super.afterViews();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        initDefaultData();
        initVideoView();
        initTextView();
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
        videoView.setClickable(false);
        videoView.setFocusable(false);
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

    private void initTextView() {
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

    @Click(R.id.btn_launcher_vpang)
    public void clickedVpang() {
        Intent intent = new Intent(LauncherMainActivity.this, PangPangSelectActivity_.class);
        intent.putExtra(Keys.MODE, Keys.Mode.PANGPANG);
        startActivity(intent);
    }

    @Click(R.id.btn_launcher_friend)
    public void clickedFriend() {
        Intent intent2 = new Intent(LauncherMainActivity.this, DuetSelectActivity_.class);
        intent2.putExtra(Keys.MODE, Keys.Mode.DUET);
        startActivity(intent2);
    }

    @Click(R.id.btn_launcher_star)
    public void clickedStar() {
        Intent intent3 = getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen");
        startActivity(intent3);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bt.send(SendData.MODE_HOME, true);
    }
}
