package com.karaokepang.Model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by clogic on 16. 3. 18..
 * 한곡의 가사 전체를 담당
 */
@Getter
@Setter
public class Lyrics {

    private List<List<Lyric>> lyrics = new ArrayList<>();
    private int index;
    private float width;

    public void add(List<Lyric> list) {
        lyrics.add(list);
    }
}
