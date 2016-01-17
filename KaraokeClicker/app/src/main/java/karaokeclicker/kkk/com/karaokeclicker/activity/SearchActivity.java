package karaokeclicker.kkk.com.karaokeclicker.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import karaokeclicker.kkk.com.karaokeclicker.R;
import karaokeclicker.kkk.com.karaokeclicker.activity.database.dao.Song;

public class SearchActivity extends Activity {

    @Bind(R.id.list_result)
    ListView listView;
    @Bind(R.id.edit_search)
    EditText editSearch;
    @Bind(R.id.spinner_search)
    Spinner spinnerSearch;
    @Bind(R.id.btn_search)
    Button btnSearch;

    private List<Song> songs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        getData();
        initListView();
        initButton();
        initSpinner();
        initListView();
    }

    void getData() {
        songs = Song.listAll(Song.class);
    }

    void initButton() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str_search = editSearch.getText().toString();
                if (spinnerSearch.getSelectedItemPosition() == 0) {
                    songs = Select.from(Song.class)
                            .where(Condition.prop("song").like("%" + str_search + "%"))
                            .list();
                } else if (spinnerSearch.getSelectedItemPosition() == 1) {
                    songs = Select.from(Song.class)
                            .where(Condition.prop("singer").like("%" + str_search + "%"))
                            .list();
                }
                listView.setAdapter(new CustomAdapter(songs));

            }
        });
    }

    void initSpinner() {
        String[] searchOption = {"제목", "가수"};
        spinnerSearch.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, searchOption));
    }

    void initListView() {
        listView.setAdapter(new CustomAdapter(songs));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NumberActivity.bt.send(songs.get(position).getSongNumber(), true);
            }
        });
    }

    class CustomAdapter extends BaseAdapter {
        List<Song> songs;


        public CustomAdapter(List<Song> songs) {
            this.songs = songs;
        }

        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int position) {
            return songs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            convertView = layoutInflater.inflate(R.layout.item_search, parent, false);

            Song song = songs.get(position);
            if (song != null) {
                TextView textNumber = (TextView) convertView.findViewById(R.id.text_number);
                TextView textSongSinger = (TextView) convertView.findViewById(R.id.text_song_singer);

                textNumber.setText(song.getSongNumber());
                textSongSinger.setText(song.getSong() + " - " + song.getSinger());

            }

            return convertView;
        }
    }
}
