package com.vpang.clicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.vpang.clicker.database.dao.Song;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends Activity {


    private EditText editSearch, editNumber;
    private TextView textSelectNumber, textSelectSinger, textSelectSong;
    private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
            btnBackSpace, btnStart, btnSoundPlus, btnSoundMinus, btnKeyPlus, btnKeyMinus,
            btnTempoPlus, btnTempoMinus, btnMelody, btnReservation, btnReservationCancle, btnStop,
            btnVpang, btnDuet, btnAudtion, btnNewSong, btnSingerName, btnSongName, btnFavoriteName;
    private ImageView btnSearch;
    private ListView listSearch;
    private LinearLayout layoutSearch;

    private SearchAdapter searchAdapter;
    private List<Song> songs;
    //0 : number, 1: song, 2: singer
    private int mode = 0;

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
//            try {
//                JSONObject jsonObject = new JSONObject(readText("song.json"));
//                JSONArray songs = jsonObject.getJSONArray("songs");
//                for (int i = 0; i < songs.length(); i++) {
//                    JSONObject jo = songs.getJSONObject(i);
//                    Song song = new Song(jo.getString("songNumber"), jo.getString("song"), jo.getString("singer"), jo.getString("createDate"));
//                    song.save();
//                }
//
//            } catch (JSONException | IOException e) {
//                e.printStackTrace();
//            }
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
                        Log.i("kkk", sheet.getCell(0, row).getContents());
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
            }
        });
    }

    private void initButton() {
        btn0 = (Button) findViewById(R.id.btn_0);
        btn1 = (Button) findViewById(R.id.btn_1);
        btn2 = (Button) findViewById(R.id.btn_2);
        btn3 = (Button) findViewById(R.id.btn_3);
        btn4 = (Button) findViewById(R.id.btn_4);
        btn5 = (Button) findViewById(R.id.btn_5);
        btn6 = (Button) findViewById(R.id.btn_6);
        btn7 = (Button) findViewById(R.id.btn_7);
        btn8 = (Button) findViewById(R.id.btn_8);
        btn9 = (Button) findViewById(R.id.btn_9);
        btnBackSpace = (Button) findViewById(R.id.btn_backspace);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnSoundPlus = (Button) findViewById(R.id.btn_sound_plus);
        btnSoundMinus = (Button) findViewById(R.id.btn_sound_minus);
        btnKeyPlus = (Button) findViewById(R.id.btn_key_plus);
        btnKeyMinus = (Button) findViewById(R.id.btn_key_minus);
        btnTempoPlus = (Button) findViewById(R.id.btn_tempo_plus);
        btnTempoMinus = (Button) findViewById(R.id.btn_tempo_minus);
        btnMelody = (Button) findViewById(R.id.btn_melody);
        btnReservation = (Button) findViewById(R.id.btn_reservation);
        btnReservationCancle = (Button) findViewById(R.id.btn_reservation_cancle);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnVpang = (Button) findViewById(R.id.btn_vpang);
        btnDuet = (Button) findViewById(R.id.btn_duet);
        btnAudtion = (Button) findViewById(R.id.btn_audition);
        btnNewSong = (Button) findViewById(R.id.btn_new_song);
        btnSingerName = (Button) findViewById(R.id.btn_singer_name);
        btnSongName = (Button) findViewById(R.id.btn_song_name);
        btnFavoriteName = (Button) findViewById(R.id.btn_favorite_song);
        btnSearch = (ImageView) findViewById(R.id.btn_search);

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

        btnSoundPlus.setOnClickListener(onClickListenerOption);
        btnSoundMinus.setOnClickListener(onClickListenerOption);
        btnKeyPlus.setOnClickListener(onClickListenerOption);
        btnKeyMinus.setOnClickListener(onClickListenerOption);
        btnTempoPlus.setOnClickListener(onClickListenerOption);
        btnTempoMinus.setOnClickListener(onClickListenerOption);
        btnMelody.setOnClickListener(onClickListenerOption);
        btnReservation.setOnClickListener(onClickListenerOption);
        btnReservationCancle.setOnClickListener(onClickListenerOption);
        btnStop.setOnClickListener(onClickListenerOption);
        btnVpang.setOnClickListener(onClickListenerOption);
        btnDuet.setOnClickListener(onClickListenerOption);
        btnAudtion.setOnClickListener(onClickListenerOption);
        btnStart.setOnClickListener(onClickListenerOption);

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

            btnNewSong.setTextColor(Color.BLACK);
            btnSongName.setTextColor(Color.BLACK);
            btnSingerName.setTextColor(Color.BLACK);
            btnFavoriteName.setTextColor(Color.BLACK);

            searchAdapter = new SearchAdapter(searchSong(editNumber.getText().toString()));
            listSearch.setAdapter(searchAdapter);
        }
    };

    View.OnClickListener onClickListenerOption = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_sound_plus:
                    break;
                case R.id.btn_sound_minus:
                    break;
                case R.id.btn_key_plus:
                    break;
                case R.id.btn_key_minus:
                    break;
                case R.id.btn_tempo_plus:
                    break;
                case R.id.btn_tempo_minus:
                    break;
                case R.id.btn_melody:
                    break;
                case R.id.btn_reservation:
                    break;
                case R.id.btn_reservation_cancle:
                    break;
                case R.id.btn_stop:
                    break;
                case R.id.btn_start:
                    break;
                case R.id.btn_vpang:
                    break;
                case R.id.btn_duet:
                    break;
                case R.id.btn_audition:
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


                    btnNewSong.setTextColor(Color.RED);
                    btnSongName.setTextColor(Color.BLACK);
                    btnSingerName.setTextColor(Color.BLACK);
                    btnFavoriteName.setTextColor(Color.BLACK);

                    searchAdapter = new SearchAdapter(searchNewSong());
                    listSearch.setAdapter(searchAdapter);
                    break;

                case R.id.btn_song_name:
                    mode = 1;
                    layoutSearch.setVisibility(LinearLayout.VISIBLE);
                    editNumber.setText("");

                    btnNewSong.setTextColor(Color.BLACK);
                    btnSongName.setTextColor(Color.RED);
                    btnSingerName.setTextColor(Color.BLACK);
                    btnFavoriteName.setTextColor(Color.BLACK);
                    break;

                case R.id.btn_singer_name:
                    mode = 2;
                    layoutSearch.setVisibility(LinearLayout.VISIBLE);
                    editNumber.setText("");

                    btnNewSong.setTextColor(Color.BLACK);
                    btnSongName.setTextColor(Color.BLACK);
                    btnSingerName.setTextColor(Color.RED);
                    btnFavoriteName.setTextColor(Color.BLACK);
                    break;

                case R.id.btn_favorite_song:
                    layoutSearch.setVisibility(LinearLayout.GONE);
                    editSearch.setText("");
                    editNumber.setText("");

                    btnNewSong.setTextColor(Color.BLACK);
                    btnSongName.setTextColor(Color.BLACK);
                    btnSingerName.setTextColor(Color.BLACK);
                    btnFavoriteName.setTextColor(Color.RED);
                    break;

                case R.id.btn_search:
                    searchAdapter = new SearchAdapter(searchSong(editSearch.getText().toString()));
                    listSearch.setAdapter(searchAdapter);
                    break;
            }
        }
    };
}
