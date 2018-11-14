package com.hua.screen_adapt_core;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;


/**
 * @author hua
 * @version 2018/9/19 18:47
 */

public class XmlAttrInfo {

    public boolean disable;
    public int baseDimen;

    static XmlAttrInfo resolveAttr(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoAdapt);
        boolean disable = ta.getBoolean(R.styleable.AutoAdapt_disable, false);
        int baseDimen = ta.getInt(R.styleable.AutoAdapt_baseDimen, -1);
        ta.recycle();

        XmlAttrInfo attrInfo = new XmlAttrInfo();
        attrInfo.disable = disable;
        attrInfo.baseDimen = baseDimen;

        return attrInfo;
    }

}
