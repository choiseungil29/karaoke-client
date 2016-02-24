package com.karaokepang.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.karaokepang.Activity.DeviceList;
import com.karaokepang.Activity.TestActivity;
import com.midisheetmusic.FileUri;

import java.io.File;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by 1002230 on 16. 2. 5..
 */
public class BluetoothActivity extends Activity {


    public static TestActivity testActivity;
    public BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBluetooth();
    }

    private String getAddress() {
        SharedPreferences pref = getSharedPreferences("vpang", MODE_PRIVATE);
        return pref.getString("address", "");
    }

    void initBluetooth() {
        bt = new BluetoothSPP(this);

        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                if (message.equals(SendData.MODE_VPANG)) {
                    Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                    intent.putExtra("mode", "vpang");
                    startActivity(intent);
                    bt.send(SendData.MODE_VPANG, true);
                } else if (message.equals(SendData.MODE_DUET)) {
                    Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                    intent.putExtra("mode", "duet");
                    bt.send(SendData.MODE_DUET, true);
                    startActivity(intent);
                } else if (message.equals(SendData.MODE_HOME)) {
                    bt.send(SendData.MODE_HOME, true);
                    if (testActivity != null) {
                        testActivity.finish();
                    }
                } else if (message.equals(SendData.MODE_AUDITION)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen");
                    startActivity(intent);
                } else if (message.equals(SendData.STOP)) {
                    if (testActivity != null) {
                        testActivity.reset();
                    }
                } else if (message.equals(SendData.MUSIC_SHEET_MODE)) {
                    if (testActivity.layoutScore.getVisibility() == View.VISIBLE) {
                        testActivity.layoutScore.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "악보모드x", Toast.LENGTH_SHORT).show();
                    } else {
                        testActivity.layoutScore.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), "악보모드o", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (testActivity != null) {
                        Toast.makeText(getApplicationContext(), "노래시작", Toast.LENGTH_SHORT).show();
                        File file = new File("/mnt/sdcard/vpang_mid/" + message + ".mid");
                        Uri uri = Uri.parse(file.getAbsolutePath());
                        FileUri fileUri = new FileUri(uri, file.getName());
                        testActivity.initVpang(fileUri.getUri(), fileUri.toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "모드를 선택해주세요", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Log.d("kkk", "연결된 주소 = " + address);
                SharedPreferences pref = getSharedPreferences("vpang", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("address", address);
                editor.commit();
                Toast.makeText(getApplicationContext(), "리모콘 연결 완료", Toast.LENGTH_SHORT).show();
                if (DeviceList.deviceList != null) {
                    DeviceList.deviceList.finish();
                }
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

        if (Strings.isNullOrEmpty(getAddress())) {
            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                bt.disconnect();
            } else {
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        }
    }


    public void bluetoothSetUp() {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bt != null) {
            bt.stopService();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (bt == null) {
            return;
        }
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
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
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bt != null) {
            if (!bt.isBluetoothEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
            } else {
                if (!bt.isServiceAvailable()) {
                    bt.setupService();
                    bt.startService(BluetoothState.DEVICE_ANDROID);
                    bluetoothSetUp();
                }
            }
        }
    }

    public static TestActivity getTestActivity() {
        return testActivity;
    }

    public static void setTestActivity(TestActivity testActivity) {
        BluetoothActivity.testActivity = testActivity;
    }
}
