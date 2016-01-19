/*
 * Copyright (c) 2007-2012 Madhav Vaidyanathan
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
import java.io.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.Log;
import android.view.*;

import com.midisheetmusic.enums.Clef;
import com.midisheetmusic.enums.NoteDuration;

/** @class BoxedInt **/
class BoxedInt {
    public int value;
}

/** @class SheetMusic
 *
 * The SheetMusic Control is the main class for displaying the sheet music.
 * The SheetMusic class has the following public methods:
 *
 * SheetMusic()
 *   Create a new SheetMusic control from the given midi file and options.
 *
 * onDraw()
 *   Method called to draw the SheetMuisc
 *
 * shadeNotes()
 *   Shade all the notes played at a given pulse time.
 */
public class SheetMusic extends SurfaceView implements SurfaceHolder.Callback, StaffInterface {

    /* Measurements used when drawing.  All measurements are in pixels. */
    public static final int LineWidth  = 1;   /** The width of a line */
    public static final int LeftMargin = 4;   /** The left margin */
    public static final int LineSpace  = 7;   /** The space between lines in the staff */
    public static final int StaffHeight = LineSpace * 4 + LineWidth * 5;  /** The height between the 5 horizontal lines of the staff */

    public static final int NoteHeight = LineSpace + LineWidth; /** The height of a whole note */
    public static final int NoteWidth = 3 * LineSpace / 2;        /** The width of a whole note */

    public static final int PageWidth = 800;    /** The width of each page */
    public static final int PageHeight = 1050;  /** The height of each page (when printing) */
    public static final int TitleHeight = 14;   /** Height of title on first page */

    public static final int immediateScroll = 1;
    public static final int gradualScroll = 2;
    public static final int dontScroll = 3;

    private ArrayList<Staff> staffs;  /** The array of staffs to display (from top to bottom) */
    private KeySignature mainKey;     /** The main key signature */

    private String   filename;        /** The midi filename */
    private int      numOfTracks;       /** The number of tracks */
    private boolean scrollVertical;      /** Whether to scroll vertically or horizontally */
    private int      showNoteLetters; /** Display the note letters */
    private int[]    NoteColors;      /** The note colors to use */
    private int      shade1;          /** The color for shading */
    private int      shade2;          /** The color for shading left-hand piano */
    private Paint    paint;           /** The paint for drawing */
    private boolean  surfaceReady;    /** True if we can draw on the surface */
    private Bitmap   bufferBitmap;    /** The bitmap for drawing */
    private Canvas   bufferCanvas;    /** The canvas for drawing */
    private MidiPlayer player;        /** For pausing the music */
    private int      playerHeight;    /** Height of the midi player */
    private int      screenWidth;     /** The screen width */
    private int      screenHeight;    /** The screen height */

    /* fields used for scrolling */

    private int sheetWidth;      /** The sheet music width (excluding zoom) */
    private int sheetHeight;     /** The sheet music height (excluding zoom) */
    private int viewWidth;       /** The width of this view. */
    private int viewHeight;      /** The height of this view. */
    private int bufferX;         /** The (left,top) of the bufferCanvas */
    private int bufferY;
    private int scrollX;         /** The (left,top) of the scroll clip */
    private int scrollY;

    private Staff[][] nowStaffs;
    private int staffHeight; /** staff maximum height */
    private Rect clip;

    public SheetMusic(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        bufferX = bufferY = scrollX = scrollY = 0;

        Activity activity = (Activity)context;
        screenWidth = activity.getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        playerHeight = MidiPlayer.getPreferredSize(screenWidth, screenHeight).y;

        nowStaffs = new Staff[2][];
        nowStaffs[0] = new Staff[2];
        nowStaffs[1] = new Staff[2];
    }

    /** Create a new SheetMusic View.
     * MidiFile is the parsed midi file to display.
     * SheetMusic Options are the menu options that were selected.
     * SheetMusic View를 만든다
     * MidiFile클래스는 미디파일을 파싱하여 보여준다.
     * SheetMusic Options는 메뉴 옵션에서 선택되어진다.
     *
     * - Apply all the Menu Options to the MidiFile tracks.
     * - Calculate the key signature
     * 조표가 연산되어진다.
     * - For each track, create a list of MusicSymbols (notes, rests, bars, etc)
     * 각각의 트랙에서 뮤직 심벌의 리스트(노트, 쉼표, 바, 등등)가 만들어진다.
     * - Vertically align the music symbols in all the tracks
     * 세로로 정렬되어진다. 모든 트랙의 music symbol들에서
     * - Partition the music notes into horizontal staffs
     * 노트를 구분한다 세로 오선지 안에서.
     */
    public void init(MidiFile file, MidiOptions options) {
        if (options == null) {
            options = new MidiOptions(file);
        }

        filename = file.getFileName();
        SetColors(null, options.shade1Color, options.shade2Color);
        paint = new Paint();
        paint.setTextSize(12.0f);
        Typeface typeface = Typeface.create(paint.getTypeface(), Typeface.NORMAL);
        paint.setTypeface(typeface);
        paint.setColor(Color.BLACK);

        ArrayList<MidiTrack> tracks = file.ChangeMidiNotes(options);
        scrollVertical = true;
        showNoteLetters = options.showNoteLetters;
        TimeSignature time = file.getTime();
        if (options.time != null) {
            time = options.time;
        }
        if (options.key == -1) {
            mainKey = GetKeySignature(tracks);
        }
        else {
            mainKey = new KeySignature(options.key);
        }
        numOfTracks = tracks.size();

        int lastStart = file.EndTime() + options.shiftTime;

        /* Create all the music symbols (notes, rests, vertical bars, and
         * clef changes).  The symbols variable contains a list of music
         * symbols for each track.  The list does not include the left-side
         * ClefSymbol and key signature symbols.  Those can only be calculated
         * when we create the staffs.
         */
        ArrayList<ArrayList<MusicSymbol>> allSymbols =
          new ArrayList<ArrayList<MusicSymbol> >(numOfTracks);

        for (int i = 0; i < numOfTracks; i++) {
            MidiTrack track = tracks.get(i);
            ClefMeasures clefs = new ClefMeasures(track.getNotes(), time.getMeasure());
            ArrayList<ChordSymbol> chords = CreateChords(track.getNotes(), mainKey, time, clefs);
            allSymbols.add(CreateSymbols(chords, clefs, time, lastStart));
        }

        ArrayList<ArrayList<LyricSymbol>> lyrics = null;
        if (options.showLyrics) {
            lyrics = GetLyrics(tracks);
        }

        /* Vertically align the music symbols */
        SymbolWidths widths = new SymbolWidths(allSymbols, lyrics);
        AlignSymbols(allSymbols, widths, options);

        staffs = CreateStaffs(allSymbols, mainKey, options, time.getMeasure());
        CreateAllBeamedChords(allSymbols, time);
        if (lyrics != null) {
            //AddLyricsToStaffs(staffs, lyricsList);
        }

        /* After making chord pairs, the stem directions can change,
         * which affects the staff height.  Re-calculate the staff height.
         */
        staffHeight = 0;
        for (Staff staff : staffs) {
            staff.CalculateHeight();
            if(staff.getHeight() > staffHeight) {
                staffHeight = staff.getHeight();
            }
        }
        for(Staff staff : staffs) {
            staff.setHeight(staffHeight);
        }
        for(int i=0; i<2; i++) {
            for(int j=0; j<2; j++) {
                nowStaffs[i][j] = staffs.get(i * 2 + j);
            }
        }

        //scrollAnimation = new ScrollAnimation(this, scrollVertical);
    }

