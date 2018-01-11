package com.android.launcher3.compat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.PackageUserKey;
import com.android.launcher3.util.Thunk;

import java.util.ArrayList;
import java.util.List;

/**
 * Version of {@link LauncherAppsCompat} for devices with API level 19.
 * Devices Pre-L don't support multiple profiles in one launcher so
 * user parameters are ignored and all methods operate on the current user.
 */

public class LauncherAppsCompatV16 extends LauncherAppsCompat {


    private PackageManager mPm;
    private Context mContext;
    private List<OnAppsChangedCallbackCompat> mCallbacks
            = new ArrayList<OnAppsChangedCallbackCompat>();
    private PackageMonitor mPackageMonitor;

    LauncherAppsCompatV16(Context context) {
        mPm = context.getPackageManager();
        mContext = context;
        mPackageMonitor = new PackageMonitor();
    }


    @Override
    public List<LauncherActivityInfoCompat> getActivityList(String packageName, UserHandle user) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);
        List<ResolveInfo> infos = mPm.queryIntentActivities(mainIntent, 0);
        List<LauncherActivityInfoCompat> list =
                new ArrayList<LauncherActivityInfoCompat>(infos.size());
        for (ResolveInfo info : infos) {
            list.add(new LauncherActivityInfoCompatV16(mContext, info));
        }
        return list;
    }

    @Override
    public LauncherActivityInfoCompat resolveActivity(Intent intent, UserHandle user) {
        ResolveInfo info = mPm.resolveActivity(intent, 0);
        if (info != null) {
            return new LauncherActivityInfoCompatV16(mContext, info);
        }
        return null;
    }

    @Override
    public void startActivityForProfile(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        Intent launchIntent = new Intent(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        launchIntent.setComponent(component);
        launchIntent.setSourceBounds(sourceBounds);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(launchIntent, opts);
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags, UserHandle user) {
        final boolean isPrimaryUser = Process.myUserHandle().equals(user);
        if (!isPrimaryUser && (flags == 0)) {
            // We are looking for an installed app on a secondary profile. Prior to O, the only
            // entry point for work profiles is through the LauncherActivity.
            List<LauncherActivityInfoCompat> activityList =
                    getActivityList(packageName, user);
            return activityList.size() > 0 ? activityList.get(0).getApplicationInfo() : null;
        }
        try {
            ApplicationInfo info =
                    mContext.getPackageManager().getApplicationInfo(packageName, flags);
            // There is no way to check if the app is installed for managed profile. But for
            // primary profile, we can still have this check.
            if (isPrimaryUser && ((info.flags & ApplicationInfo.FLAG_INSTALLED) == 0)
                    || !info.enabled) {
                return null;
            }
            return info;
        } catch (PackageManager.NameNotFoundException e) {
            // Package not found
            return null;
        }
    }

    @Override
    public void showAppDetailsForProfile(ComponentName component, UserHandle user, Rect sourceBounds, Bundle opts) {
        String packageName = component.getPackageName();
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        mContext.startActivity(intent, null);
    }

    public synchronized void addOnAppsChangedCallback(OnAppsChangedCallbackCompat callback) {
        if (callback != null && !mCallbacks.contains(callback)) {
            mCallbacks.add(callback);
            if (mCallbacks.size() == 1) {
                registerForPackageIntents();
            }
        }
    }

    public synchronized void removeOnAppsChangedCallback(OnAppsChangedCallbackCompat callback) {
        mCallbacks.remove(callback);
        if (mCallbacks.size() == 0) {
            unregisterForPackageIntents();
        }
    }

    private void unregisterForPackageIntents() {
        mContext.unregisterReceiver(mPackageMonitor);
    }

    private void registerForPackageIntents() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mPackageMonitor, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        mContext.registerReceiver(mPackageMonitor, filter);
    }

    @Override
    public boolean isPackageEnabledForProfile(String packageName, UserHandle user) {
        return isAppEnabled(mPm, packageName, 0);
    }

    @Override
    public boolean isActivityEnabledForProfile(ComponentName component, UserHandle user) {
        try {
            ActivityInfo info = mPm.getActivityInfo(component, 0);
            return info != null && info.isEnabled();
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<ShortcutConfigActivityInfo> getCustomShortcutActivityList(@Nullable PackageUserKey packageUser) {
        List<ShortcutConfigActivityInfo> result = new ArrayList<>();
        if (packageUser != null && !packageUser.mUser.equals(Process.myUserHandle())) {
            return result;
        }
        PackageManager pm = mContext.getPackageManager();
        for (ResolveInfo info :
                pm.queryIntentActivities(new Intent(Intent.ACTION_CREATE_SHORTCUT), 0)) {
            if (packageUser == null || packageUser.mPackageName
                    .equals(info.activityInfo.packageName)) {
                result.add(new ShortcutConfigActivityInfo.ShortcutConfigActivityInfoVL(info.activityInfo, pm));
            }
        }
        return result;
    }

    @Override
    public void pinShortcuts(String packageName, List<String> shortcutIds, UserHandle user) {

    }

    @Override
    public void startShortcut(ShortcutInfo shortcut,
                              Rect sourceBounds,
                              Bundle startActivityOptions) {

    }

    @Override
    public void startShortcut(String packageName,
                              String shortcutId,
                              Rect sourceBounds,
                              Bundle startActivityOptions,
                              UserHandle user) {

    }

    @Override
    public Drawable getShortcutIconDrawable(ShortcutInfo shortcut, int density) {
        return null;
    }

    @Override
    public List<ShortcutInfo> getShortcuts(LauncherApps.ShortcutQuery query, UserHandle user) {
        return null;
    }

    @Override
    public boolean hasShortcutHostPermission() {
        return false;
    }


    @Thunk synchronized List<OnAppsChangedCallbackCompat> getCallbacks() {
        return new ArrayList<OnAppsChangedCallbackCompat>(mCallbacks);
    }


    @Thunk class PackageMonitor extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final UserHandleCompat user = UserHandleCompat.myUserHandle();

            if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
                    || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                    || Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                final String packageName = intent.getData().getSchemeSpecificPart();
                final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                if (packageName == null || packageName.length() == 0) {
                    // they sent us a bad intent
                    return;
                }
                if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                    for (OnAppsChangedCallbackCompat callback : getCallbacks()) {
                        callback.onPackageChanged(packageName, user.getUser());
                    }
                } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                    if (!replacing) {
                        for (OnAppsChangedCallbackCompat callback : getCallbacks()) {
                            callback.onPackageRemoved(packageName, user.getUser());
                        }
                    }
                    // else, we are replacing the package, so a PACKAGE_ADDED will be sent
                    // later, we will update the package at this time
                } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    if (!replacing) {
                        for (OnAppsChangedCallbackCompat callback : getCallbacks()) {
                            callback.onPackageAdded(packageName, user.getUser());
                        }
                    } else {
                        for (OnAppsChangedCallbackCompat callback : getCallbacks()) {
                            callback.onPackageChanged(packageName, user.getUser());
                        }
                    }
                }
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
                // EXTRA_REPLACING is available Kitkat onwards. For lower devices, it is broadcasted
                // when moving a package or mounting/un-mounting external storage. Assume that
                // it is a replacing operation.
                final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING,
                        !Utilities.ATLEAST_KITKAT);
                String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                for (OnAppsChangedCallbackCompat callback : getCallbacks()) {
                    callback.onPackagesAvailable(packages, user.getUser(), replacing);
                }
            } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                // This intent is broadcasted when moving a package or mounting/un-mounting
                // external storage.
                // However on Kitkat this is also sent when a package is being updated, and
                // contains an extra Intent.EXTRA_REPLACING=true for that case.
                // Using false as default for Intent.EXTRA_REPLACING gives correct value on
                // lower devices as the intent is not sent when the app is updating/replacing.
                final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                String[] packages = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
                for (OnAppsChangedCallbackCompat callback : getCallbacks()) {
                    callback.onPackagesUnavailable(packages, user.getUser(), replacing);
                }
            }
        }
    }

    public boolean isAppEnabled(PackageManager pm, String packageName, int flags) {
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, flags);
            return info != null && info.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}