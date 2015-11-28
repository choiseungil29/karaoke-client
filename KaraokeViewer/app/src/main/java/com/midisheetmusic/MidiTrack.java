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



/** @class MidiTrack
 * The MidiTrack takes as input the raw MidiEvents for the track, and gets:
 * - The list of midi notes in the track.
 * - The first instrument used in the track.
 *
 * For each NoteOn event in the midi file, a new MidiNote is created
 * and added to the track, using the AddNote() method.
 * 
 * The NoteOff() method is called when a NoteOff event is encountered,
 * in order to update the duration of the MidiNote.
 */ 
public class MidiTrack {
    private int trackNum;                 /** The track number */
    private ArrayList<MidiNote> notes;    /** List of Midi notes */
    private int instrument;               /** instrument for this track */
    private ArrayList<MidiEvent> lyrics;  /** The lyrics in this track */

    /** Create an empty MidiTrack.  Used by the Clone method */
    public MidiTrack(int trackNum) {
        this.trackNum = trackNum;
        notes = new ArrayList<MidiNote>(20);
        instrument = 0;
    } 

    /** Create a MidiTrack based on the Midi events. Extract the NoteOn/NoteOff
     *  events to gather the list of MidiNotes.
     */
    public MidiTrack(ArrayList<MidiEvent> events, int trackNum) {
        this.trackNum = trackNum;
        notes = new ArrayList<MidiNote>(events.size());
        instrument = 0;
 
        for (MidiEvent event : events) {
            if (event.eventFlag == MidiFile.EventNoteOn && event.velocity > 0) {
                MidiNote note = new MidiNote(event.startTime, event.channel, event.noteNumber, 0);
                AddNote(note);
            }
            else if (event.eventFlag == MidiFile.EventNoteOn && event.velocity == 0) {
                NoteOff(event.channel, event.noteNumber, event.startTime);
            }
            else if (event.eventFlag == MidiFile.EventNoteOff) {
                NoteOff(event.channel, event.noteNumber, event.startTime);
            }
            else if (event.eventFlag == MidiFile.EventProgramChange) {
                instrument = event.instrument;
            }
            else if (event.metaEvent == MidiFile.MetaEventLyric) {
                AddLyric(event);
                if (lyrics == null) {
                    lyrics = new ArrayList<MidiEvent>();
                }
                lyrics.add(event);
            }
        }
        if (notes.size() > 0 && notes.get(0).getChannel() == 9)  {
            instrument = 128;  /* Percussion */
        }
    }

    public int trackNumber() { return trackNum; }

    public ArrayList<MidiNote> getNotes() { return notes; }

    public int getInstrument() { return instrument; }
    public void setInstrument(int value) { instrument = value; }

    public ArrayList<MidiEvent> getLyrics() { return lyrics; }
    public void setLyrics(ArrayList<MidiEvent> value) { lyrics = value; }


    public String getInstrumentName() { if (instrument >= 0 && instrument <= 128)
                  return MidiFile.Instruments[instrument];
              else
                  return "";
            }

    /** Add a MidiNote to this track.  This is called for each NoteOn event */
    public void AddNote(MidiNote m) {
        notes.add(m);
    }

    /** A NoteOff event occured.  Find the MidiNote of the corresponding
     * NoteOn event, and update the duration of the MidiNote.
     */
    public void NoteOff(int channel, int notenumber, int endtime) {
        for (int i = notes.size()-1; i >= 0; i--) {
            MidiNote note = notes.get(i);
            if (note.getChannel() == channel && note.getNumber() == notenumber &&
                note.getDuration() == 0) {
                note.NoteOff(endtime);
                return;
            }
        }
    }

    /** Add a lyric event to this track */
    public void AddLyric(MidiEvent mevent) { 
        if (lyrics == null) {
            lyrics = new ArrayList<MidiEvent>();
        }
        lyrics.add(mevent);
    }

    /** Return a deep copy clone of this MidiTrack. */
    public MidiTrack Clone() {
        MidiTrack track = new MidiTrack(trackNumber());
        track.instrument = instrument;
        for (MidiNote note : notes) {
            track.notes.add( note.Clone() );
        }
        if (lyrics != null) {
            track.lyrics = new ArrayList<MidiEvent>();
            for (MidiEvent ev : lyrics) {
                track.lyrics.add(ev);
            }
        }
        return track;
    }

    @Override
    public String toString() {
        String result = "Track number=" + trackNum + " instrument=" + instrument + "\n";
        for (MidiNote n : notes) {
           result = result + n + "\n";
        }
        result += "End Track\n";
        return result;
    }
}


