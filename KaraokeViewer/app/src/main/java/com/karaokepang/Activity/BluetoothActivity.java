package com.karaokepang.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

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
                Log.i("kkk", "bluetooth = " + message);
//                chooseSong(message);
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
//                Toast.makeText(getApplicationContext()
//                        , "Connected to " + name + "\n" + address
//                        , Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "리모콘 연결 완료", Toast.LENGTH_SHORT).show();
//                DeviceList.deviceList.finish();
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

//        if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
//            bt.disconnect();
//        } else {
//            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
//            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
//        }
    }


    public void bluetoothSetUp() {

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (bt != null) {
//            bt.stopService();
//        }
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


}
