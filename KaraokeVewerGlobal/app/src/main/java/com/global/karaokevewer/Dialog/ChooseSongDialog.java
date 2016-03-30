package com.global.karaokevewer.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.global.karaokevewer.Activity.MusicPlayActivity;
import com.global.karaokevewer.Midi.MidiFile;
import com.global.karaokevewer.Model.FileUri;
import com.global.karaokevewer.R;
import com.global.karaokevewer.adapter.IconArrayAdapter;

import java.util.ArrayList;


/**
 * Created by clogic on 2015. 12. 30..
 */
public class ChooseSongDialog extends Dialog implements TextWatcher, AdapterView.OnItemClickListener {

    private ArrayList<FileUri> list;
    private IconArrayAdapter adapter;

    ListView lv_songs;
    EditText et_songs;

    private String sendData;
    private Context context;
    private MusicPlayActivity activity;

    public ChooseSongDialog(Context context, ArrayList<FileUri> list) {
        this(context, true);
        this.activity = (MusicPlayActivity) context;
        this.list = list;
        this.init(context);
    }

    public ChooseSongDialog(Context context) {
        this(context, true);
    }

    public ChooseSongDialog(Context context, boolean cancelable) {
        this(context, cancelable, null);
    }

    protected ChooseSongDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);

    }

    public void init(Context context) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_choose_song);

        this.context = context;

        adapter = new IconArrayAdapter(context, android.R.layout.simple_list_item_1, list);
        lv_songs = (ListView) findViewById(R.id.lv_songs);
        lv_songs.setAdapter(adapter);

        et_songs = (EditText) findViewById(R.id.et_songs);
        et_songs.addTextChangedListener(this);
        et_songs.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        et_songs.clearFocus();
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(et_songs.getWindowToken(), 0);

        lv_songs.setOnItemClickListener(this);
    }

    public void setData(String data) {
        String oldData = et_songs.getText().toString();
        et_songs.setText(oldData + data);
        Toast.makeText(context, oldData + data + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        adapter.filter(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileUri file = (FileUri) this.adapter.getItem(position);
        //ChooseSongActivity.openFile(file);
        byte[] data = file.getData((Activity) context);
        if (data == null || data.length <= 6 || !MidiFile.hasMidiHeader(data)) {
            Toast.makeText(parent.getContext(), "Error: Unable to open song: " + file.toString(), Toast.LENGTH_SHORT).show();
            return;
        }
//        Intent intent = new Intent(Intent.ACTION_VIEW, file.getUri(), context, MusicPlayActivity.class);
//        intent.putExtra(Prefs.MIDI_FILE_NAME, file.toString());
        activity.dialog.dismiss();
        activity.initVpang(file.getUri(), file.toString());

//        context.startActivity(intent);
    }
}
