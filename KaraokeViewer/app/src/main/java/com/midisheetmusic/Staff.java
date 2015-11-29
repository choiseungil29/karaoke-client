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
import android.graphics.*;
import android.util.Log;

import com.midisheetmusic.enums.Clef;


/* @class Staff
 * The Staff is used to draw a single Staff (a row of measures) in the 
 * SheetMusic Control. A Staff needs to draw
 * - The Clef
 * - The key signature
 * - The horizontal lines
 * - A list of MusicSymbols
 * - The left and right vertical lines
 *
 * The height of the Staff is determined by the number of pixels each
 * MusicSymbol extends above and below the staff.
 *
 * The vertical lines (left and right sides) of the staff are joined
 * with the staffs above and below it, with one exception.  
 * The last track is not joined with the first track.
 */

public class Staff {
    private ArrayList<MusicSymbol> symbols;  /** The music symbols in this staff */
    private ArrayList<LyricSymbol> lyrics;   /** The lyrics to display (can be null) */
    private int yTop;                   /** The y pixel of the top of the staff */
    private ClefSymbol clefsym;         /** The left-side Clef symbol */
    private AccidentalSymbol[] keys;         /** The key signature symbols */
    private boolean showMeasures;       /** If true, show the measure numbers */
    private int keysigWidth;            /** The width of the clef and key signature */
    private int width;                  /** The width of the staff in pixels */
    private int height;                 /** The height of the staff in pixels */
    private int tracknum;               /** The track this staff represents */
    private int totaltracks;            /** The total number of tracks */
    private int starttime;              /** The time (in pulses) of first symbol */
    private int endtime;                /** The time (in pulses) of last symbol */
    private int measureLength;          /** The time (in pulses) of a measure */

    /** Create a new staff with the given list of music symbols,
     * and the given key signature.  The clef is determined by
     * the clef of the first chord symbol. The track number is used
     * to determine whether to join this left/right vertical sides
     * with the staffs above and below. The MidiOptions are used
     * to check whether to display measure numbers or not.
     */
    public Staff(ArrayList<MusicSymbol> symbols, KeySignature key, 
                 MidiOptions options, int tracknum, int totaltracks)  {

        keysigWidth = SheetMusic.KeySignatureWidth(key);
        this.tracknum = tracknum;
        this.totaltracks = totaltracks;
        showMeasures = (options.showMeasures && tracknum == 0);
        if (options.time != null) {
            measureLength = options.time.getMeasure();
        }
        else {
            measureLength = options.defaultTime.getMeasure();
        }
        Clef clef = FindClef(symbols);

        clefsym = new ClefSymbol(clef, 0, false);
        keys = key.GetSymbols(clef);
        this.symbols = symbols;
        CalculateWidth(options.scrollVert);
        CalculateHeight();
        CalculateStartEndTime();
        FullJustify();
    }

    /** Return the width of the staff */
    public int getWidth() { return width; }

    /** Return the height of the staff */
    public int getHeight() { return height; }

    /** Return the track number of this staff (starting from 0 */
    public int getTrack() { return tracknum; }

    /** Return the starting time of the staff, the start time of
     *  the first symbol.  This is used during playback, to 
     *  automatically scroll the music while playing.
     */
    public int getStartTime() { return starttime; }

    /** Return the ending time of the staff, the endtime of
     *  the last symbol.  This is used during playback, to 
     *  automatically scroll the music while playing.
     */
    public int getEndTime() { return endtime; }
    public void setEndTime(int value) { endtime = value; }

    /** Find the initial clef to use for this staff.  Use the clef of
     * the first ChordSymbol.
     */
    private Clef FindClef(ArrayList<MusicSymbol> list) {
        for (MusicSymbol m : list) {
            if (m instanceof ChordSymbol) {
                ChordSymbol c = (ChordSymbol) m;
                return c.getClef();
            }
        }
        return Clef.Treble;
    }

