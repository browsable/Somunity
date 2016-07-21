package com.soma.daemin.common;

import android.app.Application;

/**
 * Created by hernia on 2015-06-13.
 */
public class AppController extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyVolley.init(this);
    }
}
