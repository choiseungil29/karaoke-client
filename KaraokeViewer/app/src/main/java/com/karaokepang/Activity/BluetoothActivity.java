package com.karaokepang.Activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.karaokepang.Keys;
import com.karaokepang.Model.FileUri;
import com.karaokepang.Util.FilePath;

import org.androidannotations.annotations.EActivity;

import java.io.File;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

/**
 * Created by 1002230 on 16. 2. 5..
 */
@EActivity
public class BluetoothActivity extends BaseActivity {

    private static int PANGPANG_RESULT = 100010;
    private static int DUET_RESULT = 100011;

    public static StringBuffer sbReservation = new StringBuffer();
    public static String[] reservation;
    public static String[] reservationName;
    public BluetoothSPP bt;
    private ActivityController activityController = ActivityController.getInstance();

    public static String[] getReservation() {
        Log.i("kkk", "reseravtion = " + reservation.length);
        return reservation;
    }

    public static String[] getReservationName() {
        Log.i("kkk", "reseravtionName = " + reservationName.length);
        return reservationName;
    }

    public static boolean isReservation() {
        boolean result = false;
        try {
            result = reservation.length >= 1;
            Log.e("kkk", "reservation.length = " + reservation.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("kkk", "reservation result = " + result);
        return result;
    }

    public static String getReservationNumber() {
        return reservation[0];
    }


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
                } else if (message.equals(Keys.SendData.MODE_DUET)) {
                    Intent intent = new Intent(getApplicationContext(), DuetSelectActivity_.class);
                    intent.putExtra(Keys.MODE, Keys.Mode.DUET);
                    bt.send(Keys.SendData.MODE_DUET, true);
                    startActivity(intent);
                } else if (message.equals(Keys.SendData.MODE_HOME)) {
                    bt.send(Keys.SendData.MODE_HOME, true);
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
                    ComponentName componentName = info.get(0).topActivity;
                    String activityName = componentName.getShortClassName().substring(1);
                    if (activityName.contains("PangPangActivity") && activityController.getPangPangActivity() != null) {
                        activityController.getPangPangActivity().finish();
                    } else if (activityName.contains("DuetActivity") && activityController.getDuetActivity() != null) {
                        activityController.getDuetActivity().finish();
                    }
                    if (activityController.isDuetSelectMode()) {
                        activityController.getDuetSelectActivity().finish();
                    } else if (activityController.isPangSelectMode()) {
                        activityController.getPangPangSelectActivity().finish();
                    }
                } else if (message.equals(Keys.SendData.MODE_AUDITION)) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.clipeo.eighteen");
                    startActivity(intent);
                } else if (message.equals(Keys.SendData.STOP)) {
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningTaskInfo> info = activityManager.getRunningTasks(1);
                    ComponentName componentName = info.get(0).topActivity;
                    String activityName = componentName.getShortClassName().substring(1);
                    if (activityName.contains("PangPangActivity")) {
                        activityController.getPangPangActivity().finishSign = true;
                        PangPangActivity.pangPangActivity.finish();
//                        activityController.getPangPangActivity().finish();
                    } else if (activityName.contains("DuetActivity")) {
                        activityController.getDuetActivity().finishSign = true;
                        DuetActivity.duetActivity.finish();
//                        activityController.getDuetActivity().finish();
                    }
                } else if (message.equals(Keys.SendData.MUSIC_SHEET_MODE)) {
                    if (activityController.getDuetActivity() != null) {
                        if (activityController.getDuetActivity().sv_score.getVisibility() == View.VISIBLE) {
                            activityController.getDuetActivity().sv_score.setVisibility(View.INVISIBLE);
                        } else {
                            activityController.getDuetActivity().sv_score.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    //예약곡
                    if (message.contains("reservation")) {
                        message = message.replace("reservation", "");
                        sbReservation.append("," + message);
                        String[] split = sbReservation.toString().split(",");
                        reservation = new String[split.length - 1];
                        reservationName = new String[split.length - 1];
                        int reservationCount = 0;
//                        Log.e("kkk", "splite = " + arrayJoin("#", split));
//                        Log.e("kkk", "splite = " + split.length);
                        for (int i = 1; i < split.length; i++) {
                            reservation[reservationCount] = split[i].split("-")[0];
                            reservationName[reservationCount] = split[i].split("-")[1];
//                            Log.e("kkk", "예약곡 " + (reservationCount) + ":" + reservation[reservationCount] + "," + reservationName[reservationCount]);
                            reservationCount++;
                        }
                        if (activityController.isDuetSelectMode()) {
                            activityController.getDuetSelectActivity().textReservation.setText(arrayJoin(", ", reservation));
                        } else if (activityController.isPangSelectMode()) {
                            activityController.getPangPangSelectActivity().textReservation.setText(arrayJoin(", ", reservation));
                        }
                    } else {
                        if (activityController.getPangPangActivity() == null && activityController.getDuetActivity() == null) {
                            if (activityController.getPangPangSelectActivity() != null || activityController.getDuetSelectActivity() != null) {
                                File file;
                                if (message.contains("||")) {
                                    String[] splits = message.split("\\|\\|");
                                    if (splits.length != 0) {
                                        if (splits[1].equals("0")) {
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
                                    Intent intent = new Intent(getApplicationContext(), DuetActivity_.class);
                                    intent.setData(fileUri.getUri());
                                    startActivityForResult(intent, DUET_RESULT);
                                } else if (activityController.isPangSelectMode()) {
                                    Intent intent = new Intent(getApplicationContext(), PangPangActivity_.class);
                                    intent.setData(fileUri.getUri());
                                    startActivityForResult(intent, PANGPANG_RESULT);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "모드를 선택해주세요", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            bt.send(Keys.SendData.PLAYING, true);
                        }
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
        Log.i("kkk", "onActivityResult = " + requestCode + "," + requestCode);
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
        } else if (requestCode == DUET_RESULT) {
            if (resultCode == 9991) {
                Log.i("kkk", "예약곡있어! duet = " + data.getStringExtra("number"));
                File file = new File(FilePath.FILE_PATH_VPANGMID + data.getStringExtra("number") + ".mid");
                Uri uri = Uri.parse(file.getAbsolutePath());
                FileUri fileUri = new FileUri(uri, file.getName());
                Intent intent = new Intent(this, DuetActivity_.class);
                intent.setData(fileUri.getUri());
                startActivityForResult(intent, DUET_RESULT);
                reservationSetting();
                if (null != activityController.getDuetSelectActivity()) {
                    activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_001.mp4");
                }
            }
        } else if (requestCode == PANGPANG_RESULT) {
            if (resultCode == 9991) {
                Log.i("kkk", "예약곡있어! pang = " + data.getStringExtra("number"));
                File file = new File(FilePath.FILE_PATH_VPANGMID + data.getStringExtra("number") + ".mid");
                Uri uri = Uri.parse(file.getAbsolutePath());
                FileUri fileUri = new FileUri(uri, file.getName());
                Intent intent = new Intent(this, PangPangActivity_.class);
                intent.setData(fileUri.getUri());
                startActivityForResult(intent, PANGPANG_RESULT);
                reservationSetting();
            }
        }
    }

    public void startPangPlay(String number) {
        Log.i("kkk", "예약곡있어! startDuetPlay = " + number);
        File file = new File(FilePath.FILE_PATH_VPANGMID + number + ".mid");
        Uri uri = Uri.parse(file.getAbsolutePath());
        FileUri fileUri = new FileUri(uri, file.getName());
        Intent intent = new Intent(this, PangPangActivity_.class);
        intent.setData(fileUri.getUri());
        startActivity(intent);
        reservationSetting();
    }

    public void startDuetPlay(String number) {
        Log.i("kkk", "예약곡있어! startDuetPlay = " + number);
        File file = new File(FilePath.FILE_PATH_VPANGMID + number + ".mid");
        Uri uri = Uri.parse(file.getAbsolutePath());
        FileUri fileUri = new FileUri(uri, file.getName());
        Intent intent = new Intent(this, DuetActivity_.class);
        intent.setData(fileUri.getUri());
        startActivity(intent);
        reservationSetting();
        if (null != activityController.getDuetSelectActivity()) {
            activityController.getDuetSelectActivity().videoView.setVideoPath(FilePath.FILE_PATH_VPANGBG2 + "CBG_001.mp4");
        }
    }

    private void reservationSetting() {
        Log.i("kkk", "reservationSetting");
        sbReservation = new StringBuffer();
        for (int i = 1; i < getReservation().length; i++) {
            sbReservation.append("," + getReservation()[i] + "-" + getReservationName()[i]);
        }

        String[] split = sbReservation.toString().split(",");
        reservation = new String[split.length - 1];
        reservationName = new String[split.length - 1];
        int reservationCount = 0;
//        Log.e("kkk", "splite = " + arrayJoin("#", split));
//        Log.e("kkk", "splite = " + split.length);
        for (int i = 1; i < split.length; i++) {
            reservation[reservationCount] = split[i].split("-")[0];
            reservationName[reservationCount] = split[i].split("-")[1];
//            Log.e("kkk", "예약곡 " + (reservationCount) + ":" + reservation[reservationCount] + "," + reservationName[reservationCount]);
            reservationCount++;
        }
        if (activityController.isDuetSelectMode()) {
//            Log.e("kkk", "!@# = " + arrayJoin(", ", reservationName));
            activityController.getDuetSelectActivity().textReservation.setText(arrayJoin(", ", reservation));
        } else if (activityController.isPangSelectMode()) {
//            Log.e("kkk", "!@#### = " + arrayJoin(", ", reservationName));
            activityController.getPangPangSelectActivity().textReservation.setText(arrayJoin(", ", reservation));
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

    public String arrayJoin(String glue, String array[]) {
        String result = "";

        for (int i = 0; i < array.length; i++) {
            result += array[i];
            if (i < array.length - 1) result += glue;
        }
        return result;
    }


    public void play(String songNumber,MediaPlayer mediaPlayer) {
        Log.e("kkk", "bluetooth play");
        //todo 녹화
//        if (activityController.getPangPangSelectActivity() != null) {
//            activityController.getPangPangSelectActivity().startRecord(songNumber);
//        }
//
//        if (activityController.getDuetSelectActivity() != null) {
//            activityController.getDuetSelectActivity().startRecord(songNumber);
//        }
    }
}

