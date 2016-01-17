package karaokeclicker.kkk.com.karaokeclicker.activity;

import com.orm.SugarContext;

/**
 * Created by 1002230 on 16. 1. 15..
 */
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SugarContext.init(getApplicationContext());
    }
}
