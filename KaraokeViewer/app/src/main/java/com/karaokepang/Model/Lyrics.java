package com.karaokepang.Model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Created by clogic on 16. 3. 18..
 * 한곡의 가사 전체를 담당
 */
@Getter
public class Lyrics {

    // 전체 라인을 담당한다.
    List<List<Lyric>> allLines = new ArrayList<>();

    // 한 라인을 담당한다. 다 채우면 allLines로 들어가고 reset됨.
    List<Lyric> oneLine = new ArrayList<>();

    public void append(Lyric lyric) {
        oneLine.add(lyric);
    }

    /**
     * 가사 한글자를 담당
     */
    public class Lyric {
        private String text;
        private long startTick;
        private long endTick;
    }
}
