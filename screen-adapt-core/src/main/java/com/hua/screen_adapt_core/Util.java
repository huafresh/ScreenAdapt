package com.hua.screen_adapt_core;

import android.util.Log;

/**
 * @author hua
 * @version V1.0
 * @date 2018/11/14 10:42
 */

class Util {

    static boolean debug = false;

    static void d(String msg) {
        if (debug) {
            Log.d("ScreenAdapt: ", msg);
        }
    }

    static void e(String msg) {
        if (debug) {
            Log.e("ScreenAdapt: ", msg);
        }
    }

}
