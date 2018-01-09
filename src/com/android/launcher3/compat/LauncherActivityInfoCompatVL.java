package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * Created by uchia on 2018/1/8.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LauncherActivityInfoCompatVL extends LauncherActivityInfoCompat {
    private LauncherActivityInfo mLauncherActivityInfo;

    LauncherActivityInfoCompatVL(LauncherActivityInfo launcherActivityInfo) {
        super();
        mLauncherActivityInfo = launcherActivityInfo;
    }

    public ComponentName getComponentName() {
        return mLauncherActivityInfo.getComponentName();
    }

    public UserHandleCompat getUser() {
        return UserHandleCompat.fromUser(mLauncherActivityInfo.getUser());
    }

    public CharSequence getLabel() {
        return mLauncherActivityInfo.getLabel();
    }

    public Drawable getIcon(int density) {
        return mLauncherActivityInfo.getIcon(density);
    }

    public ApplicationInfo getApplicationInfo() {
        return mLauncherActivityInfo.getApplicationInfo();
    }

    public long getFirstInstallTime() {
        return mLauncherActivityInfo.getFirstInstallTime();
    }

    public Drawable getBadgedIcon(int density) {
        return mLauncherActivityInfo.getBadgedIcon(density);
    }

    @Override
    public LauncherActivityInfo getRawInfo() {
        return mLauncherActivityInfo;
    }
}
