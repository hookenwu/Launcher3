package com.android.launcher3.compat;


import android.support.annotation.Nullable;

/**
 * Created by uchia on 2018/1/8.
 */

public class WallpaperManagerCompatV19 extends WallpaperManagerCompat {



    @Nullable
    @Override
    public WallpaperColorsCompat getWallpaperColors(int which) {
        return null;
    }

    @Override
    public void addOnColorsChangedListener(OnColorsChangedListenerCompat listener) {

    }
}
