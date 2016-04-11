package com.global.karaokevewer.Midi.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.global.karaokevewer.Midi.event.MidiEvent;
import com.global.karaokevewer.Midi.event.NoteOn;
import com.global.karaokevewer.Midi.event.PitchBend;
import com.global.karaokevewer.Midi.event.meta.KeySignature;
import com.global.karaokevewer.Midi.event.meta.MidiLyrics;
import com.global.karaokevewer.Midi.event.meta.TimeSignature;
import com.global.karaokevewer.Midi.renderer.midi.MidiSymbol;
import com.global.karaokevewer.Midi.renderer.midi.NoteSymbol;
import com.global.karaokevewer.Midi.renderer.midi.RestSymbol;
import com.global.karaokevewer.Midi.util.MidiInfo;
import com.global.karaokevewer.Midi.util.MidiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by clogic on 2015. 12. 12..
 */
public class MeasureSymbol extends Symbol {

    public ArrayList<Symbol> symbols;
    public ArrayList<MidiSymbol> notes;
    public TimeSignatureSymbol timeSignature;

    public int startTicks;
    public int endTicks;

    public int height;

    public int paddingLeft = 0;

    public float BPM;

    public int segment;

    public int numerator; // 6
    public int denominator; // 8

    public int myIndex;

    public final ArrayList<LyricSymbol> lyricsList;
    public String lyrics = "";

    public static int totalNoteValue = 0;
    public static int noteCount = 0;

    private static final String TAG = MeasureSymbol.class.getSimpleName();

    public MeasureSymbol() {
        symbols = new ArrayList<>();
        notes = new ArrayList<>();
        lyricsList = new ArrayList<>();

        height = MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 4 + MidiInfo.FIRST_LINE_HEIGHT;
    }

