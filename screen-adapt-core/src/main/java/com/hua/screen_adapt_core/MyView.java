package com.hua.screen_adapt_core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author hua
 * @version V1.0
 * @date 2018/11/14 10:53
 */

public class MyView extends View {
    public MyView(Context context) {
        this(context, null);
    }

    public MyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}
