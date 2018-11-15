package com.hua.screen_adapt_core;

import android.util.DisplayMetrics;

/**
 * @author hua
 * @version V1.0
 * @date 2018/11/15 10:06
 */

class AdaptServiceImpl implements IAdaptService {

    private DisplayMetricsInfo adaptInfo;

    AdaptServiceImpl(DisplayMetricsInfo adaptInfo) {
        this.adaptInfo = adaptInfo;
    }

    @Override
    public void adapt(DisplayMetrics displayMetrics) {
        adaptInfo.restore(displayMetrics);
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
        return (int) ((dp * adaptInfo.density) + 0.5f);
    }

    @Override
    public int sp2px(float sp) {
        return (int) (sp * adaptInfo.scaledDensity + 0.5f);
    }
}