    @Override
    public void draw(Canvas canvas) {
        int x = 0;
        int notesFullWidth = this.width;
        //segment = notesFullWidth/(notes.size()+1);
        segment = notesFullWidth / ((MidiInfo.resolution / (denominator / 4) * numerator) / (MidiInfo.resolution/4) + 1);

        /**
         * calculating segment..
         */

        int nowX = segment;
        int beforeDuration = 0;
        for(Symbol symbol : symbols) {
            if(symbol instanceof RestSymbol &&
                    (((RestSymbol) symbol).getDuration() == (MidiInfo.resolution / (denominator / 4) * numerator))) {
                symbol.draw(canvas, notesFullWidth / 2);
            } else if(symbol instanceof MidiSymbol) {
                symbol.draw(canvas, nowX);
                ((MidiSymbol) symbol).segment = segment;
                beforeDuration = ((MidiSymbol) symbol).getDuration() / (MidiInfo.resolution/4);
                nowX += segment * beforeDuration;
            } else {
                symbol.draw(canvas, x);
                x += symbol.getWidth();
            }
        }

        Paint paint = new Paint();
        paint.setStrokeWidth(MidiInfo.LINE_STROKE * 2);
        if((myIndex+1) % MidiInfo.MEASURE_LIMIT == 0) {
            canvas.drawLine(this.width-4, MidiInfo.FIRST_LINE_HEIGHT,
                    this.width-4, MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 4, paint);
        } else {
            canvas.drawLine(this.width-2, MidiInfo.FIRST_LINE_HEIGHT,
                    this.width-2, MidiInfo.FIRST_LINE_HEIGHT + MidiInfo.LINE_SPACE_HEIGHT * 4, paint);
        }
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

    int pitchBendDelta = 0;
    private Symbol eventToSymbol(MidiEvent e) {
        if(e instanceof KeySignature) {
            return new KeySignatureSymbol((KeySignature) e);
        } else if (e instanceof TimeSignature) {
            TimeSignatureSymbol symbol = new TimeSignatureSymbol((TimeSignature) e);
            timeSignature = symbol;
            return symbol;
        } else if (e instanceof NoteOn) {
            if(((NoteOn) e).getNoteValue() < MidiInfo.LOWER_NOTE_VALUE) {
                MidiInfo.LOWER_NOTE_VALUE = ((NoteOn) e).getNoteValue();
            }
            /*if((((NoteOn) e).getNoteValue()/12) * 12 < ScoreView.DEFAULT_C) {
                ScoreView.DEFAULT_C = ((((NoteOn) e).getNoteValue()+6)/12) * 12;
                Logger.i("DEFAULT C : " + ScoreView.DEFAULT_C);
            }*/
            if(((NoteOn) e).getVelocity() > 0) {
                totalNoteValue += ((NoteOn) e).getNoteValue();
                noteCount++;
                addNoteOn((NoteOn) e);
            } else if(((NoteOn) e).getVelocity() == 0) {
                e.setDelta(e.getDelta() + pitchBendDelta);
                addNoteOff((NoteOn) e);
                pitchBendDelta = 0;
            }
            return null;
        } else if (e instanceof MidiLyrics) {
            LyricSymbol symbol = new LyricSymbol((MidiLyrics) e);
            lyricsList.add(symbol);
            return symbol;
        } else if (e instanceof PitchBend) {
            pitchBendDelta += e.getDelta();
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

//            Logger.i(TAG, "note : " + note.toString());
//            Logger.i(TAG, "note start tick : " + note.getStartTicks());
//            Logger.i(TAG, "note end tick : " + (note.getStartTicks() + note.getDuration()));
//            Logger.i(TAG, "note value : " + note.getNoteValue());
//            Logger.i(TAG, "note duration : " + note.getDuration());
//            Logger.i(TAG, "---------------------------------");

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
                /*if(lhs.getStartTicks() == rhs.getStartTicks()) {
                    throw new RuntimeException("what's happend?");
                }*/
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

        for(LyricSymbol symbol : lyricsList) {
            lyrics += symbol.midiLyrics.getLyric();
        }

        boolean isEightNote = false;
        boolean isSixteenthNote = false;
        for(int i=0; i<getAllMidiSymbols().size(); i++) {
            MidiSymbol symbol = getAllMidiSymbols().get(i);
            if(symbol instanceof RestSymbol) {
                isEightNote = false;
                isSixteenthNote = false;
                continue;
            }

            /*try {
                if(!(getAllMidiSymbols().get(i+1) instanceof NoteSymbol)) {
                    continue;
                }
                if(Math.abs(((NoteSymbol) symbol).getNoteValue() - ((NoteSymbol) getAllMidiSymbols().get(i+1)).getNoteValue()) >= 12) {
                    isSixteenthNote = false;
                    isEightNote = false;
                    continue;
                }
            } catch (Exception e) {
                continue;
            }*/
            if(MidiUtil.Sixteenth(MidiInfo.resolution) == symbol.getDuration()) {
                if(isSixteenthNote) {
                    ((NoteSymbol) symbol).prev = (NoteSymbol) getAllMidiSymbols().get(i-1);
                    ((NoteSymbol) symbol).prev.next = (NoteSymbol) symbol;
                }
                isSixteenthNote = true;
            } else {
                isSixteenthNote = false;
            }

            if(MidiUtil.Eighth(MidiInfo.resolution) == symbol.getDuration()) {
                if(isEightNote) {
                    ((NoteSymbol) symbol).prev = (NoteSymbol) getAllMidiSymbols().get(i-1);
                    ((NoteSymbol) symbol).prev.next = (NoteSymbol) symbol;
                }
                isEightNote = true;
            } else {
                isEightNote = false;
            }
        }

        for(int i=0; i<getAllMidiSymbols().size(); i++) {
            MidiSymbol symbol = getAllMidiSymbols().get(i);
            if(symbol instanceof RestSymbol) {
                continue;
            }
        }
    }

    private void addRests(List<MidiSymbol> roundNotes) {
        NoteSymbol prev = null;

        List<MidiSymbol> rests = new ArrayList<>();

        for(MidiSymbol symbol : roundNotes) {
            NoteSymbol note = (NoteSymbol) symbol;

            if(prev == null) {
                rests.add(new RestSymbol(this.startTicks, note.getStartTicks() - this.startTicks));
            } else {
                rests.add(new RestSymbol(prev.getStartTicks() + prev.getDuration(), note.getStartTicks() - prev.getStartTicks() - prev.getDuration()));
            }

            prev = note;
        }

        if(prev == null) {
            rests.add(new RestSymbol(this.startTicks, this.endTicks - this.startTicks));
        } else {
            rests.add(new RestSymbol(prev.getStartTicks() + prev.getDuration(), this.endTicks - prev.getStartTicks() - prev.getDuration()));
        }

        for(int i=rests.size()-1; i>=0; i--) {
            RestSymbol symbol = (RestSymbol) rests.get(i);
            if(symbol.getDuration() <= 0) {
                rests.remove(symbol);
            }
        }

        roundNotes.addAll(rests);
    }

    /**
     * 반올림
     * @param note
     * @return
     */
    public List<NoteSymbol> roundNote(NoteSymbol note) {
        List<NoteSymbol> notes = new ArrayList<>();

        List<Integer> scales = MidiUtil.getBeatScale(MidiInfo.resolution);
        // 30 이하의 애들을 어떻게 처리할지..

        note.roundStartTicks();
        int duration = ((note.getDuration() + 5) / 10) * 10;
        int startTicks = note.getStartTicks();
        for(int i=0; i<scales.size(); i++) {
            if (duration / scales.get(i) == 1) {
                note.setEndTicks(startTicks + duration);
            }
            if (note.getStartTicks() + note.getDuration() > this.endTicks) {
                note.setEndTicks(this.endTicks);
                note.needToTie();
            }
        }

            /*if(duration / scales.get(i) > 0) { // duration 90 -> 120으로 맞춰준다
                try {
                    note.setEndTicks(startTicks + scales.get(i - 1));
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                    note.setEndTicks(startTicks + scales.get(0));
                }
                if(note.getStartTicks() + note.getDuration() > this.endTicks) {
                    note.setEndTicks(this.endTicks);
                    note.needToTie();
                }
                break;
            }*/

        duration = note.getDuration();
        for(int i=0; i<scales.size(); i++) {
            if(duration/scales.get(i) > 0) {
                duration %= scales.get(i);
                NoteSymbol symbol = new NoteSymbol(note.getStartTicks(), note.getNoteValue(), note.getChannel());
                symbol.setEndTicks(note.getStartTicks() + scales.get(i));
                notes.add(symbol);
                note.setStartTicks(note.getStartTicks() + scales.get(i));
            }
        }

        return notes;
    }
}