    /** Calculate the height of this staff.  Each MusicSymbol contains the
     * number of pixels it needs above and below the staff.  Get the maximum
     * values above and below the staff.
     */
    public void CalculateHeight() {
        int above = 0;
        int below = 0;

        for (MusicSymbol s : symbols) {
            above = Math.max(above, s.getAboveStaff());
            below = Math.max(below, s.getBelowStaff());
        }
        above = Math.max(above, clefsym.getAboveStaff());
        below = Math.max(below, clefsym.getBelowStaff());
        if (showMeasures) {
            above = Math.max(above, SheetMusic.NoteHeight * 3);
        }
        yTop = above + SheetMusic.NoteHeight;
        height = SheetMusic.NoteHeight*5 + yTop + below;
        if (lyrics != null) {
            height += SheetMusic.NoteHeight * 3/2;
        }

        /* Add some extra vertical space between the last track
         * and first track.
         */
        if (tracknum == totaltracks-1)
            height += SheetMusic.NoteHeight * 3;
    }

    /** Calculate the width of this staff */
    private void CalculateWidth(boolean scrollVert) {
        if (scrollVert) {
            width = SheetMusic.PageWidth;
            return;
        }
        width = keysigWidth;
        for (MusicSymbol s : symbols) {
            width += s.getWidth();
        }
    }

    /** Calculate the start and end time of this staff. */
    private void CalculateStartEndTime() {
        starttime = endtime = 0;
        if (symbols.size() == 0) {
            return;
        }
        starttime = symbols.get(0).getStartTime();
        for (MusicSymbol m : symbols) {
            if (endtime < m.getStartTime()) {
                endtime = m.getStartTime();
            }
            if (m instanceof ChordSymbol) {
                ChordSymbol c = (ChordSymbol) m;
                if (endtime < c.getEndTime()) {
                    endtime = c.getEndTime();
                }
            }
        }
    }


    /** Full-Justify the symbols, so that they expand to fill the whole staff. */
    private void FullJustify() {
        if (width != SheetMusic.PageWidth)
            return;

        int totalwidth = keysigWidth;
        int totalsymbols = 0;
        int i = 0;

        while (i < symbols.size()) {
            int start = symbols.get(i).getStartTime();
            totalsymbols++;
            totalwidth += symbols.get(i).getWidth();
            i++;
            while (i < symbols.size() && symbols.get(i).getStartTime() == start) {
                totalwidth += symbols.get(i).getWidth();
                i++;
            }
        }

        int extrawidth = (SheetMusic.PageWidth - totalwidth - 1) / totalsymbols;
        if (extrawidth > SheetMusic.NoteHeight*2) {
            extrawidth = SheetMusic.NoteHeight*2;
        }
        i = 0;
        while (i < symbols.size()) {
            int start = symbols.get(i).getStartTime();
            int newwidth = symbols.get(i).getWidth() + extrawidth;
            symbols.get(i).setWidth(newwidth);
            i++;
            while (i < symbols.size() && symbols.get(i).getStartTime() == start) {
                i++;
            }
        }
    }


    /** Add the lyric symbols that occur within this staff.
     *  Set the x-position of the lyric symbol.
     */
    public void AddLyrics(ArrayList<LyricSymbol> tracklyrics) {
        if (tracklyrics == null || tracklyrics.size() == 0) {
            return;
        }
        lyrics = new ArrayList<LyricSymbol>();
        int xpos = 0;
        int symbolindex = 0;
        for (LyricSymbol lyric : tracklyrics) {
            if (lyric.getStartTime() < starttime) {
                continue;
            }
            if (lyric.getStartTime() > endtime) {
                break;
            }
            /* Get the x-position of this lyric */
            while (symbolindex < symbols.size() &&
                   symbols.get(symbolindex).getStartTime() < lyric.getStartTime()) {
                xpos += symbols.get(symbolindex).getWidth();
                symbolindex++;
            }
            lyric.setX(xpos);
            if (symbolindex < symbols.size() &&
                (symbols.get(symbolindex) instanceof BarSymbol)) {
                lyric.setX(lyric.getX() + SheetMusic.NoteWidth);
            }
            lyrics.add(lyric);
        }
        if (lyrics.size() == 0) {
            lyrics = null;
        }
    }

    /** Draw the lyrics */
    private void DrawLyrics(Canvas canvas, Paint paint) {
        /* Skip the left side Clef symbol and key signature */
        int xpos = keysigWidth;
        int ypos = height - SheetMusic.NoteHeight * 3/2;

        for (LyricSymbol lyric : lyrics) {
            canvas.drawText(lyric.getText(),
                            xpos + lyric.getX(),
                            ypos,
                            paint);
        }
    }


