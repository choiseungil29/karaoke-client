package com.karaokepang.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by clogic on 16. 3. 20..
 * 가사 한글자를 담당
 */
@AllArgsConstructor(suppressConstructorProperties = true)
@Getter
public class Lyric {
    private String parent;
    private String text;
    private long startTick;
    private long endTick;
}
