package com.incode_it.spychat;

import android.support.multidex.MultiDexApplication;
import com.appnext.appnextsdk.AppnextTrack;

public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        AppnextTrack.track(this);
    }
}
