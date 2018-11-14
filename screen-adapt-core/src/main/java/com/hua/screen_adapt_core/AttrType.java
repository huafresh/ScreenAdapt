package com.hua.screen_adapt_core;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hua
 * @version 1.0
 * @date 2018/11/3
 */
public abstract class AttrType {

    static List<AttrType> getDefaultAttrTypeList() {
        List<AttrType> list = new ArrayList<>();
        list.add(new WidthHeight());
        list.add(new Padding());
        list.add(new Margin());
        list.add(new TextSize());
        return list;
    }

    public static class WidthHeight extends AttrType {

        @Override
        public void adapt(View view, IAdaptDimen baseDimen) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams.width > 0) {
                layoutParams.width = baseDimen.getNewPxForDp(layoutParams.width);
            }
            if (layoutParams.height > 0) {
                layoutParams.height = baseDimen.getNewPxForDp(layoutParams.height);
            }
        }
    }

    public static class Padding extends AttrType {
        @Override
        public void adapt(View view, IAdaptDimen baseDimen) {
            int left = view.getPaddingLeft();
            int top = view.getPaddingTop();
            int right = view.getPaddingRight();
            int bottom = view.getPaddingBottom();

            view.setPadding(
                    baseDimen.getNewPxForDp(left),
                    baseDimen.getNewPxForDp(top),
                    baseDimen.getNewPxForDp(right),
                    baseDimen.getNewPxForDp(bottom)
            );
        }
    }

    public static class Margin extends AttrType {
        @Override
        public void adapt(View view, IAdaptDimen baseDimen) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                marginParams.leftMargin = baseDimen.getNewPxForDp(marginParams.leftMargin);
                marginParams.topMargin = baseDimen.getNewPxForDp(marginParams.topMargin);
                marginParams.rightMargin = baseDimen.getNewPxForDp(marginParams.rightMargin);
                marginParams.bottomMargin = baseDimen.getNewPxForDp(marginParams.bottomMargin);
            }
        }
    }

    public static class TextSize extends AttrType {

        @Override
        public void adapt(View view, IAdaptDimen baseDimen) {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseDimen.getNewPxForSp(textView.getTextSize()));
            }
        }
    }

    /**
     * 修改传入的View的布局参数，对特定的属性进行适配。
     * 在这里可以不用把LayoutParams设置回去，最后会统一设置。
     *
     * @param view View
     */
    public abstract void adapt(View view, IAdaptDimen baseDimen);

}
