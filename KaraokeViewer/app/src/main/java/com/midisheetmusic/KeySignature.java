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
import com.midisheetmusic.enums.Clef;

import java.util.List;

/** @class KeySignature
 * The KeySignature class represents a key signature, like G Major
 * or B-flat Major.  For sheet music, we only care about the number
 * of sharps or flats in the key signature, not whether it is major
 * or minor.
 *
 * The main operations of this class are:
 * - Guessing the key signature, given the notes in a song.
 * - Generating the accidental symbols for the key signature.
 * - Determining whether a particular note requires an accidental
 *   or not.
 *
 */

public class KeySignature {
    /** The number of sharps in each key signature */
    public static final int C = 0;
    public static final int G = 1;
    public static final int D = 2;
    public static final int A = 3;
    public static final int E = 4;
    public static final int B = 5;

    /** The number of flats in each key signature */
    public static final int F = 1;
    public static final int Bflat = 2;
    public static final int Eflat = 3;
    public static final int Aflat = 4;
    public static final int Dflat = 5;
    public static final int Gflat = 6;

    /** The two arrays below are key maps.  They take a major key
     * (like G major, B-flat major) and a note in the scale, and
     * return the Accidental required to display that note in the
     * given key.  In a nutshel, the map is
     *
     *   map[Key][NoteScale] -> Accidental
     */
    private static Accidental[][] sharpkeys;
    private static Accidental[][] flatkeys;

    private int num_flats;   /** The number of sharps in the key, 0 thru 6 */
    private int num_sharps;  /** The number of flats in the key, 0 thru 6 */

    /** The accidental symbols that denote this key, in a treble clef */
    private AccidentalSymbol[] treble;

    /** The accidental symbols that denote this key, in a bass clef */
    private AccidentalSymbol[] bass;

    /** The key map for this key signature:
     *   keymap[notenumber] -> Accidental
     */
    private Accidental[] keymap;

    /** The measure used in the previous call to GetAccidental() */
    private int prevmeasure; 


    /** Create new key signature, with the given number of
     * sharps and flats.  One of the two must be 0, you can't
     * have both sharps and flats in the key signature.
     */
    public KeySignature(int num_sharps, int num_flats) {
        if (!(num_sharps == 0 || num_flats == 0)) {
            throw new IllegalArgumentException();
        }
        this.num_sharps = num_sharps;
        this.num_flats = num_flats;

        CreateAccidentalMaps();
        keymap = new Accidental[160];
        ResetKeyMap();
        CreateSymbols();
    }

    /** Create new key signature, with the given noteScale.
     */
    public KeySignature(int noteScale) {
        num_sharps = num_flats = 0;
        switch (noteScale) {
            case NoteScale.A:     num_sharps = 3; break;
            case NoteScale.BFlat: num_flats = 2;  break;
            case NoteScale.B:     num_sharps = 5; break;
            case NoteScale.C:     break;
            case NoteScale.DFlat: num_flats = 5;  break;
            case NoteScale.D:     num_sharps = 2; break;
            case NoteScale.EFlat: num_flats = 3;  break;
            case NoteScale.E:     num_sharps = 4; break;
            case NoteScale.F:     num_flats = 1;  break;
            case NoteScale.GFlat: num_flats = 6;  break;
            case NoteScale.G:     num_sharps = 1; break;
            case NoteScale.AFlat: num_flats = 4;  break;
            default:              throw new IllegalArgumentException(); 
        }
        
        CreateAccidentalMaps();
        keymap = new Accidental[160];
        ResetKeyMap();
        CreateSymbols();
    }

