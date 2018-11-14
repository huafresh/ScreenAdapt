package com.hua.screen_adapt_core;

import android.util.Log;

/**
 * @author hua
 * @version V1.0
 * @date 2018/11/14 10:42
 */

class Util {
    private static final String LOG_TAG = "ScreenAdapt";
    static boolean debug = false;

    static void d(String msg) {
        if (debug) {
            Log.d(LOG_TAG, msg);
        }
    }

    static void e(String msg) {
        if (debug) {
            Log.e(LOG_TAG, msg);
        }
    }

}
