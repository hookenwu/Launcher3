package com.android.launcher3.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.os.UserManager;

import com.android.launcher3.util.LongArrayMap;
import com.android.launcher3.util.ManagedProfileHeuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by uchia on 2018/1/8.
 */

public class UserManagerCompatV17  extends UserManagerCompat{

    private static final String USER_CREATION_TIME_KEY = "user_creation_time_";
    private final Context mContext;
    protected LongArrayMap<UserHandle> mUsers;
    // Create a separate reverse map as LongArrayMap.indexOfValue checks if objects are same
    // and not {@link Object#equals}
    protected HashMap<UserHandle, Long> mUserToSerialMap;

    protected UserManager mUserManager;

    UserManagerCompatV17(Context context) {
        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        mContext = context;
    }

    public long getSerialNumberForUser(UserHandleCompat user) {
        synchronized (this) {
            if (mUserToSerialMap != null) {
                Long serial = mUserToSerialMap.get(user);
                return serial == null ? 0 : serial;
            }
        }
        return mUserManager.getSerialNumberForUser(user.getUser());
    }

    public UserHandle getUserForSerialNumber(long serialNumber) {
        synchronized (this) {
            if (mUsers != null) {
                return mUsers.get(serialNumber);
            }
        }
        return mUserManager.getUserForSerialNumber(serialNumber);
    }

    @Override
    public CharSequence getBadgedLabelForUser(CharSequence label, UserHandle user) {
        return null;
    }

    @Override
    public long getUserCreationTime(UserHandle user) {
        SharedPreferences prefs = ManagedProfileHeuristic.prefs(mContext);
        String key = USER_CREATION_TIME_KEY + getSerialNumberForUser(user);
        if (!prefs.contains(key)) {
            prefs.edit().putLong(key, System.currentTimeMillis()).apply();
        }
        return prefs.getLong(key, 0);
    }

    @Override
    public boolean isQuietModeEnabled(UserHandle user) {
        return false;
    }

    @Override
    public boolean isUserUnlocked(UserHandle user) {
        return true;
    }

    @Override
    public boolean isDemoUser() {
        return false;
    }


    @Override
    public List<UserHandle> getUserProfiles() {

        return new ArrayList<UserHandle>(){
            {add(UserHandleCompat.myUserHandle().getUser());}
        };
    }

    @Override
    public long getSerialNumberForUser(UserHandle user) {
        synchronized (this) {
            if (mUserToSerialMap != null) {
                Long serial = mUserToSerialMap.get(user);
                return serial == null ? 0 : serial;
            }
        }
        return mUserManager.getSerialNumberForUser(user);
    }

    @Override
    public void enableAndResetCache() {
        synchronized (this) {
            mUsers = new LongArrayMap<>();
            mUserToSerialMap = new HashMap<>();
            UserHandleCompat myUser = UserHandleCompat.myUserHandle();
            long serial = mUserManager.getSerialNumberForUser(myUser.getUser());
            mUsers.put(serial, myUser.getUser());
            mUserToSerialMap.put(myUser.getUser(), serial);
        }
    }
}
