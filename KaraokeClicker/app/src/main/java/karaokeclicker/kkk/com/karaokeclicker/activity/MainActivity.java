package karaokeclicker.kkk.com.karaokeclicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import butterknife.Bind;
import butterknife.ButterKnife;
import karaokeclicker.kkk.com.karaokeclicker.R;
import karaokeclicker.kkk.com.karaokeclicker.bluetooth.DeviceList;

public class MainActivity extends Activity {

    @Bind(R.id.btn_arrow)
    Button btnArrow;
    @Bind(R.id.btn_num)
    Button btnNum;
    @Bind(R.id.btn_search)
    Button btnSearch;
    @Bind(R.id.btn_bluetooth_connect)
    Button btnBluetotthConnect;
    @Bind(R.id.btn_bluetooth_send)
    Button btnBluetotthSend;

    private BluetoothSPP bluetoothSPP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        btnArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ArrowActivity.class));
            }
        });
        btnNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NumberActivity.class));
            }
        });
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
        btnBluetotthConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BluetoothTest.class));
//                Intent intent = new Intent(getApplicationContext(), DeviceList.class);
//                startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            }
        });

    }

}
