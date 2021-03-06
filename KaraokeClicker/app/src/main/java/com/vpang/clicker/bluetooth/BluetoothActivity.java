package com.vpang.clicker.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.vpang.clicker.activity.MainActivity;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

import static com.vpang.clicker.bluetooth.SendData.MODE_DUET;
import static com.vpang.clicker.bluetooth.SendData.MODE_HOME;
import static com.vpang.clicker.bluetooth.SendData.MODE_VPANG;
import static com.vpang.clicker.bluetooth.SendData.PLAYING;

/**
 * Created by 1002230 on 16. 2. 5..
 */
public class BluetoothActivity extends Activity {

    public String MODE = "null";
    public BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBluetooth();
        if (!Strings.isNullOrEmpty(getAddree())) {
            bt.connect(getAddree());
        }
    }

    public String getAddree() {
        SharedPreferences pref = getSharedPreferences("vpang", MODE_PRIVATE);
        return pref.getString("address", "");
    }

    void initBluetooth() {
        bt = new BluetoothSPP(this);
        bt.setupService();

        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                Log.i("kkk", "bluetooth = " + message);
                if (message.equals(MODE_VPANG)) {
                    MODE = MODE_VPANG;
                    MainActivity.buttonHomeMode();
                } else if (message.equals(MODE_DUET)) {
                    MODE = MODE_DUET;
                    MainActivity.buttonHomeMode();
                } else if (message.equals(MODE_HOME)) {
                    MODE = MODE_HOME;
                    MainActivity.buttonLayoutMode();
                }else if (message.equals(PLAYING)) {
                    Toast.makeText(getApplicationContext(),"노래가 재생중입니다",Toast.LENGTH_SHORT).show();
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
                        , "연결이 끊겼습니다. 다시 연결을 시도합니다", Toast.LENGTH_SHORT).show();
                MainActivity.buttonHomeMode();
                bt.connect(getAddree());
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "리모콘 연결에 실패했습니다. 다시시도해주세요.", Toast.LENGTH_SHORT).show();
                MainActivity.buttonHomeMode();
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });
        if (Strings.isNullOrEmpty(getAddree())) {
            if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                bt.disconnect();
            } else {
                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        }
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
            if (resultCode == Activity.RESULT_OK) {
                bt.connect(data);
                SharedPreferences pref = getSharedPreferences("vpang", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("address", data.getExtras().getString(BluetoothState.EXTRA_DEVICE_ADDRESS));
                editor.commit();
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
