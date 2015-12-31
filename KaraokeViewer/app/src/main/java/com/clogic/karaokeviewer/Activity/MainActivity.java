package com.clogic.karaokeviewer.Activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.clogic.karaokeviewer.Dialog.ChooseSongDialog;
import com.clogic.karaokeviewer.R;
import com.midisheetmusic.AllSongsActivity;
import com.midisheetmusic.ChooseSongActivity;
import com.midisheetmusic.FileUri;
import com.midisheetmusic.IconArrayAdapter;
import com.midisheetmusic.MidiSheetMusicActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    VideoView vv_background;

    ArrayList<FileUri> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout rv_parent = (RelativeLayout) findViewById(R.id.rv_parent);
        rv_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseSong();
            }
        });

        vv_background = (VideoView) findViewById(R.id.vv_background);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.produce);
        vv_background.setMediaController(new MediaController(this));
        vv_background.setVideoURI(video);
        vv_background.start();

        vv_background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseSong();
            }
        });

        list = new ArrayList<FileUri>();
        loadAssetMidiFiles();
        loadMidiFilesFromProvider(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        loadMidiFilesFromProvider(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

        // Sort the songList by name
        if (list.size() > 0) {
            Collections.sort(list, list.get(0));
        }

        // Remove duplicates
        ArrayList<FileUri> origlist = list;
        list = new ArrayList<FileUri>();
        String prevname = "";
        for (FileUri file : origlist) {
            if (!file.toString().equals(prevname)) {
                list.add(file);
            }
            prevname = file.toString();
        }
    }

    void loadAssetMidiFiles() {
        try {
            AssetManager assets = this.getResources().getAssets();
            String[] files = assets.list("");
            for (String path: files) {
                if (path.endsWith(".mid")) {
                    Uri uri = Uri.parse("file:///android_asset/" + path);
                    FileUri file = new FileUri(uri, path);
                    list.add(file);
                }
            }
        }
        catch (IOException e) {
        }
    }

    private void loadMidiFilesFromProvider(Uri content_uri) {
        ContentResolver resolver = getContentResolver();
        String columns[] = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.MIME_TYPE
        };
        String selection = MediaStore.Audio.Media.MIME_TYPE + " LIKE '%mid%'";
        Cursor cursor = resolver.query(content_uri, columns, selection, null, null);
        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        do {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int mimeColumn = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE);

            long id = cursor.getLong(idColumn);
            String title = cursor.getString(titleColumn);
            String mime = cursor.getString(mimeColumn);

            if (mime.endsWith("/midi") || mime.endsWith("/mid")) {
                Uri uri = Uri.withAppendedPath(content_uri, "" + id);
                FileUri file = new FileUri(uri, title);
                list.add(file);
            }
        } while (cursor.moveToNext());
        cursor.close();
    }

    private void chooseSong() {
        /*Intent intent = new Intent(this, AllSongsActivity.class);
        startActivity(intent);*/
        ChooseSongDialog dialog = new ChooseSongDialog(this, list);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        params.height = LinearLayout.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
