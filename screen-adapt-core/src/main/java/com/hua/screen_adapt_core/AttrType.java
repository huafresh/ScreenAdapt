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
        public void adapt(View view, IAdaptService service) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams.width > 0) {
                layoutParams.width = service.getNewPxForDp(layoutParams.width);
            }
            if (layoutParams.height > 0) {
                layoutParams.height = service.getNewPxForDp(layoutParams.height);
            }
        }
    }

    public static class Padding extends AttrType {
        @Override
        public void adapt(View view, IAdaptService service) {
            int left = view.getPaddingLeft();
            int top = view.getPaddingTop();
            int right = view.getPaddingRight();
            int bottom = view.getPaddingBottom();

            view.setPadding(
                    service.getNewPxForDp(left),
                    service.getNewPxForDp(top),
                    service.getNewPxForDp(right),
                    service.getNewPxForDp(bottom)
            );
        }
    }

    public static class Margin extends AttrType {
        @Override
        public void adapt(View view, IAdaptService service) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
                marginParams.leftMargin = service.getNewPxForDp(marginParams.leftMargin);
                marginParams.topMargin = service.getNewPxForDp(marginParams.topMargin);
                marginParams.rightMargin = service.getNewPxForDp(marginParams.rightMargin);
                marginParams.bottomMargin = service.getNewPxForDp(marginParams.bottomMargin);
            }
        }
    }

    public static class TextSize extends AttrType {

        @Override
        public void adapt(View view, IAdaptService service) {
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, service.getNewPxForSp(textView.getTextSize()));
            }
        }
    }

    /**
     * 修改传入的View的布局参数，对特定的属性进行适配。
     * 在这里可以不用把LayoutParams设置回去，最后会统一设置。
     *
     * @param view View
     */
    public abstract void adapt(View view, IAdaptService service);

}
