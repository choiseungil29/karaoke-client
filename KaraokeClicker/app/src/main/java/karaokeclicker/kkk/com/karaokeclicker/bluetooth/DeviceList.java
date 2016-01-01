package karaokeclicker.kkk.com.karaokeclicker.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.R.id;
import app.akexorcist.bluetotohspp.library.R.layout;
import karaokeclicker.kkk.com.karaokeclicker.R;

import java.util.Iterator;
import java.util.Set;

@SuppressLint({"NewApi"})
public class DeviceList extends Activity {
    private static final String TAG = "BluetoothSPP";
    private static final boolean D = true;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private Button scanButton;
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if (DeviceList.this.mBtAdapter.isDiscovering()) {
                DeviceList.this.mBtAdapter.cancelDiscovery();
            }

            String strNoFound = DeviceList.this.getIntent().getStringExtra("no_devices_found");
            if (strNoFound == null) {
                strNoFound = "No devices found";
            }

            if (!((TextView) v).getText().toString().equals(strNoFound)) {
                String info = ((TextView) v).getText().toString();
                String address = info.substring(info.length() - 17);
                Intent intent = new Intent();
                intent.putExtra(BluetoothState.EXTRA_DEVICE_ADDRESS, address);
                DeviceList.this.setResult(-1, intent);
                DeviceList.this.finish();
            }

        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice strSelectDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (strSelectDevice.getBondState() != 12) {
                    String strNoFound = DeviceList.this.getIntent().getStringExtra("no_devices_found");
                    if (strNoFound == null) {
                        strNoFound = "No devices found";
                    }

                    if (((String) DeviceList.this.mPairedDevicesArrayAdapter.getItem(0)).equals(strNoFound)) {
                        DeviceList.this.mPairedDevicesArrayAdapter.remove(strNoFound);
                    }

                    DeviceList.this.mPairedDevicesArrayAdapter.add(strSelectDevice.getName() + "\n" + strSelectDevice.getAddress());
                }
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                DeviceList.this.setProgressBarIndeterminateVisibility(false);
                String strSelectDevice1 = DeviceList.this.getIntent().getStringExtra("select_device");
                if (strSelectDevice1 == null) {
                    strSelectDevice1 = "Select a device to connect";
                }

                DeviceList.this.setTitle(strSelectDevice1);
            }

        }
    };

    public DeviceList() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(5);
        int listId = this.getIntent().getIntExtra("layout_list", layout.device_list);
        this.setContentView(listId);
        String strBluetoothDevices = this.getIntent().getStringExtra("bluetooth_devices");
        if (strBluetoothDevices == null) {
            strBluetoothDevices = "Bluetooth Devices";
        }

        this.setTitle(strBluetoothDevices);
        this.setResult(0);
        this.scanButton = (Button) this.findViewById(R.id.button_scan);
        String strScanDevice = this.getIntent().getStringExtra("scan_for_devices");
        if (strScanDevice == null) {
            strScanDevice = "SCAN FOR DEVICES";
        }

        this.scanButton.setText(strScanDevice);
        this.scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                DeviceList.this.doDiscovery();
            }
        });
        int layout_text = this.getIntent().getIntExtra("layout_text", layout.device_name);
        this.mPairedDevicesArrayAdapter = new ArrayAdapter(this, layout_text);
        ListView pairedListView = (ListView) this.findViewById(R.id.list_devices);
        pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(this.mDeviceClickListener);
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.FOUND");
        this.registerReceiver(this.mReceiver, filter);
        filter = new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        this.registerReceiver(this.mReceiver, filter);
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        this.pairedDevices = this.mBtAdapter.getBondedDevices();
        if (this.pairedDevices.size() > 0) {
            Iterator noDevices = this.pairedDevices.iterator();

            while (noDevices.hasNext()) {
                BluetoothDevice device = (BluetoothDevice) noDevices.next();
                this.mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices1 = "No devices found";
            this.mPairedDevicesArrayAdapter.add(noDevices1);
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mBtAdapter != null) {
            this.mBtAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(this.mReceiver);
        this.finish();
    }

    private void doDiscovery() {
        Log.d("BluetoothSPP", "doDiscovery()");
        this.mPairedDevicesArrayAdapter.clear();
        String strScanning1;
        if (this.pairedDevices.size() > 0) {
            Iterator strScanning = this.pairedDevices.iterator();

            while (strScanning.hasNext()) {
                BluetoothDevice device = (BluetoothDevice) strScanning.next();
                this.mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            strScanning1 = this.getIntent().getStringExtra("no_devices_found");
            if (strScanning1 == null) {
                strScanning1 = "No devices found";
            }

            this.mPairedDevicesArrayAdapter.add(strScanning1);
        }

        strScanning1 = this.getIntent().getStringExtra("scanning");
        if (strScanning1 == null) {
            strScanning1 = "Scanning for devices...";
        }

        this.setProgressBarIndeterminateVisibility(true);
        this.setTitle(strScanning1);
        if (this.mBtAdapter.isDiscovering()) {
            this.mBtAdapter.cancelDiscovery();
        }

        this.mBtAdapter.startDiscovery();
    }
}
