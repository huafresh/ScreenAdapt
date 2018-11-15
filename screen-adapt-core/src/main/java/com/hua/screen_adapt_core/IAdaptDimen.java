package com.hua.screen_adapt_core;

import android.util.DisplayMetrics;

/**
 * 适配维度
 *
 * @author hua
 * @version 2018/9/20 9:08
 */

public interface IAdaptDimen {

    /**
     * 维度唯一标识
     *
     * @return id
     */
    int id();

    /**
     * 根据系统原来的DisplayMetricsInfo，创建给定维度的DisplayMetricsInfo
     *
     * @param origin 系统原来的DisplayMetricsInfo
     * @return 给定维度的DisplayMetricsInfo
     */
    DisplayMetricsInfo createAdaptInfo(DisplayMetricsInfo origin);
}
