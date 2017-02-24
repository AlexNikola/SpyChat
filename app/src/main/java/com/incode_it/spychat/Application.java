package com.incode_it.spychat;

import com.appnext.appnextsdk.AppnextTrack;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppnextTrack.track(this);
    }
}
