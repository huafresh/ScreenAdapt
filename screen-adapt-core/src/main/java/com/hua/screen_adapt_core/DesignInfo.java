package com.hua.screen_adapt_core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * @author hua
 * @version 2018/9/19 16:47
 */

class DesignInfo {
    static DesignInfo designInfo;
    private static final String KEY_DESIGN_WIDTH = "design_width_in_px";
    private static final String KEY_DESIGN_HEIGHT = "design_height_in_px";
    int designWidth;
    int designHeight;

    static void init(Context context) {
        designInfo = new DesignInfo();
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo applicationInfo;
        try {
            applicationInfo = packageManager.getApplicationInfo(context
                    .getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo != null && applicationInfo.metaData != null) {
                designInfo.designWidth = (int) applicationInfo.metaData.get(KEY_DESIGN_WIDTH);
                designInfo.designHeight = (int) applicationInfo.metaData.get(KEY_DESIGN_HEIGHT);
            }
        } catch (Exception e) {
            throw new RuntimeException("you must config \"design_width_px\" and " +
                    "\"design_height_px\" in your androidManifest.xml");
        }
    }
}
