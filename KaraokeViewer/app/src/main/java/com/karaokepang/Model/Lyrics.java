package com.karaokepang.Model;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by clogic on 16. 3. 18..
 * 한곡의 가사 전체를 담당
 */
@Getter
@Setter
public class Lyrics {

    @Setter
    List<Lyric> lyrics = new ArrayList<>();
}
