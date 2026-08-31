package app.revanced.extension.ymail;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class AdViewCollapser implements Application.ActivityLifecycleCallbacks {
    private static final Map<Application, AdViewCollapser> INSTANCES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private final Map<Activity, ViewTreeObserver.OnGlobalLayoutListener> listeners =
            Collections.synchronizedMap(new WeakHashMap<>());

    private AdViewCollapser() {
    }

    public static void install(Application application) {
        synchronized (INSTANCES) {
            if (INSTANCES.containsKey(application)) return;
            AdViewCollapser collapser = new AdViewCollapser();
            INSTANCES.put(application, collapser);
            application.registerActivityLifecycleCallbacks(collapser);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle state) {
        finishBlockedActivity(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (finishBlockedActivity(activity)) return;
        View root = activity.getWindow().getDecorView();
        collapseRecursively(root);
        synchronized (listeners) {
            if (listeners.containsKey(activity)) return;
            ViewTreeObserver.OnGlobalLayoutListener listener = () -> collapseRecursively(root);
            listeners.put(activity, listener);
            root.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ViewTreeObserver.OnGlobalLayoutListener listener = listeners.remove(activity);
        if (listener == null) return;
        View root = activity.getWindow().getDecorView();
        if (root.getViewTreeObserver().isAlive()) {
            root.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    private static boolean finishBlockedActivity(Activity activity) {
        if (!TargetViewClassifier.isBlockedActivityClass(activity.getClass().getName())) return false;
        activity.finish();
        return true;
    }

    private static void collapseRecursively(View view) {
        if (isBlockedView(view)) {
            collapse(view);
            return;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                collapseRecursively(group.getChildAt(index));
            }
        }
    }

    private static boolean isBlockedView(View view) {
        if (TargetViewClassifier.isBlockedViewClass(view.getClass().getName())) return true;
        if (view.getId() == View.NO_ID) return false;
        try {
            return TargetViewClassifier.isBlockedResourceName(
                    view.getResources().getResourceEntryName(view.getId()));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static void collapse(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params.width = 0;
            params.height = 0;
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 0);
            }
            view.setLayoutParams(params);
        }
        view.setMinimumWidth(0);
        view.setMinimumHeight(0);
        view.setPadding(0, 0, 0, 0);
        view.setVisibility(View.GONE);
    }

    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
}
