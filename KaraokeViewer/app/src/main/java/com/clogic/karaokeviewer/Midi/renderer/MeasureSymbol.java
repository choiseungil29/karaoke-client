package com.clogic.karaokeviewer.Midi.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.clogic.karaokeviewer.Midi.event.MidiEvent;
import com.clogic.karaokeviewer.Midi.event.NoteOn;
import com.clogic.karaokeviewer.Midi.event.meta.KeySignature;
import com.clogic.karaokeviewer.Midi.event.meta.Lyrics;
import com.clogic.karaokeviewer.Midi.event.meta.Tempo;
import com.clogic.karaokeviewer.Midi.event.meta.TimeSignature;
import com.clogic.karaokeviewer.Midi.renderer.midi.MidiSymbol;
import com.clogic.karaokeviewer.Midi.renderer.midi.NoteSymbol;
import com.clogic.karaokeviewer.Midi.renderer.midi.RestSymbol;
import com.clogic.karaokeviewer.Midi.util.MidiUtil;
import com.clogic.karaokeviewer.Util.Logger;
import com.clogic.karaokeviewer.View.ScoreView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by clogic on 2015. 12. 12..
 */
public class MeasureSymbol extends Symbol {

    private ArrayList<Symbol> symbols;
    private ArrayList<MidiSymbol> notes;

    public int startTicks;
    public int endTicks;

    public int height;

    public int paddingLeft = 0;

    public Tempo tempo;

    public float BPM;

    public final ArrayList<LyricSymbol> lyricsList;
    public String lyrics = "";

    public MeasureSymbol() {
        symbols = new ArrayList<>();
        notes = new ArrayList<>();
        lyricsList = new ArrayList<>();

        height = ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 4 + ScoreView.FIRST_LINE_HEIGHT;
    }

    @Override
    public void draw(Canvas canvas) {
        int x = 0;
        int notesFullWidth = this.width - paddingLeft;
        int segment = notesFullWidth/(notes.size()+1);
        /**
         * calculating segment..
         */

        for(Symbol symbol : symbols) {
            if(symbol instanceof MidiSymbol) {
                symbol.draw(canvas, segment + paddingLeft);
                segment += notesFullWidth/(notes.size()+1);
            } else {
                symbol.draw(canvas, x);
                x += symbol.getWidth();
            }
        }
        Paint paint = new Paint();
        paint.setStrokeWidth(ScoreView.LINE_STROKE * 2);
        canvas.drawLine(this.width, ScoreView.FIRST_LINE_HEIGHT,
                        this.width, ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 4, paint);
    }

    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    public void addSymbol(MidiEvent event) {
        Symbol symbol = eventToSymbol(event);
        if(symbol == null) {
            return;
        }
        addSymbol(symbol);
    }

    /**
     * 모든 심벌들을 반환한다.
     * @return
     */
    public List<Symbol> getAllSymbols() {
        return symbols;
    }

    /**
     * 모든 노트심벌들을 반환한다.
     * @return
     */
    public List<MidiSymbol> getAllMidiSymbols() {
        return notes;
    }

    private Symbol eventToSymbol(MidiEvent e) {
        if(e instanceof KeySignature) {
            return new KeySignatureSymbol((KeySignature) e);
        } else if (e instanceof TimeSignature) {
            return new TimeSignatureSymbol((TimeSignature) e);
        } else if (e instanceof NoteOn) {
            /*Logger.i("NoteOn    NoteValue : " + ((NoteOn) e).getNoteValue() + ", Tick : "
             + e.getTick() + ", Velocity : " + ((NoteOn) e).getVelocity() + ", Delta : " + e.getDelta());*/
            if((((NoteOn) e).getNoteValue()/12) * 12 < ScoreView.DEFAULT_C) {
                ScoreView.DEFAULT_C = (((NoteOn) e).getNoteValue()/12) * 12;
            }
            if(((NoteOn) e).getVelocity() > 0) {
                addNoteOn((NoteOn) e);
            } else if(((NoteOn) e).getVelocity() == 0) {
                addNoteOff((NoteOn) e);
            }
            return null;
        } else if (e instanceof Lyrics) {
            Logger.i(e.toString());
            LyricSymbol symbol = new LyricSymbol((Lyrics) e);
            lyricsList.add(symbol);
            return symbol;
        }
        return null;
    }

    public void addNoteOn(NoteOn event) {
        notes.add(new NoteSymbol((int) event.getTick(), event.getNoteValue(), event.getChannel()));
    }

    public void addNoteOff(NoteOn event) {
        for(int i=notes.size()-1; i>=0; i--) {
            if(!(notes.get(i) instanceof NoteSymbol)) {
                continue;
            }
            NoteSymbol symbol = (NoteSymbol) notes.get(i);
            if(symbol.getChannel() == event.getChannel() &&
                    symbol.getNoteValue() == event.getNoteValue() &&
                    symbol.getDuration() == 0) {
                symbol.setEndTicks((int) event.getTick());
                return;
            }
        }

        // 여기까지오면 앞에 노트한개 넣어줘야함
        // case1 : 마디가 시작했는데 NoteOff가 들어온경우.
        NoteSymbol symbol = new NoteSymbol(this.startTicks, event.getNoteValue(), event.getChannel());
        symbol.setEndTicks((int) event.getTick());
        notes.add(symbol);
    }

