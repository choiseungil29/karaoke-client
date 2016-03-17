package com.karaokepang.Midi.util;

/**
 * Created by clogic on 16. 3. 18..
 * setting on ScoreView.java
 */
public class MidiInfo {
    public static int LINE_SPACE_HEIGHT; // 오선 사이의 공간
    public static int LINE_STROKE;
    public static int STEM_HEIGHT;
    public static int FIRST_LINE_HEIGHT; // 오선 맨 윗줄 높이
    public static int DEFAULT_C = 128; // 가장 기본이 되는 도의 위치. 중간에 계산되어지고 변경된다.
    public static int LOWER_NOTE_VALUE = 128;
    public static final int OCTAVE = 12;
    public static final int MEASURE_LIMIT = 4;

    public static int resolution = 0; // 한 박자의 단위길이
}
