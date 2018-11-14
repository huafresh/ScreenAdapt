package com.hua.screen_adapt_core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 屏幕适配分为两种情况讨论：
 * <p>
 * 1、如果页面刚开始开发，则可以调用{@link #adapt(Activity)}对整个页面进行适配。
 * <p>
 * 2、如果是局部UI调整，一般来说，只需要按之前设计时的比例就行了，虽然会偏大or
 * 偏小，但是页面整体看起来是可以的。但是可能会遇到局部UI也要精准匹配设计图的情况。
 * 这时候可以使用{@link #adapt(View)}方法进行适配。
 *
 * @author hua
 * @version 2018/9/19 10:34
 */

public class ScreenAdaptManager {

    private static final String KEY_ENABLE_ADAPT = "key_auto_adapt_enable_adapt";
    private static final String KEY_ADAPT_BASE_DIMEN = "key_adapt_base_dimen";
    private Context context;
    private DisplayMetricsInfo originDisplayInfo;
    private SparseArray<IAdaptDimen> adaptDimens;
    private List<AttrType> attrTypes = new ArrayList<>();
    private static boolean initialization = false;
    private static Field sFactorySetField;


    public static ScreenAdaptManager get() {
        return Holder.S_INSTANCE;
    }

    DisplayMetricsInfo getOriginInfo() {
        return originDisplayInfo;
    }

    private static final class Holder {
        @SuppressLint("StaticFieldLeak")
        private static final ScreenAdaptManager S_INSTANCE = new ScreenAdaptManager();
    }

    private ScreenAdaptManager() {
        attrTypes.addAll(AttrType.getDefaultAttrTypeList());
    }

    private void initialization(Application application) {
        if (!initialization) {
            this.context = application;
            originDisplayInfo = new DisplayMetricsInfo();
            originDisplayInfo.save(application.getResources().getDisplayMetrics());
//            setAdaptListener(new DefaultAdaptListener());
            application.registerActivityLifecycleCallbacks(new ActivityCallback());
            application.registerComponentCallbacks(new ConfigChangeCallback());
            DesignInfo.init(context);
            ensureAdaptDimen();
            initialization = true;
        }
    }

    public void enableDebug() {
        Util.debug = true;
    }

    public void disableDebug() {
        Util.debug = false;
    }


    /**
     * 使activity使能屏幕适配
     *
     * @param activity Activity
     */
    public void adapt(Activity activity) {
        this.adapt(activity, IAdaptDimen.DP_WIDTH_SP_HEIGHT);
    }

    /**
     * 使activity使能屏幕适配
     *
     * @param activity  Activity
     * @param baseDimen 适配维度
     */
    public void adapt(Activity activity, int baseDimen) {
        initialization(activity.getApplication());
        activity.getIntent().putExtra(KEY_ENABLE_ADAPT, true);
        activity.getIntent().putExtra(KEY_ADAPT_BASE_DIMEN, baseDimen);
    }

    private void setCustomFactory(Context context) {
        LayoutInflater.Factory2 customFactory;
        if (context instanceof Activity) {
            customFactory = new ActivityInflaterFactory((Activity) context);
        } else {
            customFactory = new AppInflaterFactory();
        }

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        LayoutInflater.Factory factory = layoutInflater.getFactory();
        if (factory == null) {
            layoutInflater.setFactory2(customFactory);
        } else if (!(factory instanceof AppInflaterFactory)) {
            //factory只能设置一次，这里反射改变这一点。
            if (sFactorySetField == null) {
                try {
                    sFactorySetField = LayoutInflater.class.getDeclaredField("mFactorySet");
                    sFactorySetField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    Util.e("reflect get \"mFactorySet\" field failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (sFactorySetField != null) {
                try {
                    sFactorySetField.set(layoutInflater, false);
                } catch (IllegalAccessException e) {
                    Util.e("reflect set \"mFactorySet\" field value failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            //LayoutInflater会把新旧的factory合并，因此不用担心覆盖。
            layoutInflater.setFactory2(customFactory);
        }
    }

    public void adapt(final View view) {
        adapt(view, IAdaptDimen.DP_WIDTH_SP_HEIGHT);
    }

    /**
     * 这个方法的适配方案是根据View的
     * 各种属性值反推原来的dp值，然后再用设计图的尺寸重新计算每个属性值，最后设置回去。
     * 目前只对常见的属性做了处理，可以调用{@link #addAttrType(AttrType)}进行扩展，
     * 参考{@link AttrType}。
     * <p>
     * 这个方法注意不要调用多次。一般来说，在onCreate中find出View然后调用此方法最佳。
     *
     * @param view      传ViewGroup，子View会遍历适配。
     * @param baseDimen 适配维度
     */
    public void adapt(final View view, final int baseDimen) {
        initialization((Application) view.getContext().getApplicationContext());
        view.post(new Runnable() {
            @Override
            public void run() {
                List<View> views = new ArrayList<>();
                collectViews(view, views);

                IAdaptDimen base = adaptDimens.get(baseDimen);
                for (View v : views) {
                    for (AttrType attrType : attrTypes) {
                        attrType.adapt(v, base);
                    }
                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    v.setLayoutParams(params);
                }
            }
        });
    }

    public int dp2px(Context context, int dp) {
        return dp2px(context, dp, IAdaptDimen.DP_WIDTH_SP_HEIGHT);
    }

    public int dp2px(Context context, int dp, int base) {
        initialization((Application) context.getApplicationContext());
        IAdaptDimen iBaseDimen = adaptDimens.get(base);
        return iBaseDimen.dp2px(dp);
    }

    public int sp2px(Context context, int sp) {
        return sp2px(context, sp, IAdaptDimen.DP_WIDTH_SP_HEIGHT);
    }

    public int sp2px(Context context, int sp, int base) {
        initialization((Application) context.getApplicationContext());
        IAdaptDimen iBaseDimen = adaptDimens.get(base);
        return iBaseDimen.sp2px(sp);
    }

    private void ensureAdaptDimen() {
        if (adaptDimens == null) {
            adaptDimens = new SparseArray<>(4);
            adaptDimens.put(IAdaptDimen.DP_WIDTH_SP_WIDTH, new DpWidthSpWidth());
            adaptDimens.put(IAdaptDimen.DP_WIDTH_SP_HEIGHT, new DpWidthSpHeight());
            adaptDimens.put(IAdaptDimen.DP_HEIGHT_SP_WIDTH, new DpHeightSpWidth());
            adaptDimens.put(IAdaptDimen.DP_HEIGHT_SP_HEIGHT, new DpHeightSpHeight());
        }
    }

    private void restoreSysMetricsInfo() {
        originDisplayInfo.restore(context.getResources().getDisplayMetrics());
    }

//    private AdaptListener mAutoAdaptListener;
//
//    public interface AdaptListener {
//        /**
//         * execute adapt.
//         * 适配的原理就是在LayoutInflater创建View之前，动态的修改系统DisplayMetrics的值。
//         * 可以参考：https://mp.weixin.qq.com/s/d9QCoBP6kV9VSWvVldVVwA
//         *
//         * @param context    Context
//         * @param attrInfo   attr info resolved from xml, if not set, use default value.
//         * @param sysMetrics change this object base on {@code attrInfo}
//         */
//        void adapt(Context context, XmlAttrInfo attrInfo, DisplayMetrics sysMetrics);
//    }
//
//    public void setAdaptListener(AdaptListener listener) {
//        mAutoAdaptListener = listener;
//    }

//    private class DefaultAdaptListener implements AdaptListener {
//
//        @Override
//        public void adapt(Context context, XmlAttrInfo attrInfo, DisplayMetrics sysMetrics) {
//            if (!attrInfo.disable) {
//                IAdaptDimen baseDimen = adaptDimens.get(attrInfo.baseDimen);
//                if (baseDimen != null) {
//                    baseDimen.adapt(sysMetrics);
//                }
//            } else {
//                restoreSysMetricsInfo();
//            }
//        }
//    }

    private class AppInflaterFactory implements LayoutInflater.Factory2 {
        boolean hasEnable;

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            if (hasEnable) {
                restoreSysMetricsInfo();
                hasEnable = false;
            }
            return null;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return this.onCreateView(null, name, context, attrs);
        }
    }

    private class ActivityInflaterFactory extends AppInflaterFactory {
        private int baseDimen;
        private WeakReference<Activity> activity;
        private final boolean wantEnable;

        private ActivityInflaterFactory(Activity activity) {
            this.activity = new WeakReference<Activity>(activity);
            Intent intent = activity.getIntent();
            this.wantEnable = intent.getBooleanExtra(KEY_ENABLE_ADAPT, false);
            this.baseDimen = intent.getIntExtra(KEY_ADAPT_BASE_DIMEN, -1);
            this.hasEnable = false;
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            if (activity.get() != null) {
                if (!wantEnable) {
                    if (hasEnable) {
                        restoreSysMetricsInfo();
                        hasEnable = false;
                        Util.d("Disable screen adapt. Activity = " +
                                activity.get().getClass().getCanonicalName());
                    }
                } else if (!hasEnable) {
                    IAdaptDimen iBaseDimen = adaptDimens.get(baseDimen);
                    if (iBaseDimen == null) {
                        iBaseDimen = adaptDimens.get(IAdaptDimen.DP_WIDTH_SP_HEIGHT);
                        Util.e("bad baseDimen: " + baseDimen +
                                ". use default baseDimen. Activity = " +
                                activity.getClass().getCanonicalName());
                    }
                    iBaseDimen.adapt(context.getResources().getDisplayMetrics());
                    hasEnable = true;
                    Util.d("Enable screen adapt. Activity = " +
                            activity.get().getClass().getCanonicalName());
                }

            }
            return null;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return this.onCreateView(null, name, context, attrs);
        }
    }

    private class ActivityCallback implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            long before = System.currentTimeMillis();
            setCustomFactory(activity);
            long cost = System.currentTimeMillis() - before;
            Util.d("setCustomFactory cost time = " + cost +
                    "ms. Activity = " + activity.getClass().getCanonicalName());
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    private void collectViews(View view, List<View> result) {
        if (view instanceof ViewGroup) {
            result.add(view);
            int childCount = ((ViewGroup) view).getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                collectViews(child, result);
            }
        } else {
            result.add(view);
        }
    }

    public void addAttrType(AttrType attrType) {
        attrTypes.add(attrType);
    }

    private class ConfigChangeCallback implements ComponentCallbacks {

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            if (newConfig != null && newConfig.fontScale > 0) {
                restoreSysMetricsInfo();
                originDisplayInfo.scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
            }
        }

        @Override
        public void onLowMemory() {

        }
    }

}