    public void created() {
        // case2 : 마디가 끝났는데 이음줄이 필요한 경우
        List<MidiSymbol> roundNotes = new ArrayList<>();
        for (int i=0; i<notes.size(); i++) {
            if(!(notes.get(i) instanceof NoteSymbol)) {
                continue;
            }
            NoteSymbol note = (NoteSymbol) notes.get(i);
            if(note.getDuration() == 0) {
                note.setEndTicks(this.endTicks);
                note.needToTie();
            }

            if(i < notes.size()-1) {
                // 다음 노트의 startTicks보다 현재 노트의 startTicks + duration이 크면 값을 축소시킴
                if(note.getStartTicks() + note.getDuration() > notes.get(i+1).getStartTicks()) {
                    note.setEndTicks(notes.get(i+1).getStartTicks());
                }
            }

            // 노트 duration 보정구간
            roundNotes.addAll(roundNote(note));
        }

        // 위 반복문이 끝나면 현재 마디의 노트 Duration 보정까지 끝남.
        // 그럼 이제 쉼표를 채워야함
        addRests(roundNotes);

        Collections.sort(roundNotes, new Comparator<MidiSymbol>() {
            @Override
            public int compare(MidiSymbol lhs, MidiSymbol rhs) {
                if(lhs.getStartTicks() < rhs.getStartTicks()) {
                    return -1;
                }
                if(lhs.getStartTicks() == rhs.getStartTicks()) {
                    throw new RuntimeException("what's happend?");
                }
                if(lhs.getStartTicks() > rhs.getStartTicks()) {
                    return 1;
                }

                return 0;
            }
        });

        // 쉼표 다 채우고 정렬까지 끝났다. 이제 symbols에 집어넣어야함
        symbols.addAll(roundNotes);
        notes.clear();
        notes.addAll(roundNotes);

        Collections.sort(symbols, new Comparator<Symbol>() {
            @Override
            public int compare(Symbol lhs, Symbol rhs) {
                if(lhs instanceof KeySignatureSymbol && rhs instanceof TimeSignatureSymbol) {
                    return -1;
                }
                return 0;
            }
        });

        Logger.i("measure created, startTicks : " + startTicks + ", endTicks : " + endTicks);
        for(Symbol symbol : symbols) {
            if(symbol instanceof KeySignatureSymbol) {
                Logger.i("Key Signature");
            }
            if(symbol instanceof TimeSignatureSymbol) {
                Logger.i("Time Signature");
            }
            if(symbol instanceof NoteSymbol) {
                Logger.i("Note -> Tick : " + ((NoteSymbol) symbol).getStartTicks()
                        + ", Duration : " + ((NoteSymbol) symbol).getDuration()
                        + ", NoteValue : " + ((NoteSymbol) symbol).getNoteValue()
                        + ", Tie : " + ((NoteSymbol) symbol).isTie());
            }
            if(symbol instanceof RestSymbol) {
                Logger.i("Rest -> Tick : " + ((RestSymbol) symbol).getStartTicks()
                        + ", Duration : " + ((RestSymbol) symbol).getDuration());
            }
        }

        for(LyricSymbol symbol : lyricsList) {
            lyrics += symbol.lyrics.getLyric();
        }
    }

    private void addRests(List<MidiSymbol> roundNotes) {
        NoteSymbol prev = null;

        List<Integer> scales = MidiUtil.getBeatScale(ScoreView.resolution);

        List<MidiSymbol> rests = new ArrayList<>();
        int startTicks;
        for(Symbol symbol : roundNotes) {
            if(!(symbol instanceof NoteSymbol)) {
                continue;
            }

            NoteSymbol note = (NoteSymbol) symbol;

            if(prev == null &&
                    this.startTicks != note.getStartTicks()) {
                startTicks = this.startTicks;
                int restDuration = note.getStartTicks() - startTicks;
                rests.add(new RestSymbol(startTicks, restDuration));
            }

            if(prev != null &&
                    (prev.getStartTicks() + prev.getDuration()) != note.getStartTicks()) {
                int restDuration = note.getStartTicks() - (prev.getStartTicks() + prev.getDuration());
                startTicks = prev.getStartTicks() + prev.getDuration();
                rests.add(new RestSymbol(startTicks, restDuration));
            }

            prev = note;
        }

        if(prev != null && (endTicks - (prev.getStartTicks() + prev.getDuration()) > 0)) {
            int duration = endTicks - (prev.getStartTicks() + prev.getDuration());
            int restStartTicks = prev.getStartTicks() + prev.getDuration();
            for(int i=0; i<scales.size(); i++) {
                if(duration/scales.get(i) > 0) {
                    duration -= scales.get(i);
                    rests.add(new RestSymbol(restStartTicks, scales.get(i)));
                    restStartTicks += scales.get(i);
                }
            }
        }
        if(prev == null) {
            int duration = endTicks - this.startTicks;
            int restStartTicks = this.startTicks;
            for(int i=0; i<scales.size(); i++) {
                if(duration/scales.get(i) > 0) {
                    duration -= scales.get(i);
                    rests.add(new RestSymbol(restStartTicks, scales.get(i)));
                    restStartTicks += scales.get(i);
                    i--;
                }
            }
        }

        roundNotes.addAll(rests);
    }

    public List<NoteSymbol> roundNote(NoteSymbol note) {
        List<NoteSymbol> notes = new ArrayList<>();

        List<Integer> scales = MidiUtil.getBeatScale(ScoreView.resolution);
        // 30 이하의 애들을 어떻게 처리할지..

        int duration = note.getDuration();
        int startTicks = note.getStartTicks();
        NoteSymbol symbol = null;
        for(int i=0; i<scales.size(); i++) {
            if(duration / scales.get(i) > 0) {
                if(symbol != null) {
                    symbol.needToTie();
                }
                duration %= scales.get(i);
                symbol = new NoteSymbol(startTicks, note.getNoteValue(), note.getChannel());
                symbol.setEndTicks(startTicks + scales.get(i));
                notes.add(symbol);
                startTicks += scales.get(i);
            }
        }

        return notes;
    }
}
