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


import com.midisheetmusic.enums.Accidental;
import com.midisheetmusic.enums.NoteDuration;

/** @class NoteData
 *  Contains fields for displaying a single note in a chord.
 */
public class NoteData {
    public int number;             /** The Midi note number, used to determine the color */
    public WhiteNote whiteNote;    /** The white note location to draw */
    public NoteDuration duration;  /** The duration of the note */
    public boolean leftSide;       /** Whether to draw note to the left or right of the stem */
    public Accidental accidental;            /** Used to create the AccidSymbols for the chord */
}