package com.vpang.clicker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.orm.query.Condition;
import com.orm.query.Select;
import com.vpang.clicker.R;
import com.vpang.clicker.adapter.SearchAdapter;
import com.vpang.clicker.bluetooth.BluetoothActivity;
import com.vpang.clicker.bluetooth.SendData;
import com.vpang.clicker.database.dao.Song;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothState;
import jxl.Sheet;
import jxl.Workbook;

import static com.vpang.clicker.bluetooth.SendData.MODE_DUET;
import static com.vpang.clicker.bluetooth.SendData.MODE_VPANG;

public class MainActivity extends BluetoothActivity {

    private static ImageView btnHome;
    private static LinearLayout layoutMode;
    private static LinearLayout layoutModeSelect;
    private static LinearLayout layoutModeSelect2;

    private EditText editSearch, editNumber;
    private TextView textSelectNumber, textSelectSinger, textSelectSong;
    private ImageView btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
            btnBackSpace, btnStart, btnKeyPlus, btnKeyMinus,
            btnTempoPlus, btnTempoMinus, btnBackgroundVideo, btnReservation, btnReservationCancle, btnStop,
            btnVpang, btnDuet, btnAudtion, btnMusicSheetMode, btnNewSong, btnSingerName, btnSongName, btnFavoriteName;

    private ImageView btnSearch;
    private ListView listSearch;
    private LinearLayout layoutSearch;

    private SearchAdapter searchAdapter;
    private List<Song> songs;
    //0 : number, 1: song, 2: singer
    private int mode = 0;

