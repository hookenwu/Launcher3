package com.android.launcher3.compat;

import android.content.Context;
import android.content.pm.PackageInstaller;

import java.util.HashMap;
import java.util.List;

/**
 * Created by uchia on 2018/1/9.
 */

public class PackageInstallerCompatV16 extends PackageInstallerCompat {

    private Context mContext;

    PackageInstallerCompatV16(Context c){
        this.mContext = c;
    }

    @Override
    public HashMap<String, Integer> updateAndGetActiveSessionCache() {
        return new HashMap<>();
    }

    @Override
    public void onStop() {

    }

    @Override
    public List<PackageInstaller.SessionInfo> getAllVerifiedSessions() {
        return null;
    }
}
