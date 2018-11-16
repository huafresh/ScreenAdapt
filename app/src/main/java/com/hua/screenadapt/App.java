package com.hua.screenadapt;

import android.app.Application;

import com.hua.screen_adapt_core.ScreenAdaptManager;

/**
 * @author hua
 * @version V1.0
 * @date 2018/11/15 16:24
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ScreenAdaptManager.get().init(this);
        ScreenAdaptManager.get().enableDebug();
    }
}