    private String nowSelectedSong;
    private String nowSelectedSinger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDefaultData();
        initTextView();
        initLinearLayout();
        initEditText();
        initButton();
        initListView();
    }

    private void getDefaultData() {
        if (Song.listAll(Song.class).size() == 0) {
            readXcel();
        }
    }

    private void readXcel() {
        Workbook workbook = null;
        Sheet sheet = null;

        try {
            InputStream is = getBaseContext().getResources().getAssets().open("DBsample.xls");
            workbook = Workbook.getWorkbook(is);

            if (workbook != null) {
                sheet = workbook.getSheet(0);

                if (sheet != null) {
                    int row = 0;
                    while (true) {
                        ++row;
                        if (Strings.isNullOrEmpty(sheet.getCell(0, row).getContents())) {
                            continue;
                        }
                        String songNumber = sheet.getCell(0, row).getContents();
                        String songName = sheet.getCell(1, row).getContents();
                        String singer = sheet.getCell(2, row).getContents();
                        String createDate = sheet.getCell(3, row).getContents();

                        Song song = new Song(songNumber, songName, singer, createDate);
                        song.save();
                    }
                } else {
                    Log.e("kkk", "Sheet is null!!");
                }
            } else {
                Log.e("kkk", "WorkBook is null!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }

    }

    private String readText(String file) throws IOException {
        InputStream is = getAssets().open(file);

        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String text = new String(buffer);

        return text;
    }

    private List<Song> searchSong(String search) {

        if (mode == 0) {
            songs = Select.from(Song.class)
                    .where(Condition.prop("SONG_NUMBER").like("%" + search + "%"))
                    .list();
        } else if (mode == 1) {
            songs = Select.from(Song.class)
                    .where(Condition.prop("SONG").like("%" + search + "%"))
                    .list();
        } else {
            songs = Select.from(Song.class)
                    .where(Condition.prop("SINGER").like("%" + search + "%"))
                    .list();
        }
        return songs;

    }

    private List<Song> searchNewSong() {
        songs = Song.find(Song.class, "", new String[]{}, "", "CREATE_DATE", "50");
        Collections.reverse(songs);
        return songs;
    }

    private void initLinearLayout() {
        layoutSearch = (LinearLayout) findViewById(R.id.layout_search);
        layoutMode = (LinearLayout) findViewById(R.id.layout_mode);
        layoutModeSelect = (LinearLayout) findViewById(R.id.layout_mode_select);
        layoutModeSelect2 = (LinearLayout) findViewById(R.id.layout_mode_select2);
    }

    private void initTextView() {
        textSelectNumber = (TextView) findViewById(R.id.text_number);
        textSelectSinger = (TextView) findViewById(R.id.text_singer_name);
        textSelectSong = (TextView) findViewById(R.id.text_song_name);
    }

    private void initEditText() {
        editSearch = (EditText) findViewById(R.id.edit_search);
        editNumber = (EditText) findViewById(R.id.edit_number);
    }

    private void initListView() {
        searchAdapter = new SearchAdapter(searchNewSong());
        listSearch = (ListView) findViewById(R.id.list_search);
        listSearch.setAdapter(searchAdapter);
        listSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Song song = songs.get(position);
                textSelectNumber.setText(song.getSongNumber());
                textSelectSinger.setText(song.getSinger());
                textSelectSong.setText(song.getSong());

                nowSelectedSong = song.getSong();
                nowSelectedSinger = song.getSinger();
            }
        });
    }

    private void initButton() {
        btn0 = (ImageView) findViewById(R.id.btn_0);
        btn1 = (ImageView) findViewById(R.id.btn_1);
        btn2 = (ImageView) findViewById(R.id.btn_2);
        btn3 = (ImageView) findViewById(R.id.btn_3);
        btn4 = (ImageView) findViewById(R.id.btn_4);
        btn5 = (ImageView) findViewById(R.id.btn_5);
        btn6 = (ImageView) findViewById(R.id.btn_6);
        btn7 = (ImageView) findViewById(R.id.btn_7);
        btn8 = (ImageView) findViewById(R.id.btn_8);
        btn9 = (ImageView) findViewById(R.id.btn_9);

        btnBackSpace = (ImageView) findViewById(R.id.btn_backspace);

        btnStart = (ImageView) findViewById(R.id.btn_start);
        btnKeyPlus = (ImageView) findViewById(R.id.btn_key_plus);
        btnKeyMinus = (ImageView) findViewById(R.id.btn_key_minus);
        btnTempoPlus = (ImageView) findViewById(R.id.btn_tempo_plus);
        btnTempoMinus = (ImageView) findViewById(R.id.btn_tempo_minus);
        btnBackgroundVideo = (ImageView) findViewById(R.id.btn_background_select);
        btnReservation = (ImageView) findViewById(R.id.btn_reservation);
        btnReservationCancle = (ImageView) findViewById(R.id.btn_reservation_cancle);
        btnStop = (ImageView) findViewById(R.id.btn_stop);
        btnVpang = (ImageView) findViewById(R.id.btn_vpang);
        btnDuet = (ImageView) findViewById(R.id.btn_duet);
        btnAudtion = (ImageView) findViewById(R.id.btn_audition);
        btnSearch = (ImageView) findViewById(R.id.btn_search);
        btnMusicSheetMode = (ImageView) findViewById(R.id.btn_music_sheet_mode);

        btnNewSong = (ImageView) findViewById(R.id.btn_new_song);
        btnSingerName = (ImageView) findViewById(R.id.btn_singer_name);
        btnSongName = (ImageView) findViewById(R.id.btn_song_name);
        btnFavoriteName = (ImageView) findViewById(R.id.btn_favorite_song);
        btnHome = (ImageView) findViewById(R.id.btn_home);

        btn0.setOnClickListener(onClickListenerNumber);
        btn1.setOnClickListener(onClickListenerNumber);
        btn2.setOnClickListener(onClickListenerNumber);
        btn3.setOnClickListener(onClickListenerNumber);
        btn4.setOnClickListener(onClickListenerNumber);
        btn5.setOnClickListener(onClickListenerNumber);
        btn6.setOnClickListener(onClickListenerNumber);
        btn7.setOnClickListener(onClickListenerNumber);
        btn8.setOnClickListener(onClickListenerNumber);
        btn9.setOnClickListener(onClickListenerNumber);
        btnBackSpace.setOnClickListener(onClickListenerNumber);

        btnKeyPlus.setOnClickListener(onClickListenerOption);
        btnKeyMinus.setOnClickListener(onClickListenerOption);
        btnTempoPlus.setOnClickListener(onClickListenerOption);
        btnTempoMinus.setOnClickListener(onClickListenerOption);
        btnBackgroundVideo.setOnClickListener(onClickListenerOption);
        btnReservation.setOnClickListener(onClickListenerOption);
        btnReservationCancle.setOnClickListener(onClickListenerOption);
        btnStop.setOnClickListener(onClickListenerOption);
        btnVpang.setOnClickListener(onClickListenerOption);
        btnDuet.setOnClickListener(onClickListenerOption);
        btnAudtion.setOnClickListener(onClickListenerOption);
        btnStart.setOnClickListener(onClickListenerOption);
        btnHome.setOnClickListener(onClickListenerOption);
        btnMusicSheetMode.setOnClickListener(onClickListenerOption);

        btnNewSong.setOnClickListener(onClickListenerSearch);
        btnSingerName.setOnClickListener(onClickListenerSearch);
        btnSongName.setOnClickListener(onClickListenerSearch);
        btnFavoriteName.setOnClickListener(onClickListenerSearch);
        btnSearch.setOnClickListener(onClickListenerSearch);

    }

    View.OnClickListener onClickListenerNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            editSearch.setText("");
            layoutSearch.setVisibility(LinearLayout.GONE);
            mode = 0;
            switch (v.getId()) {
                case R.id.btn_0:
                    editNumber.setText(editNumber.getText().toString() + "0");
                    break;
                case R.id.btn_1:
                    editNumber.setText(editNumber.getText().toString() + "1");
                    break;
                case R.id.btn_2:
                    editNumber.setText(editNumber.getText().toString() + "2");
                    break;
                case R.id.btn_3:
                    editNumber.setText(editNumber.getText().toString() + "3");
                    break;
                case R.id.btn_4:
                    editNumber.setText(editNumber.getText().toString() + "4");
                    break;
                case R.id.btn_5:
                    editNumber.setText(editNumber.getText().toString() + "5");
                    break;
                case R.id.btn_6:
                    editNumber.setText(editNumber.getText().toString() + "6");
                    break;
                case R.id.btn_7:
                    editNumber.setText(editNumber.getText().toString() + "7");
                    break;
                case R.id.btn_8:
                    editNumber.setText(editNumber.getText().toString() + "8");
                    break;
                case R.id.btn_9:
                    editNumber.setText(editNumber.getText().toString() + "9");
                    break;
                case R.id.btn_backspace:
                    if (editNumber.getText().toString().length() != 0) {
                        editNumber.setText(editNumber.getText().toString().substring(0, editNumber.getText().length() - 1));
                    }
                    break;
            }

            textSelectNumber.setText("선택된 번호");
            textSelectSong.setText("선택된 노래 제목");
            textSelectSinger.setText("선택된 가수");

            searchAdapter = new SearchAdapter(searchSong(editNumber.getText().toString()));
            listSearch.setAdapter(searchAdapter);
        }
    };

    View.OnClickListener onClickListenerOption = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_key_plus:
                    break;
                case R.id.btn_key_minus:
                    break;
                case R.id.btn_tempo_plus:
                    break;
                case R.id.btn_tempo_minus:
                    break;
                case R.id.btn_background_select:
                    Intent intent = new Intent(MainActivity.this, BackgroundSelectActivity.class);
                    intent.putExtra("singer", nowSelectedSinger);
                    intent.putExtra("song", nowSelectedSong);
                    intent.putExtra("mode", MODE);
                    startActivity(intent);
                    break;
                case R.id.btn_reservation:
                    bt.send("reservation" + textSelectNumber.getText().toString()+"-"+textSelectSong.getText().toString(), true);
                    Toast.makeText(getApplicationContext(),"["+textSelectSong.getText().toString()+"]예약완료",Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_reservation_cancle:
                    break;
                case R.id.btn_stop:
                    bt.send(SendData.STOP, true);
                    break;
                case R.id.btn_start:
                    if (MODE.equals(MODE_DUET)) {
                        Intent intent2 = new Intent(MainActivity.this, BackgroundSelectActivity.class);
                        intent2.putExtra("singer", nowSelectedSinger);
                        intent2.putExtra("song", nowSelectedSong);
                        intent2.putExtra("mode", MODE);
                        startActivityForResult(intent2, 9081);
                    } else if (MODE.equals(MODE_VPANG)) {
                        Toast.makeText(getApplicationContext(), textSelectNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                        bt.send(textSelectNumber.getText().toString(), true);
                        textSelectNumber.setText("선택된 번호");
                        textSelectSong.setText("선택된 노래 제목");
                        textSelectSinger.setText("선택된 가수");
                    }

                    break;
                case R.id.btn_vpang:
                    bt.send(SendData.MODE_VPANG, true);
                    break;
                case R.id.btn_duet:
                    bt.send(SendData.MODE_DUET, true);
                    break;
                case R.id.btn_audition:
                    bt.send(SendData.MODE_AUDITION, true);
                    break;
                case R.id.btn_home:
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(MainActivity.this);
                    alt_bld.setMessage("홈으로 돌아가시겠습니까?").setCancelable(
                            false).setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    bt.send(SendData.MODE_HOME, true);
                                    editNumber.setText("");
                                }
                            }).setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = alt_bld.create();
                    alert.setTitle("종료");
                    alert.show();
                    break;
                case R.id.btn_music_sheet_mode:
                    bt.send(SendData.MUSIC_SHEET_MODE, true);
                    break;
            }
        }
    };

    View.OnClickListener onClickListenerSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_new_song:
                    layoutSearch.setVisibility(LinearLayout.GONE);
                    editSearch.setText("");
                    editNumber.setText("");

                    btnNewSong.setBackgroundResource(R.drawable.btn_new_song_select);
                    btnSongName.setBackgroundResource(R.drawable.btn_song_name);
                    btnSingerName.setBackgroundResource(R.drawable.btn_singer_name);
                    btnFavoriteName.setBackgroundResource(R.drawable.btn_favorite);

                    searchAdapter = new SearchAdapter(searchNewSong());
                    listSearch.setAdapter(searchAdapter);
                    break;

                case R.id.btn_song_name:
                    mode = 1;
                    layoutSearch.setVisibility(LinearLayout.VISIBLE);
                    editNumber.setText("");

                    btnNewSong.setBackgroundResource(R.drawable.btn_new_song);
                    btnSongName.setBackgroundResource(R.drawable.btn_song_name_select);
                    btnSingerName.setBackgroundResource(R.drawable.btn_singer_name);
                    btnFavoriteName.setBackgroundResource(R.drawable.btn_favorite);
                    break;

                case R.id.btn_singer_name:
                    mode = 2;
                    layoutSearch.setVisibility(LinearLayout.VISIBLE);
                    editNumber.setText("");

                    btnNewSong.setBackgroundResource(R.drawable.btn_new_song);
                    btnSongName.setBackgroundResource(R.drawable.btn_song_name);
                    btnSingerName.setBackgroundResource(R.drawable.btn_singer_name_select);
                    btnFavoriteName.setBackgroundResource(R.drawable.btn_favorite);
                    break;

                case R.id.btn_favorite_song:
                    layoutSearch.setVisibility(LinearLayout.GONE);
                    editSearch.setText("");
                    editNumber.setText("");

                    btnNewSong.setBackgroundResource(R.drawable.btn_new_song);
                    btnSongName.setBackgroundResource(R.drawable.btn_song_name);
                    btnSingerName.setBackgroundResource(R.drawable.btn_singer_name);
                    btnFavoriteName.setBackgroundResource(R.drawable.btn_favorite_select);
                    break;

                case R.id.btn_search:
                    searchAdapter = new SearchAdapter(searchSong(editSearch.getText().toString().toUpperCase().toString()));
                    listSearch.setAdapter(searchAdapter);
                    break;
            }
        }
    };


    @Override
    public void onStart() {
        super.onStart();
    }

    public static void buttonHomeMode() {
        btnHome.setVisibility(View.VISIBLE);
        layoutMode.setVisibility(View.GONE);
        layoutModeSelect.setVisibility(View.GONE);
        layoutModeSelect2.setVisibility(View.GONE);
    }

    public static void buttonLayoutMode() {
        btnHome.setVisibility(View.GONE);
        layoutMode.setVisibility(View.VISIBLE);
        layoutModeSelect.setVisibility(View.VISIBLE);
        layoutModeSelect2.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 9081) {
            if (resultCode == Activity.RESULT_OK) {
                String selectBack = data.getExtras().getString("select_back");
                Toast.makeText(getApplicationContext(), textSelectNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                bt.send(textSelectNumber.getText().toString() + "||" + selectBack, true);
                textSelectNumber.setText("선택된 번호");
                textSelectSong.setText("선택된 노래 제목");
                textSelectSinger.setText("선택된 가수");
            }
        } else {
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
    }
}
