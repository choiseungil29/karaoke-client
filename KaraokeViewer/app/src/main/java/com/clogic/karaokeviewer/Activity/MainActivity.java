package com.clogic.karaokeviewer.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.clogic.karaokeviewer.Dialog.ChooseSongDialog;
import com.clogic.karaokeviewer.R;
import com.midisheetmusic.FileUri;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class MainActivity extends AppCompatActivity {

    //private BluetoothSPP bt;

    VideoView vv_background;
    ArrayList<FileUri> list;
    ChooseSongDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initBluetooth();
        RelativeLayout rv_parent = (RelativeLayout) findViewById(R.id.rv_parent);
        rv_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseSong("1");
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
                chooseSong("7");
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

        dialog = new ChooseSongDialog(this, list);
    }

    void loadAssetMidiFiles() {
        try {
            AssetManager assets = this.getResources().getAssets();
            String[] files = assets.list("");
            for (String path : files) {
                if (path.endsWith(".mid")) {
                    Uri uri = Uri.parse("file:///android_asset/" + path);
                    FileUri file = new FileUri(uri, path);
                    list.add(file);
                }
            }
        } catch (IOException e) {
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

    private void chooseSong(String sendData) {
        if (!dialog.isShowing()) {
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            params.height = LinearLayout.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setAttributes(params);
            dialog.show();
        }
        //dialog.setData(sendData);
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

    void initBluetooth() {
        /*bt = new BluetoothSPP(this);

        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                chooseSong(message);
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
            bt.disconnect();
        } else {
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //bt.stopService();
    }

    @Override
    public void onStart() {
        super.onStart();
        /*if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                bluetoothSetUp();
            }
        }*/

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                bluetoothSetUp();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }*/
    }

    private void bluetoothSetUp() {

    }
}
