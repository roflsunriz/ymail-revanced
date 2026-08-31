package app.revanced.extension.ymail;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class AdViewCollapser implements Application.ActivityLifecycleCallbacks {
    private static final Object PLACEHOLDER_TAG = new Object();

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
        if (view.getTag() == PLACEHOLDER_TAG) return;
        if (isBlockedView(view)) {
            if (TargetViewClassifier.shouldDetachPromotion(resourceName(view))) {
                detachWithPlaceholder(view);
            } else {
                collapse(view);
            }
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
        String resourceName = resourceName(view);
        if (TargetViewClassifier.isBlockedResourceName(resourceName)) return true;
        if (!(view instanceof ViewGroup)) return false;

        Set<String> childNames = new HashSet<>();
        ViewGroup group = (ViewGroup) view;
        for (int index = 0; index < group.getChildCount(); index++) {
            String childName = resourceName(group.getChildAt(index));
            if (childName != null) childNames.add(childName);
        }
        Set<String> ancestorNames = new HashSet<>();
        ViewParent parent = view.getParent();
        while (parent instanceof View) {
            String ancestorName = resourceName((View) parent);
            if (ancestorName != null) ancestorNames.add(ancestorName);
            parent = parent.getParent();
        }
        return TargetViewClassifier.isBlockedHierarchy(resourceName, ancestorNames, childNames);
    }

    private static String resourceName(View view) {
        if (view.getId() == View.NO_ID) return null;
        try {
            return view.getResources().getResourceEntryName(view.getId());
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static void collapse(View view) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            boolean layoutChanged = params.width != 0 || params.height != 0;
            params.width = 0;
            params.height = 0;
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) params;
                layoutChanged |= margins.leftMargin != 0 || margins.topMargin != 0
                        || margins.rightMargin != 0 || margins.bottomMargin != 0;
                margins.setMargins(0, 0, 0, 0);
            }
            if (layoutChanged) view.setLayoutParams(params);
        }
        if (view.getMinimumWidth() != 0) view.setMinimumWidth(0);
        if (view.getMinimumHeight() != 0) view.setMinimumHeight(0);
        if (view.getPaddingLeft() != 0 || view.getPaddingTop() != 0
                || view.getPaddingRight() != 0 || view.getPaddingBottom() != 0) {
            view.setPadding(0, 0, 0, 0);
        }
        if (view.getVisibility() != View.GONE) view.setVisibility(View.GONE);
    }

    private static void detachWithPlaceholder(View view) {
        ViewParent parent = view.getParent();
        if (!(parent instanceof ViewGroup)) {
            collapse(view);
            return;
        }

        ViewGroup group = (ViewGroup) parent;
        int index = group.indexOfChild(view);
        if (index < 0) {
            collapse(view);
            return;
        }

        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null) {
            params.width = 0;
            params.height = 0;
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) params).setMargins(0, 0, 0, 0);
            }
        }

        View placeholder = new View(view.getContext());
        placeholder.setId(view.getId());
        placeholder.setTag(PLACEHOLDER_TAG);
        placeholder.setVisibility(View.GONE);
        group.removeViewAt(index);
        view.setId(View.NO_ID);
        if (params == null) {
            group.addView(placeholder, index, new ViewGroup.LayoutParams(0, 0));
        } else {
            group.addView(placeholder, index, params);
        }
    }

    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityPaused(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
}
