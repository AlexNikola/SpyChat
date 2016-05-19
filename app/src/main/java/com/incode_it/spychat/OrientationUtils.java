package com.incode_it.spychat;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class OrientationUtils {
    private OrientationUtils() {}

    /** Locks the device window in landscape mode. */
    public static void lockOrientationLandscape(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /** Locks the device window in portrait mode. */
    public static void lockOrientationPortrait(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /** Allows user to freely use portrait or landscape mode. */
    public static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static void lockOrientation(Activity activity)
    {
        int currentOrientation = activity.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            OrientationUtils.lockOrientationLandscape(activity);
        }
        else {
            OrientationUtils.lockOrientationPortrait(activity);
        }
    }

}