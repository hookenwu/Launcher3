package com.android.launcher3.compat;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Created by uchia on 2018/1/8.
 */

public class LauncherActivityInfoCompatV16 extends LauncherActivityInfoCompat {
    private final ResolveInfo mResolveInfo;
    private final ActivityInfo mActivityInfo;
    private final ComponentName mComponentName;
    private final PackageManager mPm;

    LauncherActivityInfoCompatV16(Context context, ResolveInfo info) {
        super();
        mResolveInfo = info;
        mActivityInfo = info.activityInfo;
        mComponentName = new ComponentName(mActivityInfo.packageName, mActivityInfo.name);
        mPm = context.getPackageManager();
    }

    public ComponentName getComponentName() {
        return mComponentName;
    }

    public UserHandleCompat getUser() {
        return UserHandleCompat.myUserHandle();
    }

    public CharSequence getLabel() {
        return mResolveInfo.loadLabel(mPm);
    }

    public Drawable getIcon(int density) {
        int iconRes = mResolveInfo.getIconResource();
        Resources resources = null;
        Drawable icon = null;
        // Get the preferred density icon from the app's resources
        if (density != 0 && iconRes != 0) {
            try {
                resources = mPm.getResourcesForApplication(mActivityInfo.applicationInfo);
                icon = resources.getDrawableForDensity(iconRes, density);
            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException exc) {
            }
        }
        // Get the default density icon
        if (icon == null) {
            icon = mResolveInfo.loadIcon(mPm);
        }
        if (icon == null) {
            resources = Resources.getSystem();
            icon = resources.getDrawableForDensity(android.R.mipmap.sym_def_app_icon, density);
        }
        return icon;
    }

    public ApplicationInfo getApplicationInfo() {
        return mActivityInfo.applicationInfo;
    }

    public long getFirstInstallTime() {
        try {
            PackageInfo info = mPm.getPackageInfo(mActivityInfo.packageName, 0);
            return info != null ? info.firstInstallTime : 0;
        } catch (PackageManager.NameNotFoundException e) {
            return 0;
        }
    }

    public String getName() {
        return mActivityInfo.name;
    }

    public Drawable getBadgedIcon(int density) {
        return getIcon(density);
    }

    @Override
    public LauncherActivityInfo getRawInfo() {
        return null;
    }
}