    /** Draw the measure numbers for each measure */
    private void DrawMeasureNumbers(Canvas canvas, Paint paint) {
        /* Skip the left side Clef symbol and key signature */
        int xpos = keysigWidth;
        int ypos = yTop - SheetMusic.NoteHeight * 3;

        for (MusicSymbol s : symbols) {
            if (s instanceof BarSymbol) {
                int measure = 1 + s.getStartTime() / measureLength;
                canvas.drawText("" + measure,
                                xpos + SheetMusic.NoteWidth/2,
                                ypos,
                                paint);
            }
            xpos += s.getWidth();
        }
    }


    /** Draw the five horizontal lines of the staff */
    private void DrawHorizLines(Canvas canvas, Paint paint) {
        int line = 1;
        int y = yTop - SheetMusic.LineWidth;
        paint.setStrokeWidth(1);
        for (line = 1; line <= 5; line++) {
            canvas.drawLine(SheetMusic.LeftMargin, y, width-1, y, paint);
            y += SheetMusic.LineWidth + SheetMusic.LineSpace;
        }

    }

    /** Draw the vertical lines at the far left and far right sides. */
    private void DrawEndLines(Canvas canvas, Paint paint) {
        paint.setStrokeWidth(1);

        /* Draw the vertical lines from 0 to the height of this staff,
         * including the space above and below the staff, with two exceptions:
         * - If this is the first track, don't start above the staff.
         *   Start exactly at the top of the staff (yTop - LineWidth)
         * - If this is the last track, don't end below the staff.
         *   End exactly at the bottom of the staff.
         */
        int ystart, yend;
        if (tracknum == 0)
            ystart = yTop - SheetMusic.LineWidth;
        else
            ystart = 0;

        if (tracknum == (totaltracks-1))
            yend = yTop + 4 * SheetMusic.NoteHeight;
        else
            yend = height;

        canvas.drawLine(SheetMusic.LeftMargin, ystart, SheetMusic.LeftMargin, yend, paint);

        canvas.drawLine(width-1, ystart, width-1, yend, paint);

    }

    /** Draw this staff. Only draw the symbols inside the clip area */
    public void Draw(Canvas canvas, Rect clip, Paint paint) {
        paint.setColor(Color.BLACK);
        int xPosition = SheetMusic.LeftMargin + 5;

        /* Draw the left side Clef symbol */
        canvas.translate(xPosition, 0);
        clefsym.Draw(canvas, paint, yTop);
        canvas.translate(-xPosition, 0);
        xPosition += clefsym.getWidth();

        /* Draw the key signature */
        for (AccidentalSymbol a : keys) {
            canvas.translate(xPosition, 0);
            a.Draw(canvas, paint, yTop);
            canvas.translate(-xPosition, 0);
            xPosition += a.getWidth();
        }
       
        /* Draw the actual notes, rests, bars.  Draw the symbols one 
         * after another, using the symbol width to determine the
         * x position of the next symbol.
         *
         * For fast performance, only draw symbols that are in the clip area.
         */
        for (MusicSymbol s : symbols) {
            if ((xPosition <= clip.left + clip.width() + 50) && (xPosition + s.getWidth() + 50 >= clip.left)) {
                canvas.translate(xPosition, 0);
                s.Draw(canvas, paint, yTop);
                canvas.translate(-xPosition, 0);
            }
            xPosition += s.getWidth();
        }
        paint.setColor(Color.BLACK);
        DrawHorizLines(canvas, paint);
        DrawEndLines(canvas, paint);

        if (showMeasures) {
            DrawMeasureNumbers(canvas, paint);
        }
        if (lyrics != null) {
            DrawLyrics(canvas, paint);
        }

    }


