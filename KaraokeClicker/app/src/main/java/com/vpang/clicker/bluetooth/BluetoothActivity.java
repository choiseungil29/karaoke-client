package com.vpang.clicker.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.vpang.clicker.activity.MainActivity;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by 1002230 on 16. 2. 5..
 */
public class BluetoothActivity extends Activity {

    public BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBluetooth();
        if (!Strings.isNullOrEmpty(getAddree())) {
            bt.connect(getAddree());
        }
    }

    private String getAddree() {
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
                switch (message) {
                    case "mode_vpang":
                        MainActivity.buttonHomeMode();
                        break;
                    case "mode_duet":
                        MainActivity.buttonHomeMode();
                        break;
                    case "mode_home":
                        MainActivity.buttonLayoutMode();
                        break;
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
                bt.connect(getAddree());
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(getApplicationContext()
                        , "리모콘 연결에 실패했습니다. 다시시도해주세요.", Toast.LENGTH_SHORT).show();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bt != null) {
//            bt.autoConnect("vpang");
//            bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
//                public void onNewConnection(String name, String address) {
//                    Toast.makeText(getApplicationContext(), "새로운 자동연결", Toast.LENGTH_SHORT).show();
//                }
//
//                public void onAutoConnectionStarted() {
//                    Toast.makeText(getApplicationContext(), "자동연결 시작", Toast.LENGTH_SHORT).show();
//                }
//            });
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
}
