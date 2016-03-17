//////////////////////////////////////////////////////////////////////////////
//	Copyright 2011 Alex Leffelman
//	
//	Licensed under the Apache License, Version 2.0 (the "License");
//	you may not use this file except in compliance with the License.
//	You may obtain a copy of the License at
//	
//	http://www.apache.org/licenses/LICENSE-2.0
//	
//	Unless required by applicable law or agreed to in writing, software
//	distributed under the License is distributed on an "AS IS" BASIS,
//	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//	See the License for the specific language governing permissions and
//	limitations under the License.
//////////////////////////////////////////////////////////////////////////////

package com.karaokepang.Midi.util;

import com.karaokepang.Util.Logger;
import com.karaokepang.View.ScoreView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MidiUtil
{
    private static String TAG = MidiUtil.class.getSimpleName();
    /**
     * MIDI Unit Conversions
     */
    public static long ticksToMs(long ticks, int mpqn, int resolution)
    {
        return ((ticks * mpqn) / resolution) / 1000;
    }

    public static long ticksToMs(long ticks, float bpm, int resolution)
    {
        return ticksToMs(ticks, bpmToMpqn(bpm), resolution);
    }

    public static double msToTicks(long ms, int mpqn, int ppq)
    {
        return ((ms * 1000.0) * ppq) / mpqn;
    }

    public static double msToTicks(long ms, float bpm, int ppq)
    {
        return msToTicks(ms, bpmToMpqn(bpm), ppq);
    }

    public static int bpmToMpqn(float bpm)
    {
        return (int) (bpm * 60000000);
    }

    public static float mpqnToBpm(int mpqn)
    {
        return mpqn / 60000000.0f;
    }

    /**
     * Utility methods for working with bytes and byte buffers
     */
    public static int bytesToInt(byte[] buff, int off, int len)
    {
        int num = 0;

        int shift = 0;
        for(int i = off + len - 1; i >= off; i--)
        {

            num += (buff[i] & 0xFF) << shift;
            shift += 8;
        }

        return num;
    }

    public static byte[] intToBytes(int val, int byteCount)
    {
        byte[] buffer = new byte[byteCount];

        int[] ints = new int[byteCount];

        for(int i = 0; i < byteCount; i++)
        {
            ints[i] = val & 0xFF;
            buffer[byteCount - i - 1] = (byte) ints[i];

            val = val >> 8;

            if(val == 0)
            {
                break;
            }
        }

        return buffer;
    }

    public static boolean bytesEqual(byte[] buf1, byte[] buf2, int off, int len)
    {
        for(int i = off; i < off + len; i++)
        {
            if(i >= buf1.length || i >= buf2.length)
            {
                return false;
            }
            if(buf1[i] != buf2[i])
            {
                return false;
            }
        }
        return true;
    }

    public static byte[] extractBytes(byte[] buffer, int off, int len)
    {
        byte[] ret = new byte[len];

        for(int i = 0; i < len; i++)
        {
            ret[i] = buffer[off + i];
        }

        return ret;
    }

    private static final String HEX = "0123456789ABCDEF";

    public static String byteToHex(byte b)
    {
        int high = (b & 0xF0) >> 4;
        int low = (b & 0x0F);

        return "" + HEX.charAt(high) + HEX.charAt(low);
    }

    public static String bytesToHex(byte[] b)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < b.length; i++)
        {
            sb.append(byteToHex(b[i])).append(" ");
        }
        return sb.toString();
    }

    public static List<Integer> getBeatScale(int resolution) {
        int Whole = resolution * 4;
        int Half = resolution * 2;
        int Quarter = resolution;
        int Eighth = resolution / 2;
        int Sixteenth = resolution / 4;

        int DotHalf = Half + Half/2;
        int DotQuarter = Quarter + Quarter/2;
        int DotEighth = Eighth + Eighth/2;

        List<Integer> scales = new ArrayList<>();
        scales.add(Whole);
        scales.add(DotHalf);
        scales.add(Half);
        scales.add(DotQuarter);
        scales.add(Quarter);
        //scales.add(DotEighth);
        scales.add(Eighth);
        scales.add(Sixteenth);

        /**
         * 크기 순서
         * Whole 480
         * DotHalf 360
         * Half 240
         * DotQuarter 180
         * Quarter 120
         * DotEighth 90
         * Eighth 60
         * Sixteenth 30
         */

        return scales;
    }

    public static int Whole(int resolution) {
        return getBeatScale(resolution).get(0);
    }

    public static int DotHalf(int resolution) {
        return getBeatScale(resolution).get(1);
    }

    public static int Half(int resolution) {
        return getBeatScale(resolution).get(2);
    }

    public static int DotQuarter(int resolution) {
        return getBeatScale(resolution).get(3);
    }

    public static int Quarter(int resolution) {
        return getBeatScale(resolution).get(4);
    }

    /*public static int DotEighth(int resolution) {
        return getBeatScale(resolution).get(5);
    }*/

    public static int Eighth(int resolution) {
        return getBeatScale(resolution).get(5);
    }

    public static int Sixteenth(int resolution) {
        return getBeatScale(resolution).get(6);
    }

    public static float getHeightFromNoteValue(int noteValue) {
        int octave = 12;
        float height = 0;
        float defaultHeight = ScoreView.FIRST_LINE_HEIGHT + ScoreView.LINE_SPACE_HEIGHT * 5;

        int remainder = noteValue%(ScoreView.DEFAULT_C - ScoreView.OCTAVE);

        HashMap<Integer, Integer> scale = new HashMap<>();
        // remainder, height
        for(int i=0, j=-7; i<60; i += 12, j += 7) {
            scale.put(i, 0 + j);
            scale.put(i+1, 0 + j);
            scale.put(i+2, 1 + j);
            scale.put(i+3, 1 + j);
            scale.put(i+4, 2 + j);
            scale.put(i+5, 3 + j);
            scale.put(i+6, 3 + j);
            scale.put(i+7, 4 + j);
            scale.put(i+8, 4 + j);
            scale.put(i+9, 5 + j);
            scale.put(i+10, 5 + j);
            scale.put(i+11, 6 + j);
        }

        height = defaultHeight - ((float)ScoreView.LINE_SPACE_HEIGHT/2) * scale.get(remainder);
        return height;
    }

    /**
     * 노트가 오선지 밖에 찍힌다면, ex) 낮은 도, 와 같은 자체적으로 Line이 필요한 상태라면 true를 반환.
     * 이외에는 false 반환.
     * @param noteValue 노트의 음정값
     * @return
     */
    public static boolean needToPointLine(int noteValue) {
        int remainder = noteValue%(ScoreView.DEFAULT_C - ScoreView.OCTAVE);

        HashMap<Integer, Integer> scale = new HashMap<>();
        // remainder, height
        for(int i=0, j=-7; i<60; i += 12, j += 7) {
            scale.put(i, 0 + j);
            scale.put(i+1, 0 + j);
            scale.put(i+2, 1 + j);
            scale.put(i+3, 1 + j);
            scale.put(i+4, 2 + j);
            scale.put(i+5, 3 + j);
            scale.put(i+6, 3 + j);
            scale.put(i+7, 4 + j);
            scale.put(i+8, 4 + j);
            scale.put(i+9, 5 + j);
            scale.put(i+10, 5 + j);
            scale.put(i+11, 6 + j);
        }

        /*if (scale.get(remainder) % 2 == 0) {
            return true;
        }*/
        if(remainder <= ScoreView.OCTAVE) {
            return true;
        }
        if(remainder >= ScoreView.OCTAVE * 2 + 8) {
            return true;
        }
        return false;
    }

    public static boolean isSharp(int noteValue) {
        int octave = 12;
        int remainder = noteValue%octave;
        if(remainder == 1 || remainder == 3 || remainder == 6 || remainder == 8 || remainder == 10) {
            return true;
        }
        return false;
    }

    /**
     * 꼬리방향이 위쪽인지
     * @param noteValue 노트 음정값
     * @return true -> 위로향함, false -> 아래로향함
     */
    public static boolean isTailTop(int noteValue) {
        int remainder = noteValue - ScoreView.DEFAULT_C;
        if(remainder >= 11) {
            return false;
        }
        return true;
    }

    public static String getSinger(File ksaFile) {
        if(!isKsaFile(ksaFile)) {
            return null;
        }
        getLineInFile(ksaFile, 3);
        return null;
    }

    public static String getComposer(File ksaFile) {
        if(!isKsaFile(ksaFile)) {
            return null;
        }
        getLineInFile(ksaFile, 2);
        return null;
    }

    public static String getSongName(File ksaFile) {
        if(!isKsaFile(ksaFile)) {
            return null;
        }
        getLineInFile(ksaFile, 0);
        return null;
    }

    private static boolean isKsaFile(File file) {
        return file.getName().toLowerCase().endsWith(".ksa");
    }

    private static String getLineInFile(File file, int index) {
        try {
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "euc-kr"));
            String line;

            int count = 0;
            while ((line = reader.readLine()) != null) {
                if(count == index) {
                    return line;
                }
                count++;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
