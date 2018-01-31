package com.android.launcher3.compat;

import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.Nullable;

import com.android.launcher3.LauncherAppWidgetProviderInfo;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.PackageUserKey;

import java.util.HashMap;
import java.util.List;

/**
 * Created by uchia on 2018/1/9.
 */

public class AppWidgetManagerCompatV16 extends AppWidgetManagerCompat {

    AppWidgetManagerCompatV16(Context context) {
        super(context);
    }

    @Override
    public List<AppWidgetProviderInfo> getAllProviders(@Nullable PackageUserKey packageUser) {
        return mAppWidgetManager.getInstalledProviders();
    }

    @Override
    public boolean bindAppWidgetIdIfAllowed(int appWidgetId, AppWidgetProviderInfo info, Bundle options) {
        if (Utilities.ATLEAST_JB_MR1) {
            return mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider, options);
        } else {
            return mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, info.provider);
        }
    }

    @Override
    public LauncherAppWidgetProviderInfo findProvider(ComponentName provider, UserHandle user) {
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return null;
        }
        for (AppWidgetProviderInfo info :
                getAllProviders(new PackageUserKey(provider.getPackageName(), user))) {
            if (info.provider.equals(provider)) {
                return LauncherAppWidgetProviderInfo.fromProviderInfo(mContext, info);
            }
        }
        return null;
    }

    @Override
    public HashMap<ComponentKey, AppWidgetProviderInfo> getAllProvidersMap() {
        HashMap<ComponentKey, AppWidgetProviderInfo> result = new HashMap<>();
        if (FeatureFlags.GO_DISABLE_WIDGETS) {
            return result;
        }
        UserHandle handle = UserHandleCompat.myUserHandle().getUser();
        for (AppWidgetProviderInfo info :
                mAppWidgetManager.getInstalledProviders()) {
            result.put(new ComponentKey(info.provider, handle), info);
        }

        return  result;
    }
}