    /** Shade all the chords played in the given time.
     *  Un-shade any chords shaded in the previous pulse time.
     *  Store the x coordinate location where the shade was drawn.
     */
    public int ShadeNotes(Canvas canvas, Paint paint, int shade,
                           int currentPulseTime, int prevPulseTime, int xShade) {

        /* If there's nothing to unshade, or shade, return */
        if ((starttime > prevPulseTime || endtime < prevPulseTime) &&
            (starttime > currentPulseTime || endtime < currentPulseTime)) {
            return xShade;
        }

        /* Skip the left side Clef symbol and key signature */
        int xPosition = keysigWidth;

        MusicSymbol currentSymbol = null;
        ChordSymbol prevChord = null;
        int prevX = 0;

        /* Loop through the symbols. 
         * Unshade symbols where start <= prevPulseTime < end
         * Shade symbols where start <= currentPulseTime < end
         */ 
        for (int i = 0; i < symbols.size(); i++) {
            currentSymbol = symbols.get(i);
            if (currentSymbol instanceof BarSymbol) {
                xPosition += currentSymbol.getWidth();
                continue;
            }

            int start = currentSymbol.getStartTime();
            int end = 0;
            if (i+2 < symbols.size() && symbols.get(i+1) instanceof BarSymbol) {
                end = symbols.get(i+2).getStartTime();
            }
            else if (i+1 < symbols.size()) {
                end = symbols.get(i+1).getStartTime();
            }
            else {
                end = endtime;
            }


            /* If we've past the previous and current times, we're done. */
            if ((start > prevPulseTime) && (start > currentPulseTime)) {
                if (xShade == 0) {
                    xShade = xPosition;
                }
                return xShade;
            }
            /* If shaded notes are the same, we're done */
            if ((start <= currentPulseTime) && (currentPulseTime < end) &&
                (start <= prevPulseTime) && (prevPulseTime < end)) {

                xShade = xPosition;
                return xShade;
            }

            boolean redrawLines = false;

            /* If symbol is in the current time, draw a shaded background */
            if ((start <= currentPulseTime) && (currentPulseTime < end)) {
                xShade = xPosition;
                canvas.translate(xPosition, 0);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(shade);
                canvas.drawRect(0, 0, currentSymbol.getWidth(), this.getHeight(), paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                currentSymbol.Draw(canvas, paint, yTop);
                canvas.translate(-xPosition, 0);
                redrawLines = true;
            }

            /* If either a gray or white background was drawn, we need to redraw
             * the horizontal staff lines, and redraw the stem of the previous chord.
             */
            if (redrawLines) {
                int line = 1;
                int y = yTop - SheetMusic.LineWidth;
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(1);
                canvas.translate(xPosition-2, 0);
                for (line = 1; line <= 5; line++) {
                    canvas.drawLine(0, y, currentSymbol.getWidth()+4, y, paint);
                    y += SheetMusic.LineWidth + SheetMusic.LineSpace;
                }
                canvas.translate(-(xPosition-2), 0);

                if (prevChord != null) {
                    canvas.translate(prevX, 0);
                    prevChord.Draw(canvas, paint, yTop);
                    canvas.translate(-prevX, 0);
                }
                if (showMeasures) {
                    DrawMeasureNumbers(canvas, paint);
                }
                if (lyrics != null) {
                    DrawLyrics(canvas, paint);
                }
            }
            if (currentSymbol instanceof ChordSymbol) {
                ChordSymbol chord = (ChordSymbol) currentSymbol;
                if (chord.getStem() != null && !chord.getStem().getReceiver()) {
                    prevChord = (ChordSymbol) currentSymbol;
                    prevX = xPosition;
                }
            }
            xPosition += currentSymbol.getWidth();
        }
        return xShade;
    }

    /** Return the pulse time corresponding to the given point.
     *  Find the notes/symbols corresponding to the x position,
     *  and return the startTime (pulseTime) of the symbol.
     */
    public int PulseTimeForPoint(Point point) {

        int xpos = keysigWidth;
        int pulseTime = starttime;
        for (MusicSymbol sym : symbols) {
            pulseTime = sym.getStartTime();
            if (point.x <= xpos + sym.getWidth()) {
                return pulseTime;
            }
            xpos += sym.getWidth();
        }
        return pulseTime;
    }


    @Override
    public String toString() {
        String result = "Staff clef=" + clefsym.toString() + "\n";
        result += "  Keys:\n";
        for (AccidentalSymbol a : keys) {
            result += "    " + a.toString() + "\n";
        }
        result += "  Symbols:\n";
        for (MusicSymbol s : keys) {
            result += "    " + s.toString() + "\n";
        }
        for (MusicSymbol m : symbols) {
            result += "    " + m.toString() + "\n";
        }
        result += "End Staff\n";
        return result;
    }

}


