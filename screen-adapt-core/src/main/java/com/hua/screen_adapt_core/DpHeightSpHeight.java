package com.hua.screen_adapt_core;

import android.util.DisplayMetrics;

/**
 * @author hua
 * @version 2018/9/20 9:12
 */

class DpHeightSpHeight implements IAdaptDimen {

    @Override
    public int id() {
        return ScreenAdaptManager.DP_HEIGHT_SP_HEIGHT;
    }

    @Override
    public DisplayMetricsInfo createAdaptInfo(DisplayMetricsInfo origin) {
        DisplayMetricsInfo result = new DisplayMetricsInfo();
        result.density = origin.heightPixels * 1.0f / DesignInfo.designInfo.designHeight;
        result.densityDpi = (int) (result.density * 160);
        float ratio = origin.scaledDensity * 1.0f / origin.density;
        result.scaledDensity = ratio * result.density;
        return result;
    }
}
