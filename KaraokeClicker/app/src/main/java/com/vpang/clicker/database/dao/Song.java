package com.vpang.clicker.database.dao;

import com.orm.SugarRecord;

/**
 * Created by 1002230 on 16. 1. 15..
 */
public class Song extends SugarRecord {

    String songNumber;
    String song;
    String singer;
    String createDate;

    public Song() {
    }

    public Song(String songNumber, String song, String singer, String createDate) {
        this.songNumber = songNumber;
        this.song = song;
        this.singer = singer;
        this.createDate = createDate;
    }

    public String getSongNumber() {
        return songNumber;
    }

    public void setSongNumber(String songNumber) {
        this.songNumber = songNumber;
    }

    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return "Song{" +
                "songNumber='" + songNumber + '\'' +
                ", song='" + song + '\'' +
                ", singer='" + singer + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }
}
