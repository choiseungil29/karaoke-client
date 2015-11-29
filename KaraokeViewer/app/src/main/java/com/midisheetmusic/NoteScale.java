/*
 * Copyright (c) 2007-2011 Madhav Vaidyanathan
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


/** Enumeration of the notes in a scale (A, A#, ... G#) */
public class NoteScale {
    public static final int A      = 0;
    public static final int ASharp = 1;
    public static final int BFlat = 1;
    public static final int B      = 2;
    public static final int C      = 3;
    public static final int CSharp = 4;
    public static final int DFlat = 4;
    public static final int D      = 5;
    public static final int DSharp = 6;
    public static final int EFlat = 6;
    public static final int E      = 7;
    public static final int F      = 8;
    public static final int FSharp = 9;
    public static final int GFlat = 9;
    public static final int G      = 10;
    public static final int GSharp = 11;
    public static final int AFlat = 11;

    /** Convert a note (A, A#, B, etc) and octave into a
     * Midi Note number.
     * 노트와 옥타브를 미디 노트 넘버로 변환한다
     */
    public static int toMidiNumber(int noteScale, int octave) {
        return 9 + noteScale + octave * 12;
    }

    /** Convert a Midi note number into a notescale (A, A#, B)
     * 미디 노트 넘버를 노트스케일로 변경한다
     */
    public static int fromMidiNumber(int number) {
        return (number + 3) % 12;
    }

    /** Return true if this notescale number is a black key
     * 블랙키(검은건반# 말하는듯) 면 true를 리턴한다
     */
    public static boolean IsBlackKey(int notescale) {
        if (notescale == ASharp ||
            notescale == CSharp ||
            notescale == DSharp ||
            notescale == FSharp ||
            notescale == GSharp) {

            return true;
        }
        else {
            return false;
        }
    }
}