    /** Calculate the size of the sheet music width and height
     *  (without zoom scaling to fit the screen).  Store the result in
     *  sheetWidth and sheetHeight.
     *  악보의 사이즈(너비와 높이, 줌 스케일링 없이 화면에 맞춰서)를 연산한다.
     *  sheetwidth와 sheetheight에 저장된다.
     */
    private void calculateSize() {
        sheetWidth = 0;
        sheetHeight = 0;
        for (Staff staff : staffs) {
            sheetWidth = Math.max(sheetWidth, staff.getWidth());
            sheetHeight += (staff.getHeight());
        }
        sheetWidth += 2;
        sheetHeight += LeftMargin;
    }

    /* Adjust the zoom level so that the sheet music page (PageWidth)
     * fits within the width. If the heightspec is 0, return the screenHeight.
     * Else, use the given view width/height.
     */
    @Override
    protected void onMeasure(int widthspec, int heightspec) {
        // First, calculate the zoom level
        int specwidth = MeasureSpec.getSize(widthspec);
        int specheight = MeasureSpec.getSize(heightspec);

        if (specwidth == 0 && specheight == 0) {
            setMeasuredDimension(screenWidth, screenHeight);
        }
        else if (specwidth == 0) {
            setMeasuredDimension(screenWidth, specheight);
        }
        else if (specheight == 0) {
            setMeasuredDimension(specwidth, screenHeight);
        }
        else {
            setMeasuredDimension(specwidth, specheight);
        }
    }


