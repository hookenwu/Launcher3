package com.android.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by uchia on 2018/1/8.
 */

public abstract class LauncherActivityInfoCompat {
    LauncherActivityInfoCompat() {
    }

    public abstract ComponentName getComponentName();
    public abstract UserHandleCompat getUser();
    public abstract CharSequence getLabel();
    public abstract Drawable getIcon(int density);
    public abstract ApplicationInfo getApplicationInfo();
    public abstract long getFirstInstallTime();
    public abstract Drawable getBadgedIcon(int density);

    public abstract LauncherActivityInfo getRawInfo();

    /**
     * Creates a LauncherActivityInfoCompat for the primary user.
     */
    public static LauncherActivityInfoCompat fromResolveInfo(ResolveInfo info, Context context) {
        return new LauncherActivityInfoCompatV16(context, info);
    }
}
