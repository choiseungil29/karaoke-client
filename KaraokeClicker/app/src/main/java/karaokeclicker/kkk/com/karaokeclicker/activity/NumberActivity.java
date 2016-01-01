package karaokeclicker.kkk.com.karaokeclicker.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import karaokeclicker.kkk.com.karaokeclicker.R;

public class NumberActivity extends Activity implements View.OnClickListener {

    private BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        initBluetooth();

    }

    void setUp() {
        ImageView btnNum0 = (ImageView) findViewById(R.id.btn_num_0);
        ImageView btnNum1 = (ImageView) findViewById(R.id.btn_num_1);
        ImageView btnNum2 = (ImageView) findViewById(R.id.btn_num_2);
        ImageView btnNum3 = (ImageView) findViewById(R.id.btn_num_3);
        ImageView btnNum4 = (ImageView) findViewById(R.id.btn_num_4);
        ImageView btnNum5 = (ImageView) findViewById(R.id.btn_num_5);
        ImageView btnNum6 = (ImageView) findViewById(R.id.btn_num_6);
        ImageView btnNum7 = (ImageView) findViewById(R.id.btn_num_7);
        ImageView btnNum8 = (ImageView) findViewById(R.id.btn_num_8);
        ImageView btnNum9 = (ImageView) findViewById(R.id.btn_num_9);
        ImageView btnStart = (ImageView) findViewById(R.id.btn_start);
        ImageView btnReservation = (ImageView) findViewById(R.id.btn_reservation);
        ImageView btnTempoPlus = (ImageView) findViewById(R.id.btn_tempo_plus);
        ImageView btnTempoMinus = (ImageView) findViewById(R.id.btn_tempo_minus);
        ImageView btnKeyPlus = (ImageView) findViewById(R.id.btn_key_plus);
        ImageView btnkeyMinus = (ImageView) findViewById(R.id.btn_key_minus);
        btnNum0.setOnClickListener(this);
        btnNum1.setOnClickListener(this);
        btnNum2.setOnClickListener(this);
        btnNum3.setOnClickListener(this);
        btnNum4.setOnClickListener(this);
        btnNum5.setOnClickListener(this);
        btnNum6.setOnClickListener(this);
        btnNum7.setOnClickListener(this);
        btnNum8.setOnClickListener(this);
        btnNum9.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        btnReservation.setOnClickListener(this);
        btnTempoPlus.setOnClickListener(this);
        btnTempoMinus.setOnClickListener(this);
        btnKeyPlus.setOnClickListener(this);
        btnkeyMinus.setOnClickListener(this);
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
                Toast.makeText(NumberActivity.this, message, Toast.LENGTH_SHORT).show();
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setUp();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setUp();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        String sendData = "";
        switch (v.getId()) {
            case R.id.btn_num_0:
                sendData = "0";
                break;
            case R.id.btn_num_1:
                sendData = "1";
                break;
            case R.id.btn_num_2:
                sendData = "2";
                break;
            case R.id.btn_num_3:
                sendData = "3";
                break;
            case R.id.btn_num_4:
                sendData = "4";
                break;
            case R.id.btn_num_5:
                sendData = "5";
                break;
            case R.id.btn_num_6:
                sendData = "6";
                break;
            case R.id.btn_num_7:
                sendData = "7";
                break;
            case R.id.btn_num_8:
                sendData = "8";
                break;
            case R.id.btn_num_9:
                sendData = "9";
                break;
            case R.id.btn_start:
                sendData = "start";
                break;
            case R.id.btn_reservation:
                sendData = "reservation";
                break;
            case R.id.btn_tempo_plus:
                sendData = "tempo_plus";
                break;
            case R.id.btn_tempo_minus:
                sendData = "tempo_minus";
                break;
            case R.id.btn_key_plus:
                sendData = "key_plus";
                break;
            case R.id.btn_key_minus:
                sendData = "key_minus";
                break;
        }
        bt.send(sendData, true);
    }
}