    /** If this is the first size change, calculate the zoom level,
     *  and create the bufferCanvas.  Otherwise, do nothing.
     */
    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWith, int oldHeight) {
        viewWidth = newWidth;
        viewHeight = newHeight;

        if (bufferCanvas != null) {
            callOnDraw();
            return;
        }

        calculateSize();
        /*if (scrollVertical) {
            zoom = (float)((newWidth - 2) * 1.0 / PageWidth);
        }
        else {
            zoom = (float)( (newHeight + playerHeight) * 1.0 / sheetHeight);
            if (zoom < 0.9)
                zoom = 0.9f;
            if (zoom > 1.1)
                zoom = 1.1f;
        }*/
        if (bufferCanvas == null) {
            createBufferCanvas();
        }
        callOnDraw();
    }


    /** Get the best key signature given the midi notes in all the tracks. */
    private KeySignature GetKeySignature(ArrayList<MidiTrack> tracks) {
        List<Integer> noteNums = new ArrayList<>();
        for (MidiTrack track : tracks) {
            for (MidiNote note : track.getNotes()) {
                noteNums.add(note.getNumber());
            }
        }
        return KeySignature.Guess(noteNums);
    }


    /** Create the chord symbols for a single track.
     * @param midinotes  The Midinotes in the track.
     * @param key        The Key Signature, for determining sharps/flats.
     * @param time       The Time Signature, for determining the measures.
     * @param clefs      The clefs to use for each measure.
     * @ret An array of ChordSymbols
     */
    private ArrayList<ChordSymbol> CreateChords(ArrayList<MidiNote> midinotes,
                                   KeySignature key,
                                   TimeSignature time,
                                   ClefMeasures clefs) {

        int i = 0;
        ArrayList<ChordSymbol> chords = new ArrayList<ChordSymbol>();
        ArrayList<MidiNote> notegroup = new ArrayList<MidiNote>(12);
        int len = midinotes.size();

        while (i < len) {

            int starttime = midinotes.get(i).getStartTime();
            Clef clef = clefs.GetClef(starttime);

            /* Group all the midi notes with the same start time
             * into the notes list.
             */
            notegroup.clear();
            notegroup.add(midinotes.get(i));
            i++;
            while (i < len && midinotes.get(i).getStartTime() == starttime) {
                notegroup.add(midinotes.get(i));
                i++;
            }

            /* Create a single chord from the group of midi notes with
             * the same start time.
             */
            ChordSymbol chord = new ChordSymbol(notegroup, key, time, clef, this);
            chords.add(chord);
        }

        return chords;
    }

    /** Given the chord symbols for a track, create a new symbol list
     * that contains the chord symbols, vertical bars, rests, and
     * clef changes.
     * Return a list of symbols (ChordSymbol, BarSymbol, RestSymbol, ClefSymbol)
     */
    private ArrayList<MusicSymbol> CreateSymbols(ArrayList<ChordSymbol> chords, ClefMeasures clefs,
                  TimeSignature time, int lastStart) {

        ArrayList<MusicSymbol> symbols = new ArrayList<MusicSymbol>();
        symbols = AddBars(chords, time, lastStart);
        symbols = AddRests(symbols, time);
        symbols = AddClefChanges(symbols, clefs);

        return symbols;
    }

    /** Add in the vertical bars delimiting measures.
     *  Also, add the time signature symbols.
     */
    private ArrayList<MusicSymbol> AddBars(ArrayList<ChordSymbol> chords, TimeSignature time, int lastStart) {
        ArrayList<MusicSymbol> symbols = new ArrayList<MusicSymbol>();

        TimeSignatureSymbol timesig = new TimeSignatureSymbol(time.getNumerator(), time.getDenominator());
        symbols.add(timesig);

        /* The starttime of the beginning of the measure */
        int measuretime = 0;

        int i = 0;
        while (i < chords.size()) {
            if (measuretime <= chords.get(i).getStartTime()) {
                symbols.add(new BarSymbol(measuretime) );
                measuretime += time.getMeasure();
            }
            else {
                symbols.add(chords.get(i));
                i++;
            }
        }

        /* Keep adding bars until the last startTime (the end of the song) */
        while (measuretime < lastStart) {
            symbols.add(new BarSymbol(measuretime) );
            measuretime += time.getMeasure();
        }

        /* Add the final vertical bar to the last measure */
        symbols.add(new BarSymbol(measuretime) );
        return symbols;
    }

    /** Add rest symbols between notes.  All times below are
     * measured in pulses.
     */
    private ArrayList<MusicSymbol> AddRests(ArrayList<MusicSymbol> symbols, TimeSignature time) {
        int prevtime = 0;

        ArrayList<MusicSymbol> result = new ArrayList<MusicSymbol>( symbols.size() );

        for (MusicSymbol symbol : symbols) {
            int starttime = symbol.getStartTime();
            RestSymbol[] rests = GetRests(time, prevtime, starttime);
            if (rests != null) {
                for (RestSymbol r : rests) {
                    result.add(r);
                }
            }

            result.add(symbol);

            /* Set prevtime to the end time of the last note/symbol. */
            if (symbol instanceof ChordSymbol) {
                ChordSymbol chord = (ChordSymbol)symbol;
                prevtime = Math.max( chord.getEndTime(), prevtime );
            }
            else {
                prevtime = Math.max(starttime, prevtime);
            }
        }
        return result;
    }

    /** Return the rest symbols needed to fill the time interval between
     * start and end.  If no rests are needed, return nil.
     */
    private RestSymbol[] GetRests(TimeSignature time, int start, int end) {
        RestSymbol[] result;
        RestSymbol r1, r2;

        if (end - start < 0)
            return null;

        NoteDuration dur = time.GetNoteDuration(end - start);
        switch (dur) {
            case Whole:
            case Half:
            case Quarter:
            case Eighth:
                r1 = new RestSymbol(start, dur);
                result = new RestSymbol[]{ r1 };
                return result;

            case DottedHalf:
                r1 = new RestSymbol(start, NoteDuration.Half);
                r2 = new RestSymbol(start + time.getQuarter()*2,
                                    NoteDuration.Quarter);
                result = new RestSymbol[]{ r1, r2 };
                return result;

            case DottedQuarter:
                r1 = new RestSymbol(start, NoteDuration.Quarter);
                r2 = new RestSymbol(start + time.getQuarter(),
                                    NoteDuration.Eighth);
                result = new RestSymbol[]{ r1, r2 };
                return result;

            case DottedEighth:
                r1 = new RestSymbol(start, NoteDuration.Eighth);
                r2 = new RestSymbol(start + time.getQuarter()/2,
                                    NoteDuration.Sixteenth);
                result = new RestSymbol[]{ r1, r2 };
                return result;

            default:
                return null;
        }
    }

    /** The current clef is always shown at the beginning of the staff, on
     * the left side.  However, the clef can also change from measure to
     * measure. When it does, a ClefSymbol symbol must be shown to indicate the
     * change in clef.  This function adds these ClefSymbol change symbols.
     * This function does not add the main ClefSymbol Symbol that begins each
     * staff.  That is done in the StaffSymbol() contructor.
     */
    private ArrayList<MusicSymbol> AddClefChanges(ArrayList<MusicSymbol> symbols,
                                     ClefMeasures clefs) {

        ArrayList<MusicSymbol> result = new ArrayList<MusicSymbol>( symbols.size() );
        Clef prevClef = clefs.GetClef(0);
        for (MusicSymbol symbol : symbols) {
            /* A BarSymbol indicates a new measure */
            if (symbol instanceof BarSymbol) {
                Clef clef = clefs.GetClef(symbol.getStartTime());
                if (clef != prevClef) {
                    result.add(new ClefSymbol(clef, symbol.getStartTime()-1, true));
                }
                prevClef = clef;
            }
            result.add(symbol);
        }
        return result;
    }


    /** Notes with the same start times in different staffs should be
     * vertically aligned.  The SymbolWidths class is used to help
     * vertically align symbols.
     *
     * First, each track should have a symbol for every starttime that
     * appears in the Midi File.  If a track doesn't have a symbol for a
     * particular starttime, then add a "blank" symbol for that time.
     *
     * Next, make sure the symbols for each start time all have the same
     * width, across all tracks.  The SymbolWidths class stores
     * - The symbol width for each starttime, for each track
     * - The maximum symbol width for a given starttime, across all tracks.
     *
     * The method SymbolWidths.GetExtraWidth() returns the extra width
     * needed for a track to match the maximum symbol width for a given
     * starttime.
     */
    private void AlignSymbols(ArrayList<ArrayList<MusicSymbol>> allSymbols, SymbolWidths widths, MidiOptions options) {

        // If we show measure numbers, increase bar symbol width
        if (options.showMeasures) {
            for (int track = 0; track < allSymbols.size(); track++) {
                ArrayList<MusicSymbol> symbols = allSymbols.get(track);
                for (MusicSymbol sym : symbols) {
                    if (sym instanceof BarSymbol) {
                        sym.setWidth( sym.getWidth() + NoteWidth);
                    }
                }
            }
        }

        for (int track = 0; track < allSymbols.size(); track++) {
            ArrayList<MusicSymbol> symbols = allSymbols.get(track);
            ArrayList<MusicSymbol> result = new ArrayList<MusicSymbol>();

            int i = 0;

            /* If a track doesn't have a symbol for a starttime,
             * add a blank symbol.
             */
            for (int start : widths.getStartTimes()) {

                /* BarSymbols are not included in the SymbolWidths calculations */
                while (i < symbols.size() && (symbols.get(i) instanceof BarSymbol) &&
                    symbols.get(i).getStartTime() <= start) {
                    result.add(symbols.get(i));
                    i++;
                }

                if (i < symbols.size() && symbols.get(i).getStartTime() == start) {

                    while (i < symbols.size() &&
                           symbols.get(i).getStartTime() == start) {

                        result.add(symbols.get(i));
                        i++;
                    }
                }
                else {
                    result.add(new BlankSymbol(start, 0));
                }
            }

            /* For each starttime, increase the symbol width by
             * SymbolWidths.GetExtraWidth().
             */
            i = 0;
            while (i < result.size()) {
                if (result.get(i) instanceof BarSymbol) {
                    i++;
                    continue;
                }
                int start = result.get(i).getStartTime();
                int extra = widths.GetExtraWidth(track, start);
                int newwidth = result.get(i).getWidth() + extra;
                result.get(i).setWidth(newwidth);

                /* Skip all remaining symbols with the same starttime. */
                while (i < result.size() && result.get(i).getStartTime() == start) {
                    i++;
                }
            }
            allSymbols.set(track, result);
        }
    }


    /** Find 2, 3, 4, or 6 chord symbols that occur consecutively (without any
     *  rests or bars in between).  There can be BlankSymbols in between.
     *
     *  The startIndex is the index in the symbols to start looking from.
     *
     *  Store the indexes of the consecutive chords in chordIndexes.
     *  Store the horizontal distance (pixels) between the first and last chord.
     *  If we failed to find consecutive chords, return false.
     */
    private static boolean FindConsecutiveChords(ArrayList<MusicSymbol> symbols, TimeSignature time,
                          int startIndex, int[] chordIndexes,
                          BoxedInt horizDistance) {

        int i = startIndex;
        int numChords = chordIndexes.length;

        while (true) {
            horizDistance.value = 0;

            /* Find the starting chord */
            while (i < symbols.size() - numChords) {
                if (symbols.get(i) instanceof ChordSymbol) {
                    ChordSymbol c = (ChordSymbol) symbols.get(i);
                    if (c.getStem() != null) {
                        break;
                    }
                }
                i++;
            }
            if (i >= symbols.size() - numChords) {
                chordIndexes[0] = -1;
                return false;
            }
            chordIndexes[0] = i;
            boolean foundChords = true;
            for (int chordIndex = 1; chordIndex < numChords; chordIndex++) {
                i++;
                int remaining = numChords - 1 - chordIndex;
                while ((i < symbols.size() - remaining) &&
                       (symbols.get(i) instanceof BlankSymbol)) {

                    horizDistance.value += symbols.get(i).getWidth();
                    i++;
                }
                if (i >= symbols.size() - remaining) {
                    return false;
                }
                if (!(symbols.get(i) instanceof ChordSymbol)) {
                    foundChords = false;
                    break;
                }
                chordIndexes[chordIndex] = i;
                horizDistance.value += symbols.get(i).getWidth();
            }
            if (foundChords) {
                return true;
            }

            /* Else, start searching again from index i */
        }
    }


    /** Connect chords of the same duration with a horizontal beam.
     *  numChords is the number of chords per beam (2, 3, 4, or 6).
     *  if startBeat is true, the first chord must start on a quarter note beat.
     */
    private static void CreateBeamedChords(ArrayList<ArrayList<MusicSymbol>> allsymbols, TimeSignature time,
                       int numChords, boolean startBeat) {
        int[] chordIndexes = new int[numChords];
        ChordSymbol[] chords = new ChordSymbol[numChords];

        for (ArrayList<MusicSymbol> symbols : allsymbols) {
            int startIndex = 0;
            while (true) {
                BoxedInt horizDistance = new BoxedInt();
                horizDistance.value = 0;
                boolean found = FindConsecutiveChords(symbols, time,
                                                   startIndex,
                                                   chordIndexes,
                                                   horizDistance);
                if (!found) {
                    break;
                }
                for (int i = 0; i < numChords; i++) {
                    chords[i] = (ChordSymbol)symbols.get( chordIndexes[i] );
                }

                if (ChordSymbol.CanCreateBeam(chords, time, startBeat)) {
                    ChordSymbol.CreateBeam(chords, horizDistance.value);
                    startIndex = chordIndexes[numChords-1] + 1;
                }
                else {
                    startIndex = chordIndexes[0] + 1;
                }

                /* What is the value of startIndex here?
                 * If we created a beam, we start after the last chord.
                 * If we failed to create a beam, we start after the first chord.
                 */
            }
        }
    }


    /** Connect chords of the same duration with a horizontal beam.
     *
     *  We create beams in the following order:
     *  - 6 connected 8th note chords, in 3/4, 6/8, or 6/4 time
     *  - Triplets that start on quarter note beats
     *  - 3 connected chords that start on quarter note beats (12/8 time only)
     *  - 4 connected chords that start on quarter note beats (4/4 or 2/4 time only)
     *  - 2 connected chords that start on quarter note beats
     *  - 2 connected chords that start on any beat
     */
    private static void CreateAllBeamedChords(ArrayList<ArrayList<MusicSymbol>> allSymbols, TimeSignature time) {
        if ((time.getNumerator() == 3 && time.getDenominator() == 4) ||
            (time.getNumerator() == 6 && time.getDenominator() == 8) ||
            (time.getNumerator() == 6 && time.getDenominator() == 4) ) {

            CreateBeamedChords(allSymbols, time, 6, true);
        }
        CreateBeamedChords(allSymbols, time, 3, true);
        CreateBeamedChords(allSymbols, time, 4, true);
        CreateBeamedChords(allSymbols, time, 2, true);
        CreateBeamedChords(allSymbols, time, 2, false);
    }


    /** Get the width (in pixels) needed to display the key signature */
    public static int KeySignatureWidth(KeySignature key) {
        ClefSymbol clefSym = new ClefSymbol(Clef.Treble, 0, false);
        int result = clefSym.getMinWidth();
        AccidentalSymbol[] keys = key.GetSymbols(Clef.Treble);
        for (AccidentalSymbol symbol : keys) {
            result += symbol.getMinWidth();
        }
        return result + SheetMusic.LeftMargin + 5;
    }


    /** Given MusicSymbols for a track, create the staffs for that track.
     *  Each StaffSymbol has a maxmimum width of PageWidth (800 pixels).
     *  Also, measures should not span multiple Staffs.
     */
    private ArrayList<Staff> CreateStaffsForTrack(ArrayList<MusicSymbol> symbols, int measureLength,
                         KeySignature key, MidiOptions options,
                         int track, int totalTracks) {
        int keySignatureWidth = KeySignatureWidth(key);
        int startIndex = 0;
        ArrayList<Staff> staffs = new ArrayList<>();

        while (startIndex < symbols.size()) {
            /* startindex is the index of the first symbol in the staff.
             * endindex is the index of the last symbol in the staff.
             */
            int endIndex = startIndex;
            int width = keySignatureWidth;
            int maxWidth;

            /* If we're scrolling vertically, the maximum width is PageWidth. */
            if (scrollVertical) {
                //maxWidth = SheetMusic.PageWidth;
                maxWidth = screenWidth;
            }
            else {
                maxWidth = 2000000;
            }

            while (endIndex < symbols.size() &&
                   width + symbols.get(endIndex).getWidth() < maxWidth) {

                width += symbols.get(endIndex).getWidth();
                endIndex++;
            }
            endIndex--;

            /* There's 3 possibilities at this point:
             * 1. We have all the symbols in the track.
             *    The endindex stays the same.
             *
             * 2. We have symbols for less than one measure.
             *    The endindex stays the same.
             *
             * 3. We have symbols for 1 or more measures.
             *    Since measures cannot span multiple staffs, we must
             *    make sure endindex does not occur in the middle of a
             *    measure.  We count backwards until we come to the end
             *    of a measure.
             */

            if (endIndex == symbols.size() - 1) {
                /* endindex stays the same */
            }
            else if (symbols.get(startIndex).getStartTime() / measureLength ==
                     symbols.get(endIndex).getStartTime() / measureLength) {
                /* endindex stays the same */
            }
            else {
                int endMeasure = symbols.get(endIndex+1).getStartTime()/measureLength;
                while (symbols.get(endIndex).getStartTime() / measureLength ==
                       endMeasure) {
                    endIndex--;
                }
            }

            if (scrollVertical) {
                width = SheetMusic.PageWidth;
            }
            // int range = endindex + 1 - startindex;
            ArrayList<MusicSymbol> staffSymbols = new ArrayList<>();
            for (int i = startIndex; i <= endIndex; i++) {
                staffSymbols.add(symbols.get(i));
            }
            Staff staff = new Staff(staffSymbols, key, options, track, totalTracks);
            staffs.add(staff);
            startIndex = endIndex + 1;
        }
        return staffs;
    }


    /** Given all the MusicSymbols for every track, create the staffs
     * for the sheet music.  There are two parts to this:
     *
     * - Get the list of staffs for each track.
     *   The staffs will be stored in trackstaffs as:
     *
     *   trackstaffs[0] = { Staff0, Staff1, Staff2, ... } for track 0
     *   trackstaffs[1] = { Staff0, Staff1, Staff2, ... } for track 1
     *   trackstaffs[2] = { Staff0, Staff1, Staff2, ... } for track 2
     *
     * - Store the Staffs in the staffs list, but interleave the
     *   tracks as follows:
     *
     *   staffs = { Staff0 for track 0, Staff0 for track1, Staff0 for track2,
     *              Staff1 for track 0, Staff1 for track1, Staff1 for track2,
     *              Staff2 for track 0, Staff2 for track1, Staff2 for track2,
     *              ... }
     */
    private ArrayList<Staff> CreateStaffs(ArrayList<ArrayList<MusicSymbol>> allSymbols, KeySignature key,
                 MidiOptions options, int measureLength) {

        ArrayList<ArrayList<Staff>> trackStaffs =
                new ArrayList<>();
        int totalTracks = allSymbols.size();

        for (int track = 0; track < totalTracks; track++) {
            ArrayList<MusicSymbol> symbols = allSymbols.get( track );
            trackStaffs.add(CreateStaffsForTrack(symbols, measureLength, key,
                    options, track, totalTracks));
        }

        /* Update the EndTime of each StaffSymbol. EndTime is used for playback */
        for (ArrayList<Staff> list : trackStaffs) {
            for (int i = 0; i < list.size()-1; i++) {
                list.get(i).setEndTime( list.get(i+1).getStartTime() );
            }
        }

        /* Interleave the staffs of each track into the result array. */
        int maxStaffs = 0;
        for (int i = 0; i < trackStaffs.size(); i++) {
            if (maxStaffs < trackStaffs.get(i).size()) {
                maxStaffs = trackStaffs.get(i).size();
            }
        }
        ArrayList<Staff> result = new ArrayList<>();
        for (int i = 0; i < maxStaffs; i++) {
            for (ArrayList<Staff> list : trackStaffs) {
                if (i < list.size()) {
                    result.add(list.get(i));
                }
            }
        }
        return result;
    }


    /** Change the note colors for the sheet music, and redraw.
     *  This is not currently used.
     */
    public void SetColors(int[] newColors, int newShade1, int newShade2) {
        if (NoteColors == null) {
            NoteColors = new int[12];
            for (int i = 0; i < 12; i++) {
                NoteColors[i] = Color.BLACK;
            }
        }
        if (newColors != null) {
            for (int i = 0; i < 12; i++) {
                NoteColors[i] = newColors[i];
            }
        }
        shade1 = newShade1;
        shade2 = newShade2;
    }

    /** Get the color for a given note number. Not currently used. */
    public int NoteColor(int number) {
        return NoteColors[ NoteScale.fromMidiNumber(number) ];
    }

    /** Get the shade color */
    public int getShade1() { return shade1; }

    /** Get the shade2 color */
    public int getShade2() { return shade2; }

    /** Get whether to show note letters or not */
    public int getShowNoteLetters() { return showNoteLetters; }

    /** Get the main key signature */
    public KeySignature getMainKey() { return mainKey; }

    /** Get the lyricsList for each track */
    private static ArrayList<ArrayList<LyricSymbol>>
    GetLyrics(ArrayList<MidiTrack> tracks) {
       boolean hasLyrics = false;
        ArrayList<ArrayList<LyricSymbol>> result = new ArrayList<ArrayList<LyricSymbol>>();
        for (int i = 0; i < tracks.size(); i++) {
            ArrayList<LyricSymbol> lyrics = new ArrayList<LyricSymbol>();
            result.add(lyrics);
            MidiTrack track = tracks.get(i);
            if (track.getLyrics() == null) {
                continue;
            }
            hasLyrics = true;
            for (MidiEvent ev : track.getLyrics()) {
                try {
                    String text = new String(ev.value, 0, ev.value.length, "UTF-8");
                    LyricSymbol sym = new LyricSymbol(ev.startTime, text);
                    lyrics.add(sym);
                }
                catch (UnsupportedEncodingException e) {}
            }
        }
        if (!hasLyrics) {
            return null;
        }
        else {
            return result;
        }
    }

    /** Add the lyric symbols to the corresponding staffs */
    private static void AddLyricsToStaffs(ArrayList<Staff> staffs, ArrayList<ArrayList<LyricSymbol>> tracklyrics) {
        for (Staff staff : staffs) {
            ArrayList<LyricSymbol> lyrics = tracklyrics.get(staff.getTrack());
            staff.AddLyrics(lyrics);
        }
    }

    /** Create a bitmap/canvas to use for double-buffered drawing.
     *  This is needed for shading the notes quickly.
     *  Instead of redrawing the entire sheet music on every shade call,
     *  we draw the sheet music to this bitmap canvas.  On subsequent
     *  calls to ShadeNotes(), we only need to draw the delta (the
     *  new notes to shade/unshade) onto the bitmap, and then draw the bitmap.
     *
     *  We include the MidiPlayer height (since we hide the MidiPlayer
     *  once the music starts playing). Also, we make the bitmap twice as
     *  large as the scroll viewable area, so that we don't need to
     *  refresh the bufferCanvas on every scroll change.
     */
    private void createBufferCanvas() {
        if (bufferBitmap != null) {
            bufferCanvas = null;
            bufferBitmap.recycle();
            bufferBitmap = null;
        }
        if (scrollVertical) {
            bufferBitmap = Bitmap.createBitmap(viewWidth,
                                               (viewHeight + playerHeight) * 2,
                                               Bitmap.Config.ARGB_8888);
        }
        else {
            bufferBitmap = Bitmap.createBitmap(viewWidth * 2,
                                               (viewHeight + playerHeight) * 2,
                                               Bitmap.Config.ARGB_8888);
        }

        clip = new Rect(0, 0,
                0 + bufferBitmap.getWidth(),
                0 + bufferBitmap.getHeight());

        bufferCanvas = new Canvas(bufferBitmap);
        drawToBuffer(scrollX, scrollY);
    }


    /** Obtain the drawing canvas and call onDraw() */
    public void callOnDraw() {
        if (!surfaceReady) {
            return;
        }
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        onDraw(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    /** Draw the SheetMusic. */
    @Override
    protected void onDraw(Canvas canvas) {
        if (bufferBitmap == null) {
            createBufferCanvas();
        }
        /*if (!isScrollPositionInBuffer()) {
            drawToBuffer(scrollX, scrollY);
        }*/
        drawToBuffer(scrollX, scrollY);
        // We want (scrollX - bufferX, scrollY - bufferY)
        // to be (0,0) on the canvas
        canvas.translate(-(scrollX - bufferX), -(scrollY - bufferY));
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        canvas.translate(scrollX - bufferX, scrollY - bufferY);
    }

    /** Return true if the scrollX/scrollY is in the bufferBitmap */
    private boolean isScrollPositionInBuffer() {
        if ((scrollY < bufferY) ||
            (scrollX < bufferX) ||
            (scrollY > bufferY + bufferBitmap.getHeight()/3) ||
            (scrollX > bufferX + bufferBitmap.getWidth()/3) ) {

            return false;
        }
        else {
            return true;
        }
    }

    /** Draw the SheetMusic to the bufferCanvas, with the
     * given (left,top) corner.
     *
     * Scale the graphics by the current zoom factor.
     * Only draw Staffs which lie inside the buffer area.
     */
    private void drawToBuffer(int left, int top) {
        if (staffs == null) {
            return;
        }

        Log.i("chk", "drawToBuffer");

        bufferX = left;
        bufferY = top;

        // Scale both the canvas and the clip by the zoom factor
        //bufferCanvas.scale(zoom, zoom);

        // Draw a white background
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        bufferCanvas.drawRect(clip.left, clip.top, clip.right, clip.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        // Draw the staffs in the clip area
        // 여기서 계속 악보를 그려준다. 1번부터 끝번까지
        for(int i=0; i<2; i++) {
            for(int j=0; j<2; j++) {
                Staff staff = nowStaffs[i][j];
                bufferCanvas.translate(0, staffHeight * (i*2+j));
                staff.Draw(bufferCanvas, clip, paint);
                bufferCanvas.translate(0, -(staffHeight * (i*2+j)));
            }
        }

        //bufferCanvas.scale(1.0f/zoom, 1.0f/zoom);
        bufferCanvas.translate(bufferX, bufferY);
    }

    /** Write the MIDI filename at the top of the page */
    private void DrawTitle(Canvas canvas) {
        int leftMarginPixel = 20;
        int topMarginPixel = 20;
        String title = filename;
        title = title.replace(".mid", "").replace("_", " ");
        canvas.translate(leftMarginPixel, topMarginPixel);
        canvas.drawText(title, 0, 0, paint);
        canvas.translate(-leftMarginPixel, -topMarginPixel);
    }

    /**
     * Return the number of pages needed to print this sheet music.
     * A staff should fit within a single page, not be split across two pages.
     * If the sheet music has exactly 2 tracks, then two staffs should
     * fit within a single page, and not be split across two pages.
     */
    public int GetTotalPages() {
        int num = 1;
        int currentHeight = TitleHeight;

        if (numOfTracks == 2 && (staffs.size() % 2) == 0) {
            for (int i = 0; i < staffs.size(); i += 2) {
                int heights = staffs.get(i).getHeight() + staffs.get(i+1).getHeight();
                if (currentHeight + heights > PageHeight) {
                    num++;
                    currentHeight = heights;
                }
                else {
                    currentHeight += heights;
                }
            }
        }
        else {
            for (Staff staff : staffs) {
                if (currentHeight + staff.getHeight() > PageHeight) {
                    num++;
                    currentHeight = staff.getHeight();
                }
                else {
                    currentHeight += staff.getHeight();
                }
            }
        }
        return num;
    }

    /** Draw the given page of the sheet music.
     * Page numbers start from 1.
     * A staff should fit within a single page, not be split across two pages.
     * If the sheet music has exactly 2 tracks, then two staffs should
     * fit within a single page, and not be split across two pages.
     */
    public void DrawPage(Canvas canvas, int pageNumber) {
        int leftMarginPixel = 20;
        int topMarginPixel = 20;

        Rect clip = new Rect(0, 0, PageWidth + 40, PageHeight + 40);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawRect(clip.left, clip.top, clip.right, clip.bottom, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        int yPosition = TitleHeight;
        int pageNum = 1;
        int staffNum = 0;

        if (numOfTracks == 2 && (staffs.size() % 2) == 0) {
            /* Skip the staffs until we reach the given page number */
            while (staffNum + 1 < staffs.size() && pageNum < pageNumber) {
                int heights = staffs.get(staffNum).getHeight() +
                              staffs.get(staffNum+1).getHeight();
                if (yPosition + heights >= PageHeight) {
                    pageNum++;
                    yPosition = 0;
                }
                else {
                    yPosition += heights;
                    staffNum += 2;
                }
            }
            /* Print the staffs until the height reaches PageHeight */
            if (pageNum == 1) {
                DrawTitle(canvas);
                yPosition = TitleHeight;
            }
            else {
                yPosition = 0;
            }
            for (; staffNum + 1 < staffs.size(); staffNum += 2) {
                int heights = staffs.get(staffNum).getHeight() +
                              staffs.get(staffNum+1).getHeight();

                if (yPosition + heights >= PageHeight)
                    break;

                canvas.translate(leftMarginPixel, topMarginPixel + yPosition);
                staffs.get(staffNum).Draw(canvas, clip, paint);
                canvas.translate(-leftMarginPixel, -(topMarginPixel + yPosition));
                yPosition += staffs.get(staffNum).getHeight();
                canvas.translate(leftMarginPixel, topMarginPixel + yPosition);
                staffs.get(staffNum + 1).Draw(canvas, clip, paint);
                canvas.translate(-leftMarginPixel, -(topMarginPixel + yPosition));
                yPosition += staffs.get(staffNum + 1).getHeight();
            }
        }

        else {
            /* Skip the staffs until we reach the given page number */
            while (staffNum < staffs.size() && pageNum < pageNumber) {
                if (yPosition + staffs.get(staffNum).getHeight() >= PageHeight) {
                    pageNum++;
                    yPosition = 0;
                }
                else {
                    yPosition += staffs.get(staffNum).getHeight();
                    staffNum++;
                }
            }

            /* Print the staffs until the height reaches viewPageHeight */
            if (pageNum == 1) {
                DrawTitle(canvas);
                yPosition = TitleHeight;
            }
            else {
                yPosition = 0;
            }
            for (; staffNum < staffs.size(); staffNum++) {
                if (yPosition + staffs.get(staffNum).getHeight() >= PageHeight)
                    break;

                canvas.translate(leftMarginPixel, topMarginPixel + yPosition);
                staffs.get(staffNum).Draw(canvas, clip, paint);
                canvas.translate(-leftMarginPixel, -(topMarginPixel + yPosition));
                yPosition += staffs.get(staffNum).getHeight();
            }
        }

        /* Draw the page number */
        canvas.drawText("" + pageNumber,
                PageWidth - leftMarginPixel,
                topMarginPixel + PageHeight - 12,
                paint);

    }


    /** Shade all the chords played at the given pulse time.
     *  First, make sure the current scroll position is in the bufferBitmap.
     *  Loop through all the staffs and call staff.Shade().
     *  If scrollGradually is true, scroll gradually (smooth scrolling)
     *  to the shaded notes.
     */
    public void ShadeNotes(int currentPulseTime, int prevPulseTime, int scrollType) {
        if (!surfaceReady || staffs == null) {
            return;
        }
        if (bufferCanvas == null) {
            createBufferCanvas();
        }

        /* Loop through each staff.  Each staff will shade any notes that
         * start at currentPulseTime, and unshade notes at prevPulseTime.
         */
        int xShade = 0;
        paint.setAntiAlias(true);
        for(int i=0; i<2; i++) {
            for(int j=0; j<2; j++) {
                Staff staff = nowStaffs[i][j];
                bufferCanvas.translate(0, staffHeight * (i*2+j)); // 두번째악보는 이게 문제.
                xShade = staff.ShadeNotes(bufferCanvas, paint, shade1,
                        currentPulseTime, prevPulseTime, xShade, this);
                bufferCanvas.translate(0, -(staffHeight * (i*2+j)));
                //Log.i("chk", "xShade : " + xShade + " yPosition : " + yPosition);
            }
        }

        /* Draw the buffer canvas to the real canvas.        
         * Translate canvas such that (scrollX,scrollY) within the 
         * bufferCanvas maps to (0,0) on the real canvas.
         */
        SurfaceHolder holder = getHolder();
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) {
            return;
        }
        canvas.translate(-(scrollX - bufferX), -(scrollY - bufferY));
        canvas.drawBitmap(bufferBitmap, 0, 0, paint);
        canvas.translate(scrollX - bufferX, scrollY - bufferY);
        holder.unlockCanvasAndPost(canvas);
    }

    /** Return the pulseTime corresponding to the given point on the SheetMusic.
     *  First, find the staff corresponding to the point.
     *  Then, within the staff, find the notes/symbols corresponding to the point,
     *  and return the startTime (pulseTime) of the symbols.
     */
    public int PulseTimeForPoint(Point point) {        //Point scaledPoint = new Point((int)(point.x / zoom), (int)(point.y / zoom));

        int y = 0;
        for (Staff staff : staffs) {
            if (point.y >= y && point.y <= y + staff.getHeight()) {
                return staff.PulseTimeForPoint(point);
            }
            y += staff.getHeight();
        }
        return -1;
    }

    /** Handle touch/motion events to implement scrolling the sheet music. */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        //boolean result = scrollAnimation.onTouchEvent(event);
        boolean result = false;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // If we touch while music is playing, stop the midi player 
                if (player != null && player.getVisibility() == View.GONE) {
                    player.Pause();
                    //scrollAnimation.stopMotion();
                }
                return result;

            case MotionEvent.ACTION_MOVE:
                return result;

            case MotionEvent.ACTION_UP:
                return result;

            default:
                return false;
        }
    }

    public void setPlayer(MidiPlayer p) {
        player = p;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        callOnDraw();
    }

    /** Surface is ready for shading the notes */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceReady = true;
    }

    /** Surface has been destroyed */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
    }

    @Override
    public String toString() {
        String result = "SheetMusic staffs=" + staffs.size() + "\n";
        for (Staff staff : staffs) {
            result += staff.toString();
        }
        result += "End SheetMusic\n";
        return result;
    }

    @Override
    public void staffEnd(int endTime) {
        Log.i("chk", "called staffEnd");
        Staff endStaff = null;
        int endStaffIndex = 0;
        for(int i=0; i<staffs.size(); i++) {
            Staff staff = staffs.get(i);
            if(staff.getEndTime() == endTime) {
                endStaff = staff;
                endStaffIndex = i;
                break;
            }
        }
        if(endStaff == null) {
            return; // Error
        }

        int newStaffIndex = 0;
        for(int i=0; i<2; i++) {
            for(int j=0; j<2; j++) {
                Staff staff = nowStaffs[i][j];
                if(staff.getEndTime() == endTime) {
                    if(staffs.size() < endStaffIndex + 4) {
                        return;
                    } try {
                        newStaffIndex = i;
                        nowStaffs[i][j] = staffs.get(endStaffIndex + 4);
                        nowStaffs[i][j + 1] = staffs.get(endStaffIndex + 5);
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                        return;
                    }
                    break;
                }
            }
        }
        drawNewStaff(newStaffIndex);
    }

    public void drawNewStaff(int newStaffIndex) {
        int i = newStaffIndex;

        Rect area = new Rect(0, 0, clip.right, staffHeight * 2);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        bufferCanvas.drawRect(area, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);

        for(int j=0; j<2; j++) {
            Staff staff = nowStaffs[i][j];
            bufferCanvas.translate(0, staffHeight * j);
            staff.Draw(bufferCanvas, clip, paint);
            bufferCanvas.translate(0, -staffHeight * j);
        }
    }
}

