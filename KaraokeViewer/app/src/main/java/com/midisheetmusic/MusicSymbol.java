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

import android.graphics.*;

/** @class MusicSymbol
 * The MusicSymbol class represents music symbols that can be displayed
 * 뮤직심벌 클래스는 음악의 상징들을 보여지도록 할 수 있다.
 * on a staff.  This includes:
 * 악보 안에, 이것들이 삽입되어진다
 *  - Accidental symbols: sharp, flat, natural
 *  - 샵, 플랫, 기본
 *  - Chord symbols: single notes or chords
 *  - 코드 심벌: 싱글노트 또는 코드
 *  - Rest symbols: whole, half, quarter, eighth
 *  - 쉼표 심벌: 한박, 반박, 1/4박, 1/8박
 *  - Bar symbols, the vertical bars which delimit measures.
 *  - 바 심벌: 세로 바. 마디를 구분지어준다.
 *  - Treble and Bass clef symbols
 *  - 높은음자리표와 낮은음자리표 심벌들
 *  - Blank symbols, used for aligning notes in different staffs
 *  - 공백 심벌들, 각자 다른 악보의 노트들을 정렬하는데 사용되어진다.
 */

public interface MusicSymbol {

    /** Get the time (in pulses) this symbol occurs at.
     * This is used to determine the measure this symbol belongs to.
     */
    /**
     * 심벌이 발생하는 시간을 가져온다
     * 마디에 속하는 심벌들을 결정하는데 사용되곤 한다.
     */
    public int getStartTime();

    /** Get the minimum width (in pixels) needed to draw this symbol */
    /** 심벌을 그리는데 필요한 최소한의 너비를 가져온다. */
    public int getMinWidth();

    /** Get/Set the width (in pixels) of this symbol. The width is set
     * in SheetMusic.AlignSymbols() to vertically align symbols.
     */
    /** Get/Set 이 심벌의 너비를 Get/Set 한다. 이 너비는 설정되어진다,
     *  SheetMusic.AlignSymbols()에서 심벌을 가로로 정렬하기 위해서*/
    public int getWidth();
    public void setWidth(int value);

    /** Get the number of pixels this symbol extends above the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getAboveStaff();

    /** Get the number of pixels thissymbol extends below the staff. Used
     *  to determine the minimum height needed for the staff (Staff.FindBounds).
     */
    public int getBelowStaff();

    /** Draw the symbol.
     * @param ytop The ylocation (in pixels) where the top of the staff starts.
     */
    /**
     * 심벌을 그린다
     * @param ytop lefttop의 y포지션
     */
    public void Draw(Canvas canvas, Paint paint, int ytop);

}


