package com.karaokepang.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by clogic on 2016. 1. 15..
 */
@Getter
@Setter
@AllArgsConstructor(suppressConstructorProperties = true)
public class KSALyric {
    private String lyric;
    private long startTick;
    private long endTick;
}
