/*
 * Copyright (c) 2011-2012 Madhav Vaidyanathan
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 2.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 */

package com.midisheetmusic;

import java.util.*;

import android.widget.*;
import android.view.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;

import com.duetwperson.R;

/**
 * @class IconArrayAdapter
 * The ListAdapter for displaying the list of songs,
 * and for displaying the list of files in a directory.
 * <p/>
 * Similar to the array adapter, but adds an icon
 * to the left side of each item displayed.
 * Midi files show a NotePair icon.
 */
public class IconArrayAdapter<T> extends ArrayAdapter<FileUri> {
    private LayoutInflater inflater;
    private static Bitmap midiIcon;       /* The midi icon */
    private static Bitmap directoryIcon;  /* The directory icon */
    private List<FileUri> fileUris;
    private ArrayList<FileUri> arraylist;
    /**
     * Load the NotePair image into memory.
     */
    public void LoadImages(Context context) {
        if (midiIcon == null) {
            Resources res = context.getResources();
            midiIcon = BitmapFactory.decodeResource(res, R.mipmap.notepair);
            directoryIcon = BitmapFactory.decodeResource(res, R.mipmap.directoryicon);
        }
    }

    /**
     * Create a new IconArrayAdapter. Load the NotePair image
     */
    public IconArrayAdapter(Context context, int resourceId, List<FileUri> objects) {
        super(context, resourceId, objects);
        LoadImages(context);
        inflater = LayoutInflater.from(context);
        fileUris = objects;
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(fileUris);
    }

    /**
     * Create a view for displaying a song in the ListView.
     * The view consists of a Note Pair icon on the left-side,
     * and the name of the song.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.choose_song_item, null);
        }
        TextView text = (TextView) convertView.findViewById(R.id.choose_song_name);
        ImageView image = (ImageView) convertView.findViewById(R.id.choose_song_icon);
        text.setHighlightColor(Color.WHITE);
//        FileUri file = (FileUri) this.getItem(position);
        FileUri file = fileUris.get(position);
        if (file.isDirectory()) {
            image.setImageBitmap(directoryIcon);
            text.setText(file.getUri().getPath());
        } else {
            image.setImageBitmap(midiIcon);
            text.setText(file.toString());
        }
        return convertView;
    }


    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        this.clear();
        if (charText.length() == 0) {
            fileUris.addAll(arraylist);
        } else {
            for (FileUri fileUri : arraylist) {
                if (fileUri.getDisplayName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    fileUris.add(fileUri);
                }
            }
        }
        notifyDataSetChanged();
    }
}

