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
    public static boolean sGlobalEnable = true;
    public static final int DP_WIDTH_SP_WIDTH = 0;
    public static final int DP_WIDTH_SP_HEIGHT = 1;
    public static final int DP_HEIGHT_SP_WIDTH = 2;
    public static final int DP_HEIGHT_SP_HEIGHT = 3;
    private static final String KEY_ADAPT_ENABLE = "key_has_set_factory";
    private static final String KEY_ADAPT_BASE_DIMEN = "key_adapt_base_dimen";
    private Context context;
    private DisplayMetricsInfo originDisplayInfo;
    private SparseArray<IAdaptService> adaptServices;
    private List<AttrType> attrTypes = new ArrayList<>();
    private static boolean initialization = false;
    private static Field sFactorySetField;
    private boolean adaptEnable = false;


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

    private void ensureInitialization() {
        if (!initialization) {
            throw new IllegalStateException("call init() first");
        }
    }

    public void init(Application application) {
        if (!initialization) {
            this.context = application;
            originDisplayInfo = new DisplayMetricsInfo();
            originDisplayInfo.save(application.getResources().getDisplayMetrics());
            application.registerActivityLifecycleCallbacks(new ActivityCallback());
            application.registerComponentCallbacks(new ConfigChangeCallback());
            DesignInfo.init(context);
            ensureAdaptService();
            forceSetFactoryIfNeed(LayoutInflater.from(application), new AppInflaterFactory());
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
        this.adapt(activity, DP_WIDTH_SP_HEIGHT);
    }

    /**
     * 使activity使能屏幕适配
     *
     * @param activity  Activity
     * @param baseDimen 适配维度
     */
    public void adapt(Activity activity, int baseDimen) {
        ensureInitialization();
        Intent intent = activity.getIntent();
        intent.putExtra(KEY_ADAPT_ENABLE, true);
        intent.putExtra(KEY_ADAPT_BASE_DIMEN, baseDimen);
    }

    private static void forceSetFactoryIfNeed(LayoutInflater layoutInflater, LayoutInflater.Factory2 factory) {
        if (layoutInflater.getFactory() == null) {
            layoutInflater.setFactory2(factory);
            return;
        }

        long before = System.currentTimeMillis();

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
        layoutInflater.setFactory2(factory);

        long cost = System.currentTimeMillis() - before;
        Util.d("force set factory cost time = " + cost + "ms.");
    }

    public void adapt(final View view) {
        adapt(view, DP_WIDTH_SP_HEIGHT);
    }

    /**
     * 这个方法的适配方案是根据View的
     * 各种布局参数反推原来的dp值，然后再用设计图的尺寸重新计算每个布局参数，最后设置回去。
     * 目前只对常见的布局参数做了处理，比如：宽高、margin、padding。
     * 可以调用{@link #addAttrType(AttrType)}进行扩展，参考{@link AttrType}。
     * <p>
     * 这个方法注意不要调用多次。一般来说，在onCreate中find出View然后调用此方法最佳。
     *
     * @param view      传ViewGroup，子View会遍历适配。
     * @param baseDimen 适配维度
     */
    public void adapt(final View view, final int baseDimen) {
        ensureInitialization();

        if ("true".equals(view.getTag(R.id.key_already_adapt))) {
            Util.d("view = " + view + " already adapt, ignore!!!");
            return;
        }

        view.setTag(R.id.key_already_adapt, "true");

        List<View> views = new ArrayList<>();
        collectViews(view, views);
        IAdaptService base = adaptServices.get(baseDimen);
        for (View v : views) {
            for (AttrType attrType : attrTypes) {
                attrType.adapt(v, base);
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            v.setLayoutParams(params);
        }
    }

    public int dp2px(Context context, int dp) {
        return dp2px(context, dp, DP_WIDTH_SP_HEIGHT);
    }

    public int dp2px(Context context, int dp, int base) {
        ensureInitialization();
        IAdaptService iBaseDimen = adaptServices.get(base);
        return iBaseDimen.dp2px(dp);
    }

    public int sp2px(Context context, int sp) {
        return sp2px(context, sp, DP_WIDTH_SP_HEIGHT);
    }

    public int sp2px(Context context, int sp, int base) {
        ensureInitialization();
        IAdaptService iBaseDimen = adaptServices.get(base);
        return iBaseDimen.sp2px(sp);
    }

    public void enableAdapt() {
        enableAdapt(DP_WIDTH_SP_HEIGHT);
    }

    public void enableAdapt(int base) {
        if (!adaptEnable && sGlobalEnable) {
            IAdaptService adaptService = adaptServices.get(base);
            if (adaptService == null) {
                Util.e("attempt enable adapt with a bad base = " + base +
                        ". change to default base: DP_WIDTH_SP_HEIGHT");
                base = DP_WIDTH_SP_HEIGHT;
                adaptService = adaptServices.get(base);
            }
            adaptService.adapt(context.getResources().getDisplayMetrics());
            adaptEnable = true;
            Util.d("enable screen adapt successful. base = " + getBaseStringByInt(base));
        }
    }

    public boolean getEnableState() {
        return adaptEnable;
    }

    private static String getBaseStringByInt(int base) {
        switch (base) {
            case DP_WIDTH_SP_WIDTH:
                return "DP_WIDTH_SP_WIDTH";
            case DP_WIDTH_SP_HEIGHT:
                return "DP_WIDTH_SP_HEIGHT";
            case DP_HEIGHT_SP_WIDTH:
                return "DP_HEIGHT_SP_WIDTH";
            case DP_HEIGHT_SP_HEIGHT:
                return "DP_HEIGHT_SP_HEIGHT";
            default:
                return "invalid base value";
        }
    }

    public void cancelAdapt() {
        if (adaptEnable) {
            restoreSysMetricsInfo();
            adaptEnable = false;
            Util.d("cancel screen adapt successful.");
        }
    }

    private void ensureAdaptService() {
        if (adaptServices == null) {
            adaptServices = new SparseArray<>(4);
            IAdaptDimen dimen1 = new DpWidthSpWidth();
            IAdaptDimen dimen2 = new DpWidthSpHeight();
            IAdaptDimen dimen3 = new DpHeightSpWidth();
            IAdaptDimen dimen4 = new DpHeightSpHeight();
            adaptServices.put(dimen1.id(), new AdaptServiceImpl(dimen1.createAdaptInfo(originDisplayInfo)));
            adaptServices.put(dimen2.id(), new AdaptServiceImpl(dimen2.createAdaptInfo(originDisplayInfo)));
            adaptServices.put(dimen3.id(), new AdaptServiceImpl(dimen3.createAdaptInfo(originDisplayInfo)));
            adaptServices.put(dimen4.id(), new AdaptServiceImpl(dimen4.createAdaptInfo(originDisplayInfo)));
        }
    }

    private void restoreSysMetricsInfo() {
        originDisplayInfo.restore(context.getResources().getDisplayMetrics());
    }

    private class AppInflaterFactory implements LayoutInflater.Factory2 {

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            if (adaptEnable) {
                Util.d("use application LayoutInflater. so cancel screen adapt.");
                cancelAdapt();
            }
            return null;
        }

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return this.onCreateView(null, name, context, attrs);
        }
    }

    private class ActivityInflaterFactory extends AppInflaterFactory {
        private WeakReference<Activity> activity;
        private boolean wantEnable;
        private int baseDimen;

        private ActivityInflaterFactory(Activity activity) {
            this.activity = new WeakReference<Activity>(activity);
            Intent intent = activity.getIntent();
            this.baseDimen = intent.getIntExtra(KEY_ADAPT_BASE_DIMEN, DP_WIDTH_SP_HEIGHT);
        }

        private void getEnableState() {
            if (activity.get() != null) {
                Intent intent = activity.get().getIntent();
                if (intent != null) {
                    wantEnable = intent.getBooleanExtra(KEY_ADAPT_ENABLE, false);
                    baseDimen = intent.getIntExtra(KEY_ADAPT_BASE_DIMEN, DP_WIDTH_SP_HEIGHT);
                    return;
                }
            }

            this.wantEnable = false;
            this.baseDimen = DP_WIDTH_SP_HEIGHT;
        }

        @Override
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            getEnableState();
            if (activity.get() != null) {
                if (!adaptEnable) {
                    if (wantEnable) {
                        Util.d("activity = " +
                                activity.get().getClass().getCanonicalName() +
                                " enable screen adapt.");
                        enableAdapt(baseDimen);
                    }
                } else {
                    if (!wantEnable) {
                        Util.d("activity = " +
                                activity.get().getClass().getCanonicalName() +
                                " cancel screen adapt.");
                        cancelAdapt();
                    }
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

            forceSetFactoryIfNeed(activity.getLayoutInflater(), new ActivityInflaterFactory(activity));
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
                originDisplayInfo.scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
                adaptServices = null;
                ensureAdaptService();
                Util.d("font scale config has changed. recalculate adapt info");
            }
        }

        @Override
        public void onLowMemory() {

        }
    }

}
