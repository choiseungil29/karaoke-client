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

import java.util.*;


/** @class MidiNote
 * A MidiNote contains
 *
 * startTime - The time (measured in pulses) when the note is pressed.
 * startTime - 노트가 눌렸을때의 시간
 * channel   - The channel the note is from. This is used when matching
 *             NoteOff events with the corresponding NoteOn event.
 *             The channels for the NoteOn and NoteOff events must be
 *             the same.
 * channel   - ~~~. 이것은 사용되어진다. NoteOff이벤트와 함께 상응하는 NoteOn이벤트와 맞춰서.
 *             channel이 같아야만 한다. NoteOn과 NoteOff 이벤트는
 * noteNumber- The note number, from 0 to 127.  Middle C is 60.
 *           - 노트 넘버, 0부터 127까지. 기본 도(C4)는 60
 * duration  - The time duration (measured in pulses) after which the 
 *             note is released.
 *           - 어느 노트가 눌려진 후의 시간 간격
 *
 * A MidiNote is created when we encounter a NoteOff event.  The duration
 * is initially unknown (set to 0).  When the corresponding NoteOff event
 * is found, the duration is set by the method NoteOff().
 */
public class MidiNote implements Comparator<MidiNote> {
    private int startTime;   /** The start time, in pulses */
    private int channel;     /** The channel */
    private int noteNumber;  /** The note, from 0 to 127. Middle C is 60 */
    private int duration;    /** The duration, in pulses */


    /** Create a new MidiNote.  This is called when a NoteOn event is
     * encountered in the MidiFile.
     */
    public MidiNote(int startTime, int channel, int noteNumber, int duration) {
        this.startTime = startTime;
        this.channel = channel;
        this.noteNumber = noteNumber;
        this.duration = duration;
    }


    public int getStartTime() { return startTime; }
    public void setStartTime(int value) { startTime = value; }

    public int getEndTime() { return startTime + duration; }

    public int getChannel() { return channel; }
    public void setChannel(int value) { channel = value; }

    public int getNumber() { return noteNumber; }
    public void setNumber(int value) { noteNumber = value; }

    public int getDuration() { return duration; }
    public void setDuration(int value) { duration = value; }

    /** A NoteOff event occurs for this note at the given time.
     * Calculate the note duration based on the noteoff event.
     */
    public void NoteOff(int endTime) {
        duration = endTime - startTime;
    }

    /** Compare two MidiNotes based on their start times.
     *  If the start times are equal, compare by their numbers.
     */
    public int compare(MidiNote x, MidiNote y) {
        if (x.getStartTime() == y.getStartTime())
            return x.getNumber() - y.getNumber();
        else
            return x.getStartTime() - y.getStartTime();
    }


    public MidiNote Clone() {
        return new MidiNote(startTime, channel, noteNumber, duration);
    }

    @Override
    public 
    String toString() {
        String[] scale = new String[]{ "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#" };
        return String.format("MidiNote channel=%1$s number=%2$s %3$s start=%4$s duration=%5$s",
                             channel, noteNumber, scale[(noteNumber + 3) % 12], startTime, duration);

    }

}


