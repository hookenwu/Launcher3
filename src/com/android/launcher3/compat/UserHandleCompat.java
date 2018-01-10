package com.android.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.UserHandle;

import com.android.launcher3.Utilities;

/**
 * Created by uchia on 2018/1/8.
 */

public class UserHandleCompat {
    private UserHandle mUser;

    private UserHandleCompat(UserHandle user) {
        mUser = user;
    }

    private UserHandleCompat() {
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static UserHandleCompat myUserHandle() {
        if (Utilities.ATLEAST_JB_MR1) {
            return new UserHandleCompat(android.os.Process.myUserHandle());
        } else {
            return new UserHandleCompat();
        }
    }

    public static UserHandleCompat fromUser(UserHandle user) {
        if (user == null) {
            return null;
        } else {
            return new UserHandleCompat(user);
        }
    }

    public UserHandle getUser() {
        return mUser;
    }

    @Override
    public String toString() {
        if (Utilities.ATLEAST_JB_MR1) {
            return mUser.toString();
        } else {
            return "";
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UserHandleCompat)) {
            return false;
        }
        if (Utilities.ATLEAST_JB_MR1) {
            return mUser.equals(((UserHandleCompat) other).mUser);
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        if (Utilities.ATLEAST_JB_MR1) {
            return mUser.hashCode();
        } else {
            return 0;
        }
    }

    /**
     * Adds {@link UserHandle} to the intent in for L or above.
     * Pre-L the launcher doesn't support showing apps for multiple
     * profiles so this is a no-op.
     */
    public void addToIntent(Intent intent, String name) {
        if (Utilities.ATLEAST_LOLLIPOP && mUser != null) {
            intent.putExtra(name, mUser);
        }
    }
}