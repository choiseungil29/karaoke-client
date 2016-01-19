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

/** @class MidiEvent
 * A MidiEvent represents a single event (such as EventNoteOn) in the
 * Midi file. It includes the delta time of the event.
 */
public class MidiEvent implements Comparator<MidiEvent> {

    public int deltaTime;     /** The time between the previous event and this on */
    public int startTime;     /** The absolute time this event occurs */
    public boolean hasEventFlag; /** False if this is using the previous eventflag */
    public byte   eventFlag;     /** NoteOn, NoteOff, etc.  Full list is in class MidiFile */
    public byte channel;       /** The channel this event occurs on */

    public byte noteNumber;    /** The note number  */
    public byte velocity;      /** The volume of the note */
    public byte instrument;    /** The instrument */
    public byte keyPressure;   /** The key pressure */
    public byte channelPressure;  /** The channel pressure */
    public byte controlNum;    /** The controller number */
    public byte controlValue;  /** The controller value */
    public short pitchBend;      /** The pitch bend value */
    public byte numerator;     /** The numerator, for TimeSignature meta events */
    public byte denominator;   /** The denominator, for TimeSignature meta events */
    public int tempo;         /** The tempo, for tempo meta events */
    public byte metaEvent;     /** The metaevent, used if eventflag is MetaEvent */
    public int metaLength;    /** The metaevent length  */
    public byte[] value;         /** The raw byte value, for Sysex and meta events */

    public MidiEvent() {
    }

    /** Return a copy of this event */
    public MidiEvent Clone() {
        MidiEvent event= new MidiEvent();
        event.deltaTime = deltaTime;
        event.startTime = startTime;
        event.hasEventFlag = hasEventFlag;
        event.eventFlag = eventFlag;
        event.channel = channel;
        event.noteNumber = noteNumber;
        event.velocity = velocity;
        event.instrument = instrument;
        event.keyPressure = keyPressure;
        event.channelPressure = channelPressure;
        event.controlNum = controlNum;
        event.controlValue = controlValue;
        event.pitchBend = pitchBend;
        event.numerator = numerator;
        event.denominator = denominator;
        event.tempo = tempo;
        event.metaEvent = metaEvent;
        event.metaLength = metaLength;
        event.value = value;
        return event;
    }

    /** Compare two MidiEvents based on their start times. */
    public int compare(MidiEvent x, MidiEvent y) {
        if (x.startTime == y.startTime) {
            if (x.eventFlag == y.eventFlag) {
                return x.noteNumber - y.noteNumber;
            }
            else {
                return x.eventFlag - y.eventFlag;
            }
        }
        else {
            return x.startTime - y.startTime;
        }
    }

}


