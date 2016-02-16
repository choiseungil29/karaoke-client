package com.karaokepang.Activity;

import java.util.ArrayList;

public interface MusicListener {
    public void notifyMeasureChanged(ArrayList<String> lyrics, long tick);
    public void notifyCurrentTick(float tick, int term, int measureLength);
}
