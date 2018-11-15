package com.hua.screen_adapt_core;

import android.util.DisplayMetrics;

/**
 * @author hua
 * @version 2018/9/20 9:12
 */

class DpWidthSpWidth implements IAdaptDimen {

    @Override
    public int id() {
        return ScreenAdaptManager.DP_WIDTH_SP_WIDTH;
    }

    @Override
    public DisplayMetricsInfo createAdaptInfo(DisplayMetricsInfo origin) {
        DisplayMetricsInfo result = new DisplayMetricsInfo();
        result.density = origin.widthPixels * 1.0f / DesignInfo.designInfo.designWidth;
        result.densityDpi = (int) (result.density * 160);
        float ratio = origin.scaledDensity * 1.0f / origin.density;
        result.scaledDensity = ratio * result.density;
        return result;
    }
}
