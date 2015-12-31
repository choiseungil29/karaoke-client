package com.clogic.karaokeviewer.Activity;

import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.clogic.karaokeviewer.Midi.MidiFile;
import com.clogic.karaokeviewer.R;
import com.clogic.karaokeviewer.Util.Logger;
import com.clogic.karaokeviewer.Util.Prefs;
import com.clogic.karaokeviewer.View.ScoreView;
import com.midisheetmusic.ClefSymbol;
import com.midisheetmusic.MidiPlayer;
import com.midisheetmusic.TimeSignatureSymbol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by clogic on 2015. 12. 10..
 */
public class TestActivity extends AppCompatActivity implements MusicListener {
    private MidiFile midi = null;

    @Bind(R.id.sv_score) ScoreView scoreView;
    @Bind(R.id.tv_lyrics) TextView tv_lyrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        ButterKnife.bind(this);

        ClefSymbol.LoadImages(this);
        TimeSignatureSymbol.LoadImages(this);
        MidiPlayer.LoadImages(this);

        Uri uri = this.getIntent().getData();
        /*String title = this.getIntent().getStringExtra(MidiTitleID);
        if (title == null) {
            title = uri.getLastPathSegment();
        }*/

        try {
            AssetManager assetManager = getResources().getAssets();
            InputStream stream = assetManager.open(uri.getLastPathSegment());
            Logger.i("File name : " + uri.getLastPathSegment());
            scoreView.setMidiFile(new MidiFile(stream), getIntent().getStringExtra(Prefs.MIDI_FILE_NAME));
            scoreView.setFileUri(uri);
            scoreView.setListener(this);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //scoreView.callOnDraw();
    }

    @Override
    public void notifyMeasureChanged(ArrayList<String> lyrics) {
        String text = "";
        for(String lyric : lyrics) {
            text += lyric;
            text += "\r\n";
        }
        final String finalText = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_lyrics.setText(finalText);
            }
        });
    }
}

