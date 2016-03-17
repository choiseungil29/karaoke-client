package com.karaokepang.Activity;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.karaokepang.Midi.MidiFile;
import com.karaokepang.Midi.util.MidiUtil;
import com.karaokepang.R;
import com.karaokepang.View.CustomTextView;
import com.karaokepang.View.OutlineTextView;
import com.karaokepang.View.ScoreView;
import com.karaokepang.ftp.FtpServiceUp;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity
public class PlayActivity extends BaseActivity {

    MediaPlayer player;

    @ViewById(R.id.layout_song_name) LinearLayout layoutSongName;
    @ViewById(R.id.tv_songName) CustomTextView tv_songName;
    @ViewById(R.id.tv_composer) CustomTextView tv_composerName;
    @ViewById(R.id.tv_singer) CustomTextView tv_singer;

    @Override
    public void afterViews() {
        super.afterViews();
    }

    public void initMidiFile(Uri uri) {
        try {
            FileInputStream fis = new FileInputStream(uri.getPath());
            FileDescriptor fd = fis.getFD();
            player.reset();
            player.setDataSource(fd);
            player.prepare();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        File musicFile = new File(uri.getPath());
        tv_songName.setText(MidiUtil.getSongName(musicFile));
        tv_composerName.setText(MidiUtil.getComposer(musicFile));
        tv_singer.setText(MidiUtil.getSinger(musicFile));
    }

    public void play() {
        player.start();
    }
}
