package com.hua.screen_adapt_core;

import android.util.DisplayMetrics;

/**
 * 适配维度
 *
 * @author hua
 * @version 2018/9/20 9:08
 */

public interface IAdaptDimen {
    int DP_WIDTH_SP_WIDTH = 0;
    int DP_WIDTH_SP_HEIGHT = 1;
    int DP_HEIGHT_SP_WIDTH = 2;
    int DP_HEIGHT_SP_HEIGHT = 3;

    /**
     * 执行适配
     *
     * @param displayMetrics inflate display metrics into this object
     */
    void adapt(DisplayMetrics displayMetrics);

    /**
     * 根据当前的px值，获取适配后的px值。
     * 换算基于dp
     *
     * @param curPx px before adapt
     * @return px after adapt
     */
    int getNewPxForDp(int curPx);

    /**
     * 根据当前的px值，获取适配后的px值。
     * 换算基于sp
     *
     * @param curPx px before adapt
     * @return px after adapt
     */
    int getNewPxForSp(float curPx);

    /**
     * dp转px
     *
     * @param dp dp value
     * @return px value
     */
    int dp2px(float dp);

    /**
     * sp转px
     *
     * @param sp sp value
     * @return px value
     */
    int sp2px(float sp);
}
