package com.hua.screen_adapt_core;

import android.util.DisplayMetrics;

/**
 * @author hua
 * @version 2018/9/20 9:12
 */

class DpHeightSpHeight implements IAdaptDimen {
    private DisplayMetricsInfo result;

    DpHeightSpHeight() {
        DisplayMetricsInfo origin = ScreenAdaptManager.get().getOriginInfo();
        result = new DisplayMetricsInfo();
        result.density = origin.heightPixels * 1.0f / DesignInfo.designInfo.designHeight;
        result.densityDpi = (int) (result.density * 160);
        float ratio = origin.scaledDensity * 1.0f / origin.density;
        result.scaledDensity = ratio * result.density;
    }

    @Override
    public void adapt(DisplayMetrics displayMetrics) {
        result.restore(displayMetrics);
    }

    @Override
    public int getNewPxForDp(int curPx) {
        DisplayMetricsInfo origin = ScreenAdaptManager.get().getOriginInfo();
        float dp = curPx * 1.0f / origin.density;
        return dp2px(dp);
    }

    @Override
    public int getNewPxForSp(float curPx) {
        DisplayMetricsInfo origin = ScreenAdaptManager.get().getOriginInfo();
        float sp = curPx * 1.0f / origin.scaledDensity;
        return sp2px(sp);
    }

    @Override
    public int dp2px(float dp) {
        return (int) ((dp * result.density) + 0.5f);
    }

    @Override
    public int sp2px(float sp) {
        return (int) (sp * result.scaledDensity + 0.5f);
    }
}
