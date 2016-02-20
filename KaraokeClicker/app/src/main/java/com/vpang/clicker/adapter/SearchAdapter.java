package com.vpang.clicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vpang.clicker.R;
import com.vpang.clicker.database.dao.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 1002230 on 16. 2. 21..
 */
public class SearchAdapter extends BaseAdapter {

    private List<Song> songs = new ArrayList<>();

    public SearchAdapter() {

    }

    public SearchAdapter(List<Song> songs) {
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
            TextView textSingerName = (TextView) convertView.findViewById(R.id.text_singer_name);
            TextView textSongName = (TextView) convertView.findViewById(R.id.text_song_name);

            textNumber.setText(song.getSongNumber());
            textSingerName.setText(song.getSinger());
            textSongName.setText(song.getSong());
        }

        return convertView;
    }
}