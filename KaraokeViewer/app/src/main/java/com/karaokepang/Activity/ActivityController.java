package com.karaokepang.Activity;

/**
 * Created by 1002230 on 16. 3. 17..
 */
public class ActivityController {
    private static ActivityController ourInstance = new ActivityController();

    public static ActivityController getInstance() {
        return ourInstance;
    }

    private ActivityController() {
    }


    private PangPangSelectActivity pangPangSelectActivity;
    private DuetSelectActivity duetSelectActivity;
    private PangPangActivity pangPangActivity;
    private DuetActivity duetActivity;


    public PangPangSelectActivity getPangPangSelectActivity() {
        return pangPangSelectActivity;
    }

    public void setPangPangSelectActivity(PangPangSelectActivity pangPangSelectActivity) {
        this.pangPangSelectActivity = pangPangSelectActivity;
    }

    public DuetSelectActivity getDuetSelectActivity() {
        return duetSelectActivity;
    }

    public void setDuetSelectActivity(DuetSelectActivity duetSelectActivity) {
        this.duetSelectActivity = duetSelectActivity;
    }

    public PangPangActivity getPangPangActivity() {
        return pangPangActivity;
    }

    public void setPangPangActivity(PangPangActivity pangPangActivity) {
        this.pangPangActivity = pangPangActivity;
    }

    public DuetActivity getDuetActivity() {
        return duetActivity;
    }

    public void setDuetActivity(DuetActivity duetActivity) {
        this.duetActivity = duetActivity;
    }
}