    /** Iniitalize the sharpkeys and flatkeys maps */
    private static void CreateAccidentalMaps() {
        if (sharpkeys != null)
            return; 

        Accidental[] map;
        sharpkeys = new Accidental[8][];
        flatkeys = new Accidental[8][];

        for (int i = 0; i < 8; i++) {
            sharpkeys[i] = new Accidental[12];
            flatkeys[i] = new Accidental[12];
        }

        map = sharpkeys[C];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.ASharp] = Accidental.Flat;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.CSharp] = Accidental.Sharp;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.DSharp] = Accidental.Sharp;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.FSharp] = Accidental.Sharp;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.GSharp] = Accidental.Sharp;

        map = sharpkeys[G];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.ASharp] = Accidental.Flat;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.CSharp] = Accidental.Sharp;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.DSharp] = Accidental.Sharp;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.Natural;
        map[ NoteScale.FSharp] = Accidental.None;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.GSharp] = Accidental.Sharp;

        map = sharpkeys[D];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.ASharp] = Accidental.Flat;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.Natural;
        map[ NoteScale.CSharp] = Accidental.None;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.DSharp] = Accidental.Sharp;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.Natural;
        map[ NoteScale.FSharp] = Accidental.None;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.GSharp] = Accidental.Sharp;

        map = sharpkeys[A];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.ASharp] = Accidental.Flat;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.Natural;
        map[ NoteScale.CSharp] = Accidental.None;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.DSharp] = Accidental.Sharp;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.Natural;
        map[ NoteScale.FSharp] = Accidental.None;
        map[ NoteScale.G ]      = Accidental.Natural;
        map[ NoteScale.GSharp] = Accidental.None;

        map = sharpkeys[E];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.ASharp] = Accidental.Flat;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.Natural;
        map[ NoteScale.CSharp] = Accidental.None;
        map[ NoteScale.D ]      = Accidental.Natural;
        map[ NoteScale.DSharp] = Accidental.None;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.Natural;
        map[ NoteScale.FSharp] = Accidental.None;
        map[ NoteScale.G ]      = Accidental.Natural;
        map[ NoteScale.GSharp] = Accidental.None;

        map = sharpkeys[B];
        map[ NoteScale.A ]      = Accidental.Natural;
        map[ NoteScale.ASharp] = Accidental.None;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.Natural;
        map[ NoteScale.CSharp] = Accidental.None;
        map[ NoteScale.D ]      = Accidental.Natural;
        map[ NoteScale.DSharp] = Accidental.None;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.Natural;
        map[ NoteScale.FSharp] = Accidental.None;
        map[ NoteScale.G ]      = Accidental.Natural;
        map[ NoteScale.GSharp] = Accidental.None;

        /* Flat keys */
        map = flatkeys[C];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.ASharp] = Accidental.Flat;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.CSharp] = Accidental.Sharp;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.DSharp] = Accidental.Sharp;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.FSharp] = Accidental.Sharp;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.GSharp] = Accidental.Sharp;

        map = flatkeys[F];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.BFlat]  = Accidental.None;
        map[ NoteScale.B ]      = Accidental.Natural;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.CSharp] = Accidental.Sharp;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.EFlat]  = Accidental.Flat;
        map[ NoteScale.E ]      = Accidental.None;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.FSharp] = Accidental.Sharp;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.AFlat]  = Accidental.Flat;

        map = flatkeys[Bflat];
        map[ NoteScale.A ]      = Accidental.None;
        map[ NoteScale.BFlat]  = Accidental.None;
        map[ NoteScale.B ]      = Accidental.Natural;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.CSharp] = Accidental.Sharp;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.EFlat]  = Accidental.None;
        map[ NoteScale.E ]      = Accidental.Natural;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.FSharp] = Accidental.Sharp;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.AFlat]  = Accidental.Flat;

        map = flatkeys[Eflat];
        map[ NoteScale.A ]      = Accidental.Natural;
        map[ NoteScale.BFlat]  = Accidental.None;
        map[ NoteScale.B ]      = Accidental.Natural;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.DFlat]  = Accidental.Flat;
        map[ NoteScale.D ]      = Accidental.None;
        map[ NoteScale.EFlat]  = Accidental.None;
        map[ NoteScale.E ]      = Accidental.Natural;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.FSharp] = Accidental.Sharp;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.AFlat]  = Accidental.None;

        map = flatkeys[Aflat];
        map[ NoteScale.A ]      = Accidental.Natural;
        map[ NoteScale.BFlat]  = Accidental.None;
        map[ NoteScale.B ]      = Accidental.Natural;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.DFlat]  = Accidental.None;
        map[ NoteScale.D ]      = Accidental.Natural;
        map[ NoteScale.EFlat]  = Accidental.None;
        map[ NoteScale.E ]      = Accidental.Natural;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.FSharp] = Accidental.Sharp;
        map[ NoteScale.G ]      = Accidental.None;
        map[ NoteScale.AFlat]  = Accidental.None;

        map = flatkeys[Dflat];
        map[ NoteScale.A ]      = Accidental.Natural;
        map[ NoteScale.BFlat]  = Accidental.None;
        map[ NoteScale.B ]      = Accidental.Natural;
        map[ NoteScale.C ]      = Accidental.None;
        map[ NoteScale.DFlat]  = Accidental.None;
        map[ NoteScale.D ]      = Accidental.Natural;
        map[ NoteScale.EFlat]  = Accidental.None;
        map[ NoteScale.E ]      = Accidental.Natural;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.GFlat]  = Accidental.None;
        map[ NoteScale.G ]      = Accidental.Natural;
        map[ NoteScale.AFlat]  = Accidental.None;

        map = flatkeys[Gflat];
        map[ NoteScale.A ]      = Accidental.Natural;
        map[ NoteScale.BFlat]  = Accidental.None;
        map[ NoteScale.B ]      = Accidental.None;
        map[ NoteScale.C ]      = Accidental.Natural;
        map[ NoteScale.DFlat]  = Accidental.None;
        map[ NoteScale.D ]      = Accidental.Natural;
        map[ NoteScale.EFlat]  = Accidental.None;
        map[ NoteScale.E ]      = Accidental.Natural;
        map[ NoteScale.F ]      = Accidental.None;
        map[ NoteScale.GFlat]  = Accidental.None;
        map[ NoteScale.G ]      = Accidental.Natural;
        map[ NoteScale.AFlat]  = Accidental.None;


    }

    /** The keymap tells what accidental symbol is needed for each
     *  note in the scale.  Reset the keymap to the values of the
     *  key signature.
     */
    private void ResetKeyMap()
    {
        Accidental[] key;
        if (num_flats > 0)
            key = flatkeys[num_flats];
        else
            key = sharpkeys[num_sharps];

        for (int notenumber = 0; notenumber < keymap.length; notenumber++) {
            keymap[notenumber] = key[NoteScale.fromMidiNumber(notenumber)];
        }
    }


    /** Create the Accidental symbols for this key, for
     * the treble and bass clefs.
     */
    private void CreateSymbols() {
        int count = Math.max(num_sharps, num_flats);
        treble = new AccidentalSymbol[count];
        bass = new AccidentalSymbol[count];

        if (count == 0) {
            return;
        }

        WhiteNote[] treblenotes = null;
        WhiteNote[] bassnotes = null;

        if (num_sharps > 0)  {
            treblenotes = new WhiteNote[] {
                new WhiteNote(WhiteNote.F, 5),
                new WhiteNote(WhiteNote.C, 5),
                new WhiteNote(WhiteNote.G, 5),
                new WhiteNote(WhiteNote.D, 5),
                new WhiteNote(WhiteNote.A, 6),
                new WhiteNote(WhiteNote.E, 5)
            };
            bassnotes = new WhiteNote[] {
                new WhiteNote(WhiteNote.F, 3),
                new WhiteNote(WhiteNote.C, 3),
                new WhiteNote(WhiteNote.G, 3),
                new WhiteNote(WhiteNote.D, 3),
                new WhiteNote(WhiteNote.A, 4),
                new WhiteNote(WhiteNote.E, 3)
            };
        }
        else if (num_flats > 0) {
            treblenotes = new WhiteNote[] {
                new WhiteNote(WhiteNote.B, 5),
                new WhiteNote(WhiteNote.E, 5),
                new WhiteNote(WhiteNote.A, 5),
                new WhiteNote(WhiteNote.D, 5),
                new WhiteNote(WhiteNote.G, 4),
                new WhiteNote(WhiteNote.C, 5)
            };
            bassnotes = new WhiteNote[] {
                new WhiteNote(WhiteNote.B, 3),
                new WhiteNote(WhiteNote.E, 3),
                new WhiteNote(WhiteNote.A, 3),
                new WhiteNote(WhiteNote.D, 3),
                new WhiteNote(WhiteNote.G, 2),
                new WhiteNote(WhiteNote.C, 3)
            };
        }

        Accidental a = Accidental.None;
        if (num_sharps > 0)
            a = Accidental.Sharp;
        else
            a = Accidental.Flat;

        for (int i = 0; i < count; i++) {
            treble[i] = new AccidentalSymbol(a, treblenotes[i], Clef.Treble);
            bass[i] = new AccidentalSymbol(a, bassnotes[i], Clef.Bass);
        }
    }

    /** Return the Accidental symbols for displaying this key signature
     * for the given clef.
     */
    public AccidentalSymbol[] GetSymbols(Clef clef) {
        if (clef == Clef.Treble)
            return treble;
        else
            return bass;
    }

    /** Given a midi note number, return the accidental (if any) 
     * that should be used when displaying the note in this key signature.
     *
     * The current measure is also required.  Once we return an
     * accidental for a measure, the accidental remains for the
     * rest of the measure. So we must update the current keymap
     * with any new accidentals that we return.  When we move to another
     * measure, we reset the keymap back to the key signature.
     */
    public Accidental GetAccidental(int notenumber, int measure) {
        if (measure != prevmeasure) {
            ResetKeyMap();
            prevmeasure = measure;
        }
        if (notenumber <= 1 || notenumber >= 127) {
            return Accidental.None;
        } 

        Accidental result = keymap[notenumber];
        if (result == Accidental.Sharp) {
            keymap[notenumber] = Accidental.None;
            keymap[notenumber-1] = Accidental.Natural;
        }
        else if (result == Accidental.Flat) {
            keymap[notenumber] = Accidental.None;
            keymap[notenumber+1] = Accidental.Natural;
        }
        else if (result == Accidental.Natural) {
            keymap[notenumber] = Accidental.None;
            int nextkey = NoteScale.fromMidiNumber(notenumber + 1);
            int prevkey = NoteScale.fromMidiNumber(notenumber - 1);

            /* If we insert a natural, then either:
             * - the next key must go back to sharp,
             * - the previous key must go back to flat.
             */
            if (keymap[notenumber-1] == Accidental.None && keymap[notenumber+1] == Accidental.None &&
                NoteScale.IsBlackKey(nextkey) && NoteScale.IsBlackKey(prevkey) ) {

                if (num_flats == 0) {
                    keymap[notenumber+1] = Accidental.Sharp;
                }
                else {
                    keymap[notenumber-1] = Accidental.Flat;
                }
            }
            else if (keymap[notenumber-1] == Accidental.None && NoteScale.IsBlackKey(prevkey)) {
                keymap[notenumber-1] = Accidental.Flat;
            }
            else if (keymap[notenumber+1] == Accidental.None && NoteScale.IsBlackKey(nextkey)) {
                keymap[notenumber+1] = Accidental.Sharp;
            }
            else {
                /* Shouldn't get here */
            }
        }
        return result;
    }


    /** Given a midi note number, return the white note (the
     * non-sharp/non-flat note) that should be used when displaying
     * this note in this key signature.  This should be called
     * before calling GetAccidental().
     */
    public WhiteNote GetWhiteNote(int notenumber) {
        int notescale = NoteScale.fromMidiNumber(notenumber);
        int octave = (notenumber + 3) / 12 - 1;
        int letter = 0;

        int[] whole_sharps = { 
            WhiteNote.A, WhiteNote.A, 
            WhiteNote.B, 
            WhiteNote.C, WhiteNote.C,
            WhiteNote.D, WhiteNote.D,
            WhiteNote.E,
            WhiteNote.F, WhiteNote.F,
            WhiteNote.G, WhiteNote.G
        };

        int[] whole_flats = {
            WhiteNote.A, 
            WhiteNote.B, WhiteNote.B,
            WhiteNote.C,
            WhiteNote.D, WhiteNote.D,
            WhiteNote.E, WhiteNote.E,
            WhiteNote.F,
            WhiteNote.G, WhiteNote.G,
            WhiteNote.A
        };

        Accidental accidental = keymap[notenumber];
        if (accidental == Accidental.Flat) {
            letter = whole_flats[notescale];
        }
        else if (accidental == Accidental.Sharp) {
            letter = whole_sharps[notescale];
        }
        else if (accidental == Accidental.Natural) {
            letter = whole_sharps[notescale];
        }
        else if (accidental == Accidental.None) {
            letter = whole_sharps[notescale];

            /* If the note number is a sharp/flat, and there's no accidental,
             * determine the white note by seeing whether the previous or next note
             * is a natural.
             */
            if (NoteScale.IsBlackKey(notescale)) {
                if (keymap[notenumber-1] == Accidental.Natural &&
                    keymap[notenumber+1] == Accidental.Natural) {

                    if (num_flats > 0) {
                        letter = whole_flats[notescale];
                    }
                    else {
                        letter = whole_sharps[notescale];
                    }
                }
                else if (keymap[notenumber-1] == Accidental.Natural) {
                    letter = whole_sharps[notescale];
                }
                else if (keymap[notenumber+1] == Accidental.Natural) {
                    letter = whole_flats[notescale];
                }
            }
        }

        /* The above algorithm doesn't quite work for G-flat major.
         * Handle it here.
         */
        if (num_flats == Gflat && notescale == NoteScale.B) {
            letter = WhiteNote.C;
        }
        if (num_flats == Gflat && notescale == NoteScale.BFlat) {
            letter = WhiteNote.B;
        }

        if (num_flats > 0 && notescale == NoteScale.AFlat) {
            octave++;
        }

        return new WhiteNote(letter, octave);
    }


    /** Guess the key signature, given the midi note numbers used in
     * the song.
     */
    public static KeySignature Guess(List<Integer> notes) {
        CreateAccidentalMaps();

        /* Get the frequency count of each note in the 12-note scale */
        int[] noteCount = new int[12];
        for (int i = 0; i < notes.size(); i++) {
            int noteNumber = notes.get(i);
            int noteScale = (noteNumber + 3) % 12;
            noteCount[noteScale] += 1;
        }

        /* For each key signature, count the total number of accidentals
         * needed to display all the notes.  Choose the key signature
         * with the fewest accidentals.
         */
        int bestKey = 0;
        boolean is_best_sharp = true;
        int smallestAccidCount = notes.size();
        int key;

        for (key = 0; key < 6; key++) {
            int accidentalCount = 0;
            for (int n = 0; n < 12; n++) {
                if (sharpkeys[key][n] != Accidental.None) {
                    accidentalCount += noteCount[n];
                }
            }
            if (accidentalCount < smallestAccidCount) {
                smallestAccidCount = accidentalCount;
                bestKey = key;
                is_best_sharp = true;
            }
        }

        for (key = 0; key < 7; key++) {
            int accidentalCount = 0;
            for (int n = 0; n < 12; n++) {
                if (flatkeys[key][n] != Accidental.None) {
                    accidentalCount += noteCount[n];
                }
            }
            if (accidentalCount < smallestAccidCount) {
                smallestAccidCount = accidentalCount;
                bestKey = key;
                is_best_sharp = false;
            }
        }
        if (is_best_sharp) {
            return new KeySignature(bestKey, 0);
        }
        else {
            return new KeySignature(0, bestKey);
        }
    }

    /** Return true if this key signature is equal to key signature k */
    public boolean equals(KeySignature k) {
        if (k.num_sharps == num_sharps && k.num_flats == num_flats)
            return true;
        else
            return false;
    }

    /* Return the Major Key of this Key Signature */
    public int getNoteScale() {
        int[] flatMajor = {
            NoteScale.C, NoteScale.F, NoteScale.BFlat, NoteScale.EFlat,
            NoteScale.AFlat, NoteScale.DFlat, NoteScale.GFlat, NoteScale.B
        };

        int[] sharpMajor = {
            NoteScale.C, NoteScale.G, NoteScale.D, NoteScale.A, NoteScale.E,
            NoteScale.B, NoteScale.FSharp, NoteScale.CSharp, NoteScale.GSharp,
            NoteScale.DSharp
        };
        if (num_flats > 0)
            return flatMajor[num_flats];
        else 
            return sharpMajor[num_sharps];
    }

    /* Convert a Major Key into a String */
    public static String KeyToString(int noteScale) {
        switch (noteScale) {
            case NoteScale.A:     return "A major" ;
            case NoteScale.BFlat: return "B-flat major";
            case NoteScale.B:     return "B major";
            case NoteScale.C:     return "C major";
            case NoteScale.DFlat: return "D-flat major";
            case NoteScale.D:     return "D major";
            case NoteScale.EFlat: return "E-flat major";
            case NoteScale.E:     return "E major";
            case NoteScale.F:     return "F major";
            case NoteScale.GFlat: return "G-flat major";
            case NoteScale.G:     return "G major";
            case NoteScale.AFlat: return "A-flat major";
            default:              return "";
        }
    }

    /* Return a string representation of this key signature.
     * We only return the major key signature, not the minor one.
     */
    @Override
    public String toString() {
        return KeyToString( getNoteScale() );
    }


}

