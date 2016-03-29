package com.karaokepang;

/**
 * Created by clogic on 16. 3. 17..
 */

// 키값들을 Keys클래스에 정리한다
public class Keys {

    public static final String MODE = "mode";

    // 내부 클래스로 Value값들을 묶어둔다
    public static class Mode {
        public static final String PANGPANG = "vpang";
        public static final String DUET = "duet";
    }

    public static class SendData {
        public static final String START = "start";
        public static final String STOP = "stop";
        public static final String KEY_PLUS = "key_plus";
        public static final String KEY_MINUS = "key_minus";
        public static final String TEMPO_PLUS = "tempo_plus";
        public static final String TEMPO_MINUS = "tempo_minus";
        public static final String MUSIC_SHEET_MODE = "music_sheet_mode";
        public static final String MODE_VPANG = "mode_vpang";
        public static final String MODE_DUET = "mode_duet";
        public static final String MODE_AUDITION = "mode_audition";
        public static final String MODE_HOME = "mode_home";
        public static final String RESERVATION = "reservation";
        public static final String RESERVATION_CANCEL = "reservation_cancel";
        public static final String PLAYING = "playing";
    }
}
