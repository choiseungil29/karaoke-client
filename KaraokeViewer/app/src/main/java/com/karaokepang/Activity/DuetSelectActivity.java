package com.karaokepang.Activity;

import com.karaokepang.R;

import org.androidannotations.annotations.EActivity;

/**
 * Created by clogic on 16. 3. 17..
 */
@EActivity(R.layout.activity_select_duet)
public class DuetSelectActivity extends SelectActivity {

    private ActivityController activityController = ActivityController.getInstance();
    @Override
    public void afterViews() {
        super.afterViews();
        activityController.setDuetSelectActivity(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityController.setDuetSelectActivity(null);
    }
}
