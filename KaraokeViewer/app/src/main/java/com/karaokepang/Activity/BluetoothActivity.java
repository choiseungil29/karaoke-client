package com.karaokepang.Activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.karaokepang.Keys;
import com.karaokepang.Model.FileUri;
import com.karaokepang.Util.FilePath;

import org.androidannotations.annotations.EActivity;

import java.io.File;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by 1002230 on 16. 2. 5..
 */
@EActivity
public class BluetoothActivity extends BaseActivity {

    public BluetoothSPP bt;
    private ActivityController activityController = ActivityController.getInstance();

    @Override
    public void afterViews() {
        super.afterViews();
        initBluetooth();
    }

    private String getAddress() {
        SharedPreferences pref = getSharedPreferences("vpang", MODE_PRIVATE);
        return pref.getString("address", "");
    }

    public void initBluetooth() {
        bt = new BluetoothSPP(this);

        if (!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                if (message.equals(Keys.SendData.MODE_VPANG)) {
                    Intent intent = new Intent(getApplicationContext(), PangPangSelectActivity_.class);
                    intent.putExtra(Keys.MODE, Keys.Mode.PANGPANG);
                    startActivity(intent);
                    bt.send(Keys.SendData.MODE_VPANG, true);
                    Log.e("kkk", "pangpangSelect A = " + activityController.getPangPangSelectActivity());
                } else if (message.equals(Keys.SendData.MODE_DUET)) {
                    Intent intent = new Intent(getApplicationContext(), DuetSelectActivity_.class);
                    intent.putExtra(Keys.MODE, Keys.Mode.DUET);
                    bt.send(Keys.SendData.MODE_DUET, true);
                    startActivity(intent);
                    Log.e("kkk", "duetSelect A = " + activityController.getDuetSelectActivity());
                } else if (message.equals(Keys.SendData.MODE_HOME)) {
                    bt.send(Keys.SendData.MODE_HOME, true);
                    if (activityController.isDuetSelectMode()) {
                        activityController.getDuetSelectActivity().finish();
                    } else if (activityController.isPangSelectMode()) {
                        activityController.getPangPangSelectActivity().finish();
                    }
                    Log.e("kkk", "pangpangSelect A = " + activityController.getPangPangSelectActivity());
                    Log.e("kkk", "duetSelect A = " + activityController.getDuetSelectActivity());
                } else if (message.equals(Keys.SendData.MODE_AUDITION)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen");
                    startActivity(intent);
                } else if (message.equals(Keys.SendData.STOP)) {
                    if (activityController.isPangMode()) {
                        activityController.getPangPangActivity().finish();
                    } else if (activityController.isDuetMode()) {
                        activityController.getDuetActivity().finish();
                    }
                } else if (message.equals(Keys.SendData.MUSIC_SHEET_MODE)) {
//                    if (musicPlayActivity.layoutScore.getVisibility() == View.VISIBLE) {
//                        musicPlayActivity.layoutScore.setVisibility(View.INVISIBLE);
//                        Toast.makeText(getApplicationContext(), "악보모드x", Toast.LENGTH_SHORT).show();
//                    } else {
//                        musicPlayActivity.layoutScore.setVisibility(View.VISIBLE);
//                        Toast.makeText(getApplicationContext(), "악보모드o", Toast.LENGTH_SHORT).show();
//                    }
                } else {
                    if (activityController.getPangPangSelectActivity() != null || activityController.getDuetSelectActivity() != null) {
                        File file;
                        if (message.contains("||")) {
                            String[] splits = message.split("\\|\\|");
                            if (splits.length != 0) {
                                if (splits[1].equals("0")) {
                                    Log.e("kkk", "0번째 bg2");
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_001.mp4");
                                } else if (splits[1].equals("1")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_002.mp4");
                                } else if (splits[1].equals("2")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_003.mp4");
                                } else if (splits[1].equals("3")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_004.mp4");
                                } else if (splits[1].equals("4")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_005.mp4");
                                } else if (splits[1].equals("5")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_006.mp4");
                                } else if (splits[1].equals("6")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_007.mp4");
                                } else if (splits[1].equals("7")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_008.mp4");
                                } else if (splits[1].equals("8")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_009.mp4");
                                } else if (splits[1].equals("9")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_010.mp4");
                                } else if (splits[1].equals("10")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_011.mp4");
                                } else if (splits[1].equals("11")) {
                                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_012.mp4");
                                }
                            }
                            file = new File(FilePath.FILE_PATH_VPANGMID + splits[0] + ".mid");
                        } else {
                            file = new File(FilePath.FILE_PATH_VPANGMID + message + ".mid");
                        }
                        Uri uri = Uri.parse(file.getAbsolutePath());
                        FileUri fileUri = new FileUri(uri, file.getName());
                        if (activityController.isDuetSelectMode()) {
                            Intent intent = new Intent(activityController.getDuetSelectActivity(), DuetActivity_.class);
                            intent.setData(fileUri.getUri());
                            startActivity(intent);
                        } else if (activityController.isPangSelectMode()) {
                            Intent intent = new Intent(activityController.getPangPangSelectActivity(), PangPangActivity_.class);
                            intent.setData(fileUri.getUri());
                            startActivity(intent);
                        }
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
                if (DeviceListActivity.deviceListActivity != null) {
                    DeviceListActivity.deviceListActivity.finish();
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
                Intent intent = new Intent(getApplicationContext(), DeviceListActivity_.class);
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
}